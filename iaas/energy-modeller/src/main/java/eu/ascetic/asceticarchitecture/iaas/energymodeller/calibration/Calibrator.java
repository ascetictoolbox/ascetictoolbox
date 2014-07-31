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

import eu.ascetic.asceticarchitecture.iaas.energymodeller.datastore.DataGatherer;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.HostDataSource;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.HostMeasurement;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.usage.HostEnergyCalibrationData;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This takes an existing host and calibrates the energy model, for it so that
 * predictions can be made regarding its energy usage.
 *
 * @author Richard
 */
public class Calibrator implements Runnable {

    private LoadGenerator generator = new DummyLoadGenerator(); //new DefaultLoadGenerator();
    private final HostDataSource datasource;
    private boolean running = true;
    private final LinkedBlockingDeque<Host> queue = new LinkedBlockingDeque<>();

    /**
     * This creates a new calibrator.
     *
     * @param dataSource The data source that is used to monitor the calibration
     * test.
     */
    public Calibrator(HostDataSource dataSource) {
        this.datasource = dataSource;
    }
    
    /**
     * This finds the upper and lower bounds of a host.
     *
     * @param host The host to train
     */    
    public void calibrateHostEnergyData(Host host) {
        queue.add(host);
    }
    
    /**
     * This checks to see if a host is in the training queue or not.
     * @param host The host to check to see if it is queued or not.
     * @return If the host is in the queue for training.
     */
    public boolean isQueued(Host host){
        return queue.contains(host);
    }

    /**
     * This finds the upper and lower bounds of a host.
     *
     * @param host The host to train
     * @return The newly trained/updated host
     */
    private Host performCalibration(Host host) {
        host = readLowestboundForHost(host);
        generator.generateCalibrationData(host);
        host = readEnergyDataForHost(host);
        return host;
    }

    /**
     * This method aims to read the energy data for a host and convert it into
     * the calibration data required for the energy modeller.
     *
     * @param host The host to get the calibration data for
     * @return The updated host
     */
    @SuppressWarnings("SleepWhileInLoop")
    private Host readEnergyDataForHost(Host host) {
        ArrayList<HostEnergyCalibrationData> calibrationData = host.getCalibrationData();
        ArrayList<HostMeasurement> data = new ArrayList<>();
        long lastClock = 0;

        /**
         * collect data for 2 mins because of wait interval + loop counter.
         */
        for (int i = 0; i < TimeUnit.MINUTES.toSeconds(1); i++) {
            HostMeasurement dataEntry = datasource.getHostData(host);
            long currentClock = dataEntry.getClock();
            if (currentClock > lastClock) {
                data.add(dataEntry);
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Logger.getLogger(DataGatherer.class.getName()).log(Level.SEVERE, "The data gatherer was interupted.", ex);
            }
        }
        calibrationData.addAll(HostEnergyCalibrationData.getCalibrationData(data));
        host.setCalibrationData(calibrationData);
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
        //TODO Implement better code here, fixing assumptions
        double cpu = 0.0; //An assumption that the lowest power usage measured has 0.0 cpu load
        double memory = 0.0; //The same but much poorer assumption
        double watts = datasource.getLowestHostPowerUsage(host);
        HostEnergyCalibrationData data = new HostEnergyCalibrationData(cpu, memory, watts);
        host.addCalibrationData(data);
        return host;
    }

    /**
     * This sets the load generator used by the calibration module.
     *
     * @param generator the generator to set
     */
    public void setGenerator(DefaultLoadGenerator generator) {
        this.generator = generator;
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
