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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The aim of the multi host power emulator is to push out power meter values
 * for hosts that do not have a watt meter attached but do have calibration
 * data. This will push data out for all hosts that are detectable by the
 * monitoring infrastructure. Thus in the cases where the raw power value is
 * missing the estimated power may be used instead.
 *
 * @author Richard Kavanagh
 */
public class MultiHostPowerEmulator implements Runnable {

    private HostDataSource source = new ZabbixDirectDbDataSourceAdaptor();
    private final DatabaseConnector database = new DefaultDatabaseConnector();
    private boolean running = true;
    private int pollInterval = 1;
    private String loggerOutputFile = "EstimatedHostPowerData.txt";
    private String outputName = "estimated-power";
    private String predictorName = "CpuOnlyBestFitEnergyPredictor";
    /**
     * Enables host discovery during run i.e. not just at the start It is faster
     * without need for continual host discovery
     */
    private boolean autoHostDiscovery = false;
    private final Settings settings = new Settings(PROPS_FILE_NAME);
    private static final String PROPS_FILE_NAME = "watt-meter-emulator.properties";
    private static final String DEFAULT_DATA_SOURCE_PACKAGE = "eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient";
    private static final String DEFAULT_PREDICTOR_PACKAGE = "eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor";

    /**
     * This creates a new multi host power emulator instance. It is to be used
     * on a server in place of Host power emulators on a per device basis. It
     * will then emulate all hosts that have calibration data.
     */
    public MultiHostPowerEmulator() {
        pollInterval = settings.getInt("poll_interval", pollInterval);
        loggerOutputFile = settings.getString("output_filename", loggerOutputFile);
        outputName = settings.getString("output_name", outputName);
        predictorName = settings.getString("predictor", predictorName);
        autoHostDiscovery = settings.getBoolean("auto_host_discovery", autoHostDiscovery);
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
     * This runs the emulation tool.
     *
     * @param args The first argument indicates the host to generate the host
     * power consumption data for, the second argument is optional and indicates
     *
     */
    public static void main(String[] args) {
        Thread emulatorThread;
        MultiHostPowerEmulator emulator = new MultiHostPowerEmulator();
        emulatorThread = new Thread(emulator);
        emulatorThread.setDaemon(false);
        emulatorThread.start();
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
        List<Host> hosts = source.getHostList();
        HostPowerLogger logger = new HostPowerLogger(new File(loggerOutputFile), true);
        logger.setMetricName(outputName);
        Thread loggerThread = new Thread(logger);
        loggerThread.setDaemon(true);
        loggerThread.start();
        EnergyPredictorInterface predictor = getPredictor(predictorName);
        getHostCalibrationData(hosts, predictor);
        /**
         * The main phase is to monitor the host and to report its estimated
         * host energy usage, in the event calibration data is available.
         */
        while (running) {
            if (autoHostDiscovery) {
                hosts = updateHostsList(hosts, predictor, logger);
            }
            List<HostMeasurement> mesurements = source.getHostData(hosts);
            for (HostMeasurement measurement : mesurements) {
                double power;
                Host host = measurement.getHost();
                if (!host.isCalibrated()) {
                    continue;
                }
                power = predictor.predictPowerUsed(host, measurement.getCpuUtilisation());
                logger.printToFile(logger.new Pair(host, power));
            }
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(pollInterval));
            } catch (InterruptedException ex) {
                Logger.getLogger(HostPowerEmulator.class.getName()).log(Level.SEVERE, "The power emulator was interupted.", ex);
            }
        }
    }

    /**
     * This collects host calibration data from the database
     *
     * @param hosts The list of hosts to calibrate.
     * @param predictor The predictor used to give performance information for
     * the host's fit
     * @return The list of hosts that have calibration data
     */
    private List<Host> getHostCalibrationData(List<Host> hosts, EnergyPredictorInterface predictor) {
        database.getHostCalibrationData(hosts);
        for (Host host : hosts) {
            if (!host.isCalibrated()) {
                hosts.remove(host);
                continue;
            }
            System.out.println("Host: " + host.toString());
            predictor.printFitInformation(host);
            System.out.println("");
        }
        return hosts;
    }

    /**
     * This updates the hosts list and ensures when a host disappears that its
     * last reported value is zero.
     *
     * @param hosts The previous list of hosts
     * @return The new list of hosts
     */
    private List<Host> updateHostsList(List<Host> hosts, EnergyPredictorInterface predictor, HostPowerLogger logger) {
        //The fresh copy of the host list.
        List<Host> newHostList = source.getHostList();
        //Hosts that have newly been discovered i.e. needs calibration data from DB
        List<Host> addedHosts = getHostListDifference(hosts, newHostList);
        //List of hosts that have disapeared. Needs zeroing
        List<Host> removedHosts = getHostListDifference(newHostList, hosts);
        //This will not add hosts that aren't calibrated
        hosts.addAll(getHostCalibrationData(addedHosts, predictor));
        for (Host removedHost : removedHosts) {
            logger.printToFile(logger.new Pair(removedHost, 0));
        }
        hosts.removeAll(removedHosts);
        return hosts;
    }

    /**
     * This compares a two list of hosts and shows whats been added
     *
     * @param originalList The new list of hosts.
     * @return The list of hosts that were otherwise unknown to the data
     * gatherer.
     */
    private List<Host> getHostListDifference(List<Host> newList, List<Host> originalList) {
        List<Host> answer = new ArrayList<>();
        for (Host host : originalList) {
            if (!newList.contains(host)) {
                answer.add(host);
            }
        }
        return answer;
    }

    /**
     * This allows the main thread to be stopped from running.
     */
    public void stop() {
        this.running = false;
    }

}
