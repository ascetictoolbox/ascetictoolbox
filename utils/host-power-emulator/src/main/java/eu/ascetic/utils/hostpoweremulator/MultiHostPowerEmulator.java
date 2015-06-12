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
import eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.EnergyPredictorInterface;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.HostDataSource;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.HostMeasurement;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.SigarDataSourceAdaptor;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.ZabbixDirectDbDataSourceAdaptor;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.ioutils.Settings;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
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
    private String outputName = "estimated-power";
    private final Settings settings = new Settings(PROPS_FILE_NAME);
    private static final String PROPS_FILE_NAME = "watt-meter-emulator.properties";
    private static final String DEFAULT_DATA_SOURCE_PACKAGE = "eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient";

    /**
     * This creates a new multi host power emulator instance. It is to be used
     * on a server in place of Host power emulators on a per device basis. It
     * will then emulate all hosts that have calibration data.
     */
    public MultiHostPowerEmulator() {
        pollInterval = settings.getInt("poll_interval", pollInterval);
        outputName = settings.getString("output_name", outputName);
        if (settings.isChanged()) {
            settings.save(PROPS_FILE_NAME);
        }
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
        HashMap<Host, EnergyPredictorInterface> predictorMap = new HashMap<>();
        CpuOnlyEnergyPredictor linearPredictor = new CpuOnlyEnergyPredictor();
        CpuOnlyPolynomialEnergyPredictor polyPredictor = new CpuOnlyPolynomialEnergyPredictor();
        ArrayList<EnergyPredictorInterface> predictors = new ArrayList<>();
        predictors.add(linearPredictor);
        predictors.add(polyPredictor);
        HostPowerLogger logger = new HostPowerLogger(new File("EstimatedHostPowerData.txt"), true);
        logger.setMetricName(outputName);
        Thread loggerThread = new Thread(logger);
        loggerThread.setDaemon(true);
        loggerThread.start();
        database.getHostCalibrationData(hosts);
        for (Host host : hosts) {
            if (!host.isCalibrated()) {
                continue;
            }
            EnergyPredictorInterface predictor = AbstractEnergyPredictor.getBestPredictor(host, predictors);
            predictorMap.put(host, predictor);            
            System.out.println("Using the " + predictor.toString());
            System.out.println("Linear SSE: " + linearPredictor.getSumOfSquareError(host));
            System.out.println("Polynomial SSE: " + polyPredictor.getSumOfSquareError(host));
            System.out.println("Linear RMSE: " + linearPredictor.getRootMeanSquareError(host));
            System.out.println("Polynomial RMSE: " + polyPredictor.getRootMeanSquareError(host));
            System.out.println("");
        }
        /**
         * The main phase is to monitor the host and to report its estimated
         * host energy usage, in the event calibration data is available.
         */
        while (running) {
            List<HostMeasurement> mesurements = source.getHostData(hosts);
            for (HostMeasurement measurement : mesurements) {
                double power;
                Host host = measurement.getHost();
                if (!host.isCalibrated()) {
                    continue;
                }
                power = predictorMap.get(host).predictPowerUsed(host, measurement.getCpuUtilisation());           
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
     * This allows the main thread to be stopped from running.
     */
    public void stop() {
        this.running = false;
    }

}
