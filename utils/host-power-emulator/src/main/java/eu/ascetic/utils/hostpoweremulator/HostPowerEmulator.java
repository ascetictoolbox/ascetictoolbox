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
import eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.AbstractEnergyPredictor;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.CpuOnlyEnergyPredictor;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.CpuOnlyPolynomialEnergyPredictor;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.CpuOnlySplinePolynomialEnergyPredictor;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.EnergyPredictorInterface;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.HostDataSource;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.HostMeasurement;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.SigarDataSourceAdaptor;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.ZabbixDirectDbDataSourceAdaptor;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.ioutils.Settings;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The aim of this application is to clone another's calibration profile so that
 * it can estimate the amount of energy used by the host.
 *
 */
public class HostPowerEmulator implements Runnable {

    private HostDataSource source = new ZabbixDirectDbDataSourceAdaptor();
    private final DatabaseConnector database = new DefaultDatabaseConnector();
    private String hostname = "";
    private String cloneHostname = "";
    private boolean running = true;
    private int pollInterval = 1;
    private String outputName = "power";
    private final Settings settings = new Settings(PROPS_FILE_NAME);
    private static final String PROPS_FILE_NAME = "watt-meter-emulator.properties";
    private static final String DEFAULT_DATA_SOURCE_PACKAGE = "eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient";

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
                    + "be the hostname to clone the calibration data from.");
            System.exit(0);
        }
        if (args.length >= 2) {
            cloneHostname = args[1];
        }
        HostPowerEmulator emulator = new HostPowerEmulator(hostname, cloneHostname);
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
        outputName = settings.getString("output_name", outputName);
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
        outputName = settings.getString("output_name", outputName);
        if (settings.isChanged()) {
            settings.save(PROPS_FILE_NAME);
        }
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
        EnergyPredictorInterface predictor;
        CpuOnlyEnergyPredictor linearPredictor = new CpuOnlyEnergyPredictor();
        CpuOnlyPolynomialEnergyPredictor polyPredictor = new CpuOnlyPolynomialEnergyPredictor();
        CpuOnlySplinePolynomialEnergyPredictor splinePolynomialPredictor = new CpuOnlySplinePolynomialEnergyPredictor();
        ArrayList<EnergyPredictorInterface> predictors = new ArrayList<>();
        predictors.add(linearPredictor);
        predictors.add(polyPredictor);
        predictors.add(splinePolynomialPredictor);
        Host host = source.getHostByName(hostname);
        HostPowerLogger logger = new HostPowerLogger(new File("EstimatedHostPowerData.txt"), true);
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
        predictor = AbstractEnergyPredictor.getBestPredictor(host, predictors);
        System.out.println("Using the " + predictor.toString());
        System.out.println("Linear - SSE: " + linearPredictor.getSumOfSquareError(host) + " RMSE: " + linearPredictor.getRootMeanSquareError(host));
        System.out.println("Polynomial - SSE: " + polyPredictor.getSumOfSquareError(host)  + " RMSE: " + polyPredictor.getRootMeanSquareError(host));
        System.out.println("Polynomial Spline - SSE: " + splinePolynomialPredictor.getSumOfSquareError(host)  + " RMSE: " + splinePolynomialPredictor.getRootMeanSquareError(host));

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
}
