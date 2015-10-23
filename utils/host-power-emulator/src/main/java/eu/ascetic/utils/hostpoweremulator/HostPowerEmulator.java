/**
 * Copyright 2015 University of Leeds
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package eu.ascetic.utils.hostpoweremulator;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.EnergyModeller;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.datastore.DatabaseConnector;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.datastore.DefaultDatabaseConnector;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.CpuOnlyBestFitEnergyPredictor;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.EnergyPredictorInterface;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.HostDataSource;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.HostMeasurement;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.SigarDataSourceAdaptor;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.ZabbixDirectDbDataSourceAdaptor;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.ioutils.Settings;
import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The aim of this application is to clone another's calibration profile so that
 * it can estimate the amount of energy used by the host.
 * 
 * @author Richard Kavanagh
 *
 */
public class HostPowerEmulator implements Runnable {

    private HostDataSource source = new ZabbixDirectDbDataSourceAdaptor();
    private final DatabaseConnector database = new DefaultDatabaseConnector();
    private String hostname = "";
    private String cloneHostname = "";
    private boolean stopOnClone = false;
    private boolean running = true;
    private int pollInterval = 1;
    private String loggerOutputFile = "EstimatedHostPowerData.txt";
    private String outputName = "power";
    private String predictorName = "CpuOnlyBestFitEnergyPredictor";
    private final Settings settings = new Settings(PROPS_FILE_NAME);
    private static final String PROPS_FILE_NAME = "watt-meter-emulator.properties";
    private static final String DEFAULT_DATA_SOURCE_PACKAGE = "eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient";
    private static final String DEFAULT_PREDICTOR_PACKAGE = "eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor";

    /**
     * This runs the emulation tool.
     *
     * @param args The first argument indicates the host to generate the host
     * power consumption data for, the second argument is optional and indicates
     * the host to clone calibration data from.
     */
    public static void main(String[] args) {
        Thread emulatorThread;
        String hostname = null;
        String cloneHostname = null;
        if (args.length > 0) {
            hostname = args[0];
        } else {
            System.out.println("The first argument provided to this application"
                    + " should be the hostname, the second if needed should "
                    + "be the hostname to clone the calibration data from."
                    + " An optional last argument of stop_on_clone will allow "
                    + "the power emulator to be used for cloning calibration data.");
            System.exit(0);
        }
        if (args.length >= 2) {
            cloneHostname = args[1];
        }
        HostPowerEmulator emulator = new HostPowerEmulator(hostname, cloneHostname);
        if (args.length >= 3 && "stop_on_clone".equals(args[2])) {
            emulator.setStopOnClone(true);
        }        
        emulatorThread = new Thread(emulator);
        emulatorThread.setDaemon(false);
        emulatorThread.start();
    }

    /**
     * This creates a new host power emulator instance.
     *
     * @param hostname The host to emulate a watt meter for.
     * @param cloneHostname The host to clone the calibration data from in the
     * event the first one is not calibrated.
     */
    public HostPowerEmulator(String hostname, String cloneHostname) {
        this.hostname = hostname;
        this.cloneHostname = cloneHostname;
        pollInterval = settings.getInt("poll_interval", pollInterval);
        loggerOutputFile = settings.getString("output_filename", loggerOutputFile);
        outputName = settings.getString("output_name", outputName);
        predictorName = settings.getString("predictor", predictorName);
        if (settings.isChanged()) {
            settings.save(PROPS_FILE_NAME);
        }
    }

    /**
     * This creates a new host power emulator instance.
     *
     * @param hostname The host to emulate a watt meter for.
     */
    public HostPowerEmulator(String hostname) {
        this.hostname = hostname;
        this.cloneHostname = null;
        pollInterval = settings.getInt("poll_interval", pollInterval);
        loggerOutputFile = settings.getString("output_filename", loggerOutputFile);        
        outputName = settings.getString("output_name", outputName);
        predictorName = settings.getString("predictor", predictorName);
        if (settings.isChanged()) {
            settings.save(PROPS_FILE_NAME);
        }
    }
    
    /**
     * This allows the power estimator to be set
     *
     * @param powerUtilisationPredictor The name of the predictor to use
     * @return The predictor to use.
     */
    public EnergyPredictorInterface getPredictor(String powerUtilisationPredictor) {
        EnergyPredictorInterface answer = null;
        try {
            if (!powerUtilisationPredictor.startsWith(DEFAULT_PREDICTOR_PACKAGE)) {
                powerUtilisationPredictor = DEFAULT_PREDICTOR_PACKAGE + "." + powerUtilisationPredictor;
            }
            answer = (EnergyPredictorInterface) (Class.forName(powerUtilisationPredictor).newInstance());
        } catch (ClassNotFoundException ex) {
            if (answer == null) {
                answer = new CpuOnlyBestFitEnergyPredictor();
            }
            Logger.getLogger(HostPowerEmulator.class.getName()).log(Level.WARNING, "The predictor specified was not found");
        } catch (InstantiationException | IllegalAccessException ex) {
            if (answer == null) {
                answer = new CpuOnlyBestFitEnergyPredictor();
            }
            Logger.getLogger(HostPowerEmulator.class.getName()).log(Level.WARNING, "The predictor specified did not work", ex);
        }
        return answer;
    }    

    /**
     * This clones a host's calibration data in the event the first host named
     * is not calibrated.
     *
     * @param hostname The host to calibrate if it is not calibrated.
     * @param cloneHostname The host to clone the calibration data from in the
     * event the first one is not calibrated.
     */
    public void cloneHostProfile(String hostname, String cloneHostname) {
        Host host = source.getHostByName(hostname);
        Host clone = source.getHostByName(cloneHostname);
        host = database.getHostCalibrationData(host);
        if (!host.isCalibrated()) {
            clone = database.getHostCalibrationData(clone);
            host.setCalibrationData(clone.getCalibrationData());
            database.setHostCalibrationData(host);
        }
        if (stopOnClone == true) {
            running = false;
        }
    }

    /**
     * This allows the energy modellers data source to be set
     *
     * @param dataSource The name of the data source to use for the energy
     * modeller
     */
    public void setDataSource(String dataSource) {
        try {
            if (!dataSource.startsWith(DEFAULT_DATA_SOURCE_PACKAGE)) {
                dataSource = DEFAULT_DATA_SOURCE_PACKAGE + "." + dataSource;
            }
            /**
             * This is a special case that requires it to be loaded under the
             * singleton design pattern.
             */
            String wattMeter = DEFAULT_DATA_SOURCE_PACKAGE + ".WattsUpMeterDataSourceAdaptor";
            if (wattMeter.equals(dataSource)) {
                source = SigarDataSourceAdaptor.getInstance();
            } else {
                source = (HostDataSource) (Class.forName(dataSource).newInstance());
            }
        } catch (ClassNotFoundException ex) {
            if (source == null) {
                source = new ZabbixDirectDbDataSourceAdaptor();
            }
            Logger.getLogger(EnergyModeller.class.getName()).log(Level.WARNING, "The data source specified was not found");
        } catch (InstantiationException | IllegalAccessException ex) {
            if (source == null) {
                source = new ZabbixDirectDbDataSourceAdaptor();
            }
            Logger.getLogger(EnergyModeller.class.getName()).log(Level.WARNING, "The data source did not work", ex);
        }
    }

    @Override
    public void run() {
        EnergyPredictorInterface predictor = getPredictor(predictorName);
        Host host = source.getHostByName(hostname);
        HostPowerLogger logger = new HostPowerLogger(new File(loggerOutputFile), true);
        logger.setMetricName(outputName);
        Thread loggerThread = new Thread(logger);
        loggerThread.setDaemon(true);
        loggerThread.start();
        database.getHostCalibrationData(host);

        /**
         * The first phase is to clone the resource calibration data of another
         * in the event that this is necessary.
         */
        if (cloneHostname != null) {
            cloneHostProfile(hostname, cloneHostname);
            try { //Ensure there is enough time to copy the calibration data.
                Thread.sleep(TimeUnit.SECONDS.toMillis(5));
            } catch (InterruptedException ex) {
                Logger.getLogger(HostPowerEmulator.class.getName()).log(Level.SEVERE, "The power emulator was interupted.", ex);
            }
            database.getHostCalibrationData(host);
        }
        if (!host.isCalibrated()) {
            running = false;
            System.out.println("The host has no calibration data, so emulation cannot occur! ");
            System.out.println("Please specify where to clone the calibration data from.");
            System.exit(0);
        }
        predictor.printFitInformation(host);

        /**
         * The second phase is to monitor the host and to report its estimated
         * host energy usage.
         */
        while (running) {
            HostMeasurement measurement = source.getHostData(host);
            double power = predictor.predictPowerUsed(host, measurement.getCpuUtilisation());
            logger.printToFile(logger.new Pair(host, power));
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(pollInterval));
            } catch (InterruptedException ex) {
                Logger.getLogger(HostPowerEmulator.class.getName()).log(Level.SEVERE, "The power emulator was interupted.", ex);
            }
        }
    }

    /**
     * This returns the host name which the watt meter emulator runs for
     *
     * @return the name of the host
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * This sets the host name which the watt meter emulator runs for
     *
     * @param hostname the host name to set
     */
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    /**
     * This gets the name of the host which the calibration data should be
     * cloned.
     *
     * @return The name of the host from which the calibration data should be
     * cloned.
     */
    public String getCloneHostname() {
        return cloneHostname;
    }

    /**
     * This sets the name of the host which the calibration data should be
     * cloned.
     *
     * @param cloneHostname the cloneHostname to set
     */
    public void setCloneHostname(String cloneHostname) {
        this.cloneHostname = cloneHostname;
    }

    /**
     * This indicates if the main thread should carry on running.
     *
     * @return the running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * This allows the main thread to be stopped from running.
     */
    public void stop() {
        this.running = false;
    }

    /**
     * This checks to see if the emulator will stop on cloning data, 
     * thus it acts as a cloning tool.
     * @return the stopOnClone
     */
    public boolean isStopOnClone() {
        return stopOnClone;
    }

    /**
     * This stops the emulator on cloning data, thus it acts as a cloning tool.
     * @param stopOnClone the stopOnClone to set
     */
    public void setStopOnClone(boolean stopOnClone) {
        this.stopOnClone = stopOnClone;
    }
}
