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

import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.HostDataSource;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.HostMeasurement;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.usage.HostEnergyCalibrationData;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This logs the data for a specified host and writes them out to disk.
 * @author Richard
 */
public class CalibratorDataLogger implements Runnable {

    private final Host host;
    private final HostDataSource datasource;
    private final int calibratorWaitSec;
    private final int calibratorMaxDurationSec;

    public CalibratorDataLogger(Host host, HostDataSource datasource, 
            int calibratorWaitSec, int calibratorMaxDurationSec) {
        this.host = host;
        this.datasource = datasource;
        this.calibratorWaitSec = calibratorWaitSec;
        this.calibratorMaxDurationSec = calibratorMaxDurationSec;
    }

    /**
     * This method aims to read the energy data for a host and convert it into
     * the calibration data required for the energy modeller.
     *
     * @param host The host to get the calibration data for
     * @return The updated host
     */
    private Host readEnergyDataForHost(Host host) {
        ArrayList<HostEnergyCalibrationData> calibrationData = host.getCalibrationData();
        ArrayList<HostMeasurement> data = new ArrayList<>();
        long lastClock = 0;
        
        long stopTime = System.currentTimeMillis();
        stopTime = stopTime + TimeUnit.SECONDS.toMillis(calibratorMaxDurationSec);

        while (System.currentTimeMillis() < stopTime) {
            HostMeasurement dataEntry = datasource.getHostData(host);
            long currentClock = dataEntry.getClock();
            if (currentClock > lastClock) {
                data.add(dataEntry);
            }
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(calibratorWaitSec));
            } catch (InterruptedException ex) {
                Logger.getLogger(Calibrator.class.getName()).log(Level.SEVERE, "The data gatherer was interupted.", ex);
            }
        }
        calibrationData.addAll(HostEnergyCalibrationData.getCalibrationData(data));
        host.setCalibrationData(calibrationData);
        return host;
    }

    @Override
    public void run() {
        /**
         * Note the aim of starting a thread is so that the calibrator can take
         * measurements while the load generator is doing its work.
         */
        if (host != null) {
            readEnergyDataForHost(host);
        }
    }

}
