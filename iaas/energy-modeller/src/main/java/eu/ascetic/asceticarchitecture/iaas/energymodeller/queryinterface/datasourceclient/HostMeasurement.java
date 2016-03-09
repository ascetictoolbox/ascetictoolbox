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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient;

import static eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.KpiList.ENERGY_KPI_NAME;
import static eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.KpiList.ESTIMATED_POWER_KPI_NAME;
import static eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.KpiList.POWER_KPI_NAME;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import java.util.List;

/**
 * This represents a single snapshot of the data from a data source.
 *
 * @author Richard Kavanagh
 */
public class HostMeasurement extends Measurement {

    private Host host;

    /**
     * This creates a host measurement.
     *
     * @param host The host the measurement is for
     */
    public HostMeasurement(Host host) {
        this.host = host;
    }

    /**
     * This creates a host measurement.
     *
     * @param host The host the measurement is for
     * @param clock The time when the measurement was taken, this is in unix
     * time. i.e. Calendar.
     */
    public HostMeasurement(Host host, long clock) {
        this.host = host;
        setClock(clock);
    }

    /**
     * The gets the host that the measurement is for.
     *
     * @return The host that the measurement is for.
     */
    public Host getHost() {
        return host;
    }

    /**
     * The sets the host that the measurement is for.
     *
     * @param host The host that the measurement is for.
     */
    public void setHost(Host host) {
        this.host = host;
    }

    @Override
    public String toString() {
        return host.toString() + " Time: " + getClock() + " Metric Count: " + getMetricCount() + " Clock Diff: " + getMaximumClockDifference();
    }

    /**
     * This provides rapid access to power values from a host measurement.
     *
     * @return The power consumed when the measurement was taken.
     */
    public double getPower() {
        return this.getMetric(POWER_KPI_NAME).getValue();
    }

    /**
     * This provides rapid access to indicate if the power metric exists or not.
     *
     * @return If the power metric is contained inside this host measurement.
     */
    public boolean getPowerMetricExist() {
        return this.metricExists(POWER_KPI_NAME);
    }
    
    /**
     * This provides rapid access to estimated power values from a host measurement.
     *
     * @return The power consumed when the measurement was taken.
     */
    public double getEstimatedPower() {
        return this.getMetric(ESTIMATED_POWER_KPI_NAME).getValue();
    }

    /**
     * This provides rapid access to indicate if the estimated power metric 
     * exists or not.
     *
     * @return If the power metric is contained inside this host measurement.
     */
    public boolean getEstimatedPowerMetricExist() {
        return this.metricExists(ESTIMATED_POWER_KPI_NAME);
    }
    
    /**
     * This gets the power or substitute power from host measurements.
     * @param safe If true estimated power will be used as a substitute in cases
     * where the power value is not found or is negative.
     * @return This returns the power value for a host measurement or the 
     * estimated power if that does not exist. In the event neither exist it will
     * return -1.
     */
    public double getPower(boolean safe) {
        if (!safe) {
            return getPower();
        }
        if (getPowerMetricExist() && getPower() >= 0) {
            return getPower();
        } else {
            /**
             * This is a fall back to estimated values from the emulated watt
             * meter.
             */
            if (getEstimatedPowerMetricExist()) {
                return getEstimatedPower();
            }
        }
        return -1;
    }
    
    /**
     * This takes a list of host measurements and sums the power consumption
     * @param toSum The list to sum together
     * @return The total power of the list of host measurements
     */
    public static double sumPower(List<HostMeasurement> toSum) {
        double answer = 0;
        for (HostMeasurement item : toSum) {
            answer = answer + item.getPower(true);
        }
        return answer;
    }    

    /**
     * This provides rapid access to energy value from a host measurement.
     *
     * @return The energy consumed when the measurement was taken, going back to
     * an unspecified period of time. To be used like a meter reading that you
     * might give to an energy company.
     */
    public double getEnergy() {
        return this.getMetric(ENERGY_KPI_NAME).getValue();
    }

    /**
     * This provides rapid access to indicate if the energy metric exists or not.
     *
     * @return If the energy metric is contained inside this host measurement.
     */
    public boolean getEnergyMetricExist() {
        return this.metricExists(ENERGY_KPI_NAME);
    }

    /**
     * This provides rapid access to indicate if the power and energy metric
     * exists or not.
     *
     * @return If the power and energy metrics are contained inside this host
     * measurement.
     */
    public boolean getPowerAndEnergyMetricsExist() {
        return getPowerMetricExist() && getEnergyMetricExist();
    }

}
