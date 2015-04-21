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
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.HostMeasurement;
import static eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.KpiList.POWER_KPI_NAME;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.usage.HostEnergyCalibrationData;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This logs the data for a specified host and writes them out to disk.
 *
 * @author Richard
 */
public class CalibratorDataLogger implements Runnable {

    private final Host host;
    private final HostDataSource datasource;
    private final int calibratorWaitSec;
    private final int calibratorMaxDurationSec;
    private final DatabaseConnector database;

    /**
     * This creates a data logger for calibration information.
     *
     * @param host The host to get the calibration data for
     * @param datasource The data source to use to gather the required data
     * @param database The database used to store the information gathered
     * @param calibratorWaitSec The duration between readings in seconds
     * @param calibratorMaxDurationSec The duration the calibration phase occurs
     * for
     */
    public CalibratorDataLogger(Host host, HostDataSource datasource, DatabaseConnector database,
            int calibratorWaitSec, int calibratorMaxDurationSec) {
        this.host = host;
        this.datasource = datasource;
        this.database = database;
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
        double lastPowerValue = 0;
        int faultCount = 0;
        long stopTime = System.currentTimeMillis();
        stopTime = stopTime + TimeUnit.SECONDS.toMillis(calibratorMaxDurationSec);

        while (System.currentTimeMillis() < stopTime) {
            try {
                HostMeasurement dataEntry = datasource.getHostData(host);
                if (dataEntry.getMetric(POWER_KPI_NAME) == null) {
                    faultCount = faultCount + 1;
                    if (faultCount > 25) {
                        break; //Exit if faults keep occuring in a sequence.
                    }
                    continue; //No power reading so fail straight away
                }
                long currentClock = dataEntry.getClock();
                double currentPower = dataEntry.getPower();
                /**
                 * The next checks ensure, that at least one metric value has
                 * been updated since the last poll interval. Plus it checks
                 * that the power and CPU values are within close enough
                 * proximity to be still useful. and finally it checks to make
                 * sure the dependant and independent variables are within close
                 * enough to be still representing the same time period.
                 */
                if (currentClock > lastClock
                        && dataEntry.isContemporary(POWER_KPI_NAME,
                                dataEntry.getCpuUtilisationTimeStamp(), 3)
                        && absdifference(currentPower, lastPowerValue) < 0.1) {
                    data.add(dataEntry);
                }
                lastClock = dataEntry.getClock();
                lastPowerValue = dataEntry.getPower();
                try {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(calibratorWaitSec));
                } catch (InterruptedException ex) {
                    Logger.getLogger(CalibratorDataLogger.class.getName()).log(Level.SEVERE, "The calibrator's data logger was interupted.", ex);
                }
                faultCount = (faultCount > 0 ? faultCount - 1 : 0);
            } catch (Exception ex) { //This should always try to gather data from the data source.
                Logger.getLogger(CalibratorDataLogger.class.getName()).log(Level.SEVERE, "The calibrator's data logger had a problem.", ex);
                faultCount = faultCount + 1;
                if (faultCount > 25) {
                    break; //Exit if faults keep occuring in a sequence.
                }
            }
        }
        calibrationData.addAll(HostEnergyCalibrationData.getCalibrationData(data));
        host.setCalibrationData(calibrationData);
        if (database != null) {
            database.setHostCalibrationData(host);
        }
        return host;
    }

    /**
     * This returns the absolute difference between two values
     *
     * @param value1 The first value
     * @param value2 The second value
     * @return The absolute difference between the two values
     */
    private double absdifference(double value1, double value2) {
        return Math.max(value1, value2) - Math.min(value1, value2);
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
