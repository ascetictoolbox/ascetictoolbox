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
     * @param args
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
        database.getHostCalibrationData(host);
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
            EnergyUsagePrediction prediction = predictor.getHostPredictedEnergy(host,null);
            double power = prediction.getAvgPowerUsed();
        }
    }

    /**
     * @return the hostname
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * @param hostname the hostname to set
     */
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    /**
     * @return the cloneHostname
     */
    public String getCloneHostname() {
        return cloneHostname;
    }

    /**
     * @param cloneHostname the cloneHostname to set
     */
    public void setCloneHostname(String cloneHostname) {
        this.cloneHostname = cloneHostname;
    }

    /**
     * @return the running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * @param running the running to set
     */
    public void setRunning(boolean running) {
        this.running = running;
    }
}
