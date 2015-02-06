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

import eu.ascetic.asceticarchitecture.iaas.energymodeller.datastore.DatabaseConnector;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.datastore.DefaultDatabaseConnector;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.CpuOnlyEnergyPredictor;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.HostDataSource;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.ZabbixDirectDbDataSourceAdaptor;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.EnergyUsagePrediction;
import java.io.File;

/**
 * The aim of this application is to clone another's calibration profile so that
 * it can estimate the amount of energy used by the host.
 *
 */
public class HostPowerEmulator implements Runnable {

    private final HostDataSource source = new ZabbixDirectDbDataSourceAdaptor();
    private final DatabaseConnector database = new DefaultDatabaseConnector();
    private String hostname = "";
    private String cloneHostname = "";
    private boolean running = true;

    /**
     * This runs the emulation tool.
     *
     * @param args The first argument indicates the host to generate the host
     * power consumption data for, the second argument is optional and indicates
     * 
     */
    public static void main(String[] args) {
        Thread emulatorThread;
        String hostname = null;
        String cloneHostname = "";
        if (args.length > 1) {
            hostname = args[0];
        } else {
            System.out.println("The first argument provided to this application"
                    + "should be the hostname, the second if needed should "
                    + "be the hostname to clone the calibration data from.");
            System.exit(0);
        }
        if (args.length > 2) {
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
    }

    /**
     * This creates a new host power emulator instance.
     *
     * @param hostname The host to emulate a watt meter for.
     */
    public HostPowerEmulator(String hostname) {
        this.hostname = hostname;
        this.cloneHostname = null;
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
        }

    }

    @Override
    public void run() {
        CpuOnlyEnergyPredictor predictor = new CpuOnlyEnergyPredictor();
        Host host = source.getHostByName(hostname);
        HostPowerLogger logger = new HostPowerLogger(new File("EstimatedHostPowerData.txt"), true);
        Thread loggerThread = new Thread(logger);
        loggerThread.setDaemon(true);
        loggerThread.start();
        database.getHostCalibrationData(host);
        if (!host.isCalibrated()) {
            running = false;
            System.out.println("The host has no calibration data, so emulation cannot occur!"
                    + "please specifiy where to clone the calibration data from.");
            System.exit(0);
        }
        /**
         * The first phase is to clone the resource calibration data of another
         * in the event that this is necessary.
         */
        if (cloneHostname != null) {
            cloneHostProfile(hostname, cloneHostname);
        }
        /**
         * The second phase is to monitor the host and to report its estimated
         * host energy usage.
         */
        while (running) {
            EnergyUsagePrediction prediction = predictor.getHostPredictedEnergy(host, null);
            double power = prediction.getAvgPowerUsed();
            logger.printToFile(logger.new Pair(host, power));
        }
    }

    /**
     * This returns the host name which the watt meter emulator runs for
     * @return the name of the host
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * This sets the host name which the watt meter emulator runs for
     * @param hostname the host name to set
     */
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    /**
     * This gets the name of the host which the calibration data should be cloned.
     * @return The name of the host from which the calibration data should be cloned.
     */
    public String getCloneHostname() {
        return cloneHostname;
    }

    /**
     * This sets the name of the host which the calibration data should be cloned.
     * @param cloneHostname the cloneHostname to set
     */
    public void setCloneHostname(String cloneHostname) {
        this.cloneHostname = cloneHostname;
    }

    /**
     * This indicates if the main thread should carry on running.
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
