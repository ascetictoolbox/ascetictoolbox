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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.TimePeriod;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.EnergyUsageSource;
import java.util.Calendar;
import java.util.HashSet;

/**
 *
 * This stores a record of the historical energy usage of either a VM or a
 * underlying resource. It is expected to indicate the: Avg Watts used over time
 * Avg Current (useful??) Avg Voltage (useful??) kWh of energy used since
 * instantiation/boot
 *
 * @author Richard
 */
public class HistoricUsageRecord extends EnergyUsageRecord {

    private double avgPowerUsed; // units Watts
    private double avgCurrentUsed; //units Amps
    private double avgVoltageUsed; //units Volts
    private double totalEnergyUsed; //units kWh
    private TimePeriod duration; // The time period to which the results correspond.

    public HistoricUsageRecord(EnergyUsageSource energyUser) {
        addEnergyUser(energyUser);
    }    
    
    public HistoricUsageRecord(HashSet<EnergyUsageSource> energyUser) {
        addEnergyUser(energyUser);
    }

    public HistoricUsageRecord(HashSet<EnergyUsageSource> energyUser, double avgPowerUsed, double avgCurrentUsed, double avgVoltageUsed, double totalEnergyUsed) {
        addEnergyUser(energyUser);
        this.avgPowerUsed = avgPowerUsed;
        this.avgCurrentUsed = avgCurrentUsed;
        this.avgVoltageUsed = avgVoltageUsed;
        this.totalEnergyUsed = totalEnergyUsed;
    }
    
    /**
     * @return the avgPowerUsed
     */
    public double getAvgPowerUsed() {
        return avgPowerUsed;
    }

    /**
     * @param avgPowerUsed the avgPowerUsed to set
     */
    public void setAvgPowerUsed(double avgPowerUsed) {
        this.avgPowerUsed = avgPowerUsed;
    }

    /**
     * @return the avgCurrentUsed
     */
    public double getAvgCurrentUsed() {
        return avgCurrentUsed;
    }

    /**
     * @param avgCurrentUsed the avgCurrentUsed to set
     */
    public void setAvgCurrentUsed(double avgCurrentUsed) {
        this.avgCurrentUsed = avgCurrentUsed;
    }

    /**
     * @return the avgVoltageUsed
     */
    public double getAvgVoltageUsed() {
        return avgVoltageUsed;
    }

    /**
     * @param avgVoltageUsed the avgVoltageUsed to set
     */
    public void setAvgVoltageUsed(double avgVoltageUsed) {
        this.avgVoltageUsed = avgVoltageUsed;
    }

    /**
     * @return the totalEnergyUsed
     */
    public double getTotalEnergyUsed() {
        return totalEnergyUsed;
    }

    /**
     * @param totalEnergyUsed the totalEnergyUsed to set
     */
    public void setTotalEnergyUsed(double totalEnergyUsed) {
        this.totalEnergyUsed = totalEnergyUsed;
    }

    /**
     * @return the recordStartTime
     */
    public Calendar getRecordStartTime() {
        return getDuration().getStartTime();
    }

    /**
     * @return the recordEndTime
     */
    public Calendar getRecordEndTime() {
        return getDuration().getEndTime();
    }

    /**
     * @return the duration
     */
    public TimePeriod getDuration() {
        return duration;
    }

    /**
     * @param duration the duration to set
     */
    public void setDuration(TimePeriod duration) {
        this.duration = duration;
    }

}
