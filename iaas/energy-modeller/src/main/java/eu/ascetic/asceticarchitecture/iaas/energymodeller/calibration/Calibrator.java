/**
 * Copyright 2014 University of Leeds
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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.calibration;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.datastore.DatabaseConnector;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.HostDataSource;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import java.io.File;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * This takes an existing host and calibrates the energy model, for it so that
 * predictions can be made regarding its energy usage.
 *
 * @author Richard
 */
public class Calibrator implements Runnable {

    private HostDataSource datasource;
    private final DatabaseConnector database;
    private boolean running = true;
    private final LinkedBlockingDeque<Host> queue = new LinkedBlockingDeque<>();
    private static int calibratorWaitSec = 2; //Default 2 second poll interval during training
    private static int calibratorMaxDurationSec = 240; //for 2 minutes.
    private static String defaultLoadGenerator = "DefaultLoadGenerator";
    private static final String CONFIG_FILE = "energy-modeller-calibrator.properties";
    private static final String DEFAULT_LOAD_GEN_PACKAGE = "eu.ascetic.asceticarchitecture.iaas.energymodeller.calibration";
    private Class<?> loadGenerator = DummyLoadGenerator.class;
    private String loadGeneratorDomain = ".cit.tu-berlin.de:8080/energy-modeller-load-calibration-tool-0.0.1-SNAPSHOT/";

    /**
     * This creates a new calibrator.
     *
     * @param dataSource The data source that is used to monitor the calibration
     * @param database The database to write the final results to once gathered.
     * test.
     */
    public Calibrator(HostDataSource dataSource, DatabaseConnector database) {
        this.datasource = dataSource;
        this.database = database;
        try {
            PropertiesConfiguration config;
            if (new File(CONFIG_FILE).exists()) {
                config = new PropertiesConfiguration(CONFIG_FILE);
            } else {
                config = new PropertiesConfiguration();
                config.setFile(new File(CONFIG_FILE));
            }
            config.setAutoSave(true); //This will save the configuration file back to disk. In case the defaults need setting.
            calibratorWaitSec = config.getInt("iaas.energy.modeller.calibrator.wait", calibratorWaitSec);
            config.setProperty("iaas.energy.modeller.calibrator.wait", calibratorWaitSec);
            calibratorMaxDurationSec = config.getInt("iaas.energy.modeller.calibrator.duration", calibratorMaxDurationSec);
            config.setProperty("iaas.energy.modeller.calibrator.duration", calibratorMaxDurationSec);
            defaultLoadGenerator = config.getString("iaas.energy.modeller.calibrator.load.generator", defaultLoadGenerator);
            config.setProperty("iaas.energy.modeller.calibrator.load.generator", defaultLoadGenerator);
            setGenerator(defaultLoadGenerator);
            loadGeneratorDomain = config.getString("iaas.energy.modeller.calibrator.load.generator.domain", loadGeneratorDomain);
            config.setProperty("iaas.energy.modeller.calibrator.load.generator.domain", loadGeneratorDomain);

        } catch (ConfigurationException ex) {
            Logger.getLogger(Calibrator.class.getName()).log(Level.INFO, "Error loading the configuration of the IaaS energy modeller", ex);
        }
    }

    /**
     * This performs a calibration for a host finding its power usage profile.
     * Including details of its upper and lower bounds of power consumption.
     *
     * @param host The host to train
     */
    public void calibrateHostEnergyData(Host host) {
        queue.add(host);
    }

    /**
     * This checks to see if a host is in the training queue or not.
     *
     * @param host The host to check to see if it is queued or not.
     * @return If the host is in the queue for training.
     */
    public boolean isQueued(Host host) {
        return queue.contains(host);
    }

    /**
     * This finds the upper and lower bounds of a host.
     *
     * @param host The host to train
     * @return The newly trained/updated host
     */
    private Host performCalibration(Host host) {
        LoadGenerator generator;
        try {
            generator = (LoadGenerator) loadGenerator.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(Calibrator.class.getName()).log(Level.WARNING, "The load generator specified could not be instantiated", ex);
            generator = new DummyLoadGenerator();
        }
        //Set the load generator going
        generator.setHost(host);
        generator.setDomain(loadGeneratorDomain);
        Thread loadGeneratorThread = new Thread(generator);
        loadGeneratorThread.setDaemon(true);
        loadGeneratorThread.start();
        //Gather basic data
        host = readLowestboundForHost(host);
        //Perform the full logging
        CalibratorDataLogger logger = new CalibratorDataLogger(host, datasource, database, calibratorWaitSec, calibratorMaxDurationSec);
        Thread dataLoggerThread = new Thread(logger);
        dataLoggerThread.setDaemon(true);
        dataLoggerThread.start();
        return host;
    }

    /**
     * This looks at the historic log values for a host and finds the lowest
     * value ever read. The aim is to find the minimum bound for how much energy
     * a host uses. This value will then need sharing out among VMs as it is the
     * amount of energy used for the host being switched on.
     *
     * @param host The host to train
     * @return The updated host
     */
    private Host readLowestboundForHost(Host host) {
        /**
         * This is separated from the reset of the calibration procedure as it
         * can be called again without having to induce load on the host.
         */
        double watts = datasource.getLowestHostPowerUsage(host);
        host.setDefaultIdlePowerConsumption(watts);
        return host;
    }

    /**
     * This sets the load generator used by the calibration module.
     *
     * @param loadGenerator the generator to set
     *
     * If the name of the class is not identified then the dummy load generator
     * will be loaded as a default.
     */
    public final void setGenerator(String loadGenerator) {
        try {
            if (!loadGenerator.startsWith(DEFAULT_LOAD_GEN_PACKAGE)) {
                loadGenerator = DEFAULT_LOAD_GEN_PACKAGE + "." + loadGenerator;
            }
            this.loadGenerator = (Class.forName(loadGenerator));
        } catch (ClassNotFoundException ex) {
            if (this.loadGenerator == null) {
                this.loadGenerator = DummyLoadGenerator.class;
            }
            Logger.getLogger(Calibrator.class.getName()).log(Level.WARNING, "The load generator specified was not found", ex);
        }
    }

    /**
     * This allows the data source for calibration data to be set.
     *
     * @param datasource The data source to set
     */
    public void setDatasource(HostDataSource datasource) {
        this.datasource = datasource;
    }

    /**
     * This stops the calibrator from running.
     */
    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        /**
         * Note the aim of starting a thread is so that the main, thread doesn't
         * have to wait for calibration to occur. It can carry on going once
         * calibration has started.
         */
        while (running) {
            try {
                Host currentItem = queue.poll(30, TimeUnit.SECONDS);
                if (currentItem != null) {
                    performCalibration(currentItem);
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(Calibrator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
