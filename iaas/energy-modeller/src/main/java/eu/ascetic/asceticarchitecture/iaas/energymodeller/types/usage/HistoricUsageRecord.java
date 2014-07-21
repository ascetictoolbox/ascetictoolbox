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
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

    /**
     * This creates a historic usage record, for the energy user specified.
     *
     * @param energyUser The energy user.
     */
    public HistoricUsageRecord(EnergyUsageSource energyUser) {
        addEnergyUser(energyUser);
    }

    /**
     * This creates a historic usage record, for the energy user specified. This
     * allows a summary record to be setup for a set of hosts or VMs.
     *
     * @param energyUsers The energy users.
     */
    public HistoricUsageRecord(HashSet<EnergyUsageSource> energyUsers) {
        addEnergyUser(energyUsers);
    }

    /**
     * This creates a historic usage record from a list of host energy records.
     * The historic usage record therefore acts a summary.
     *
     * @param energyUser The energy user, namely a host.
     * @param data The data about how much energy the host has used.
     */
    public HistoricUsageRecord(Host energyUser, List<HostEnergyRecord> data) {
        addEnergyUser(energyUser);
        if (data.size() > 2) {
            Collections.sort(data);
            HostEnergyRecord first = data.get(0);
            HostEnergyRecord last = data.get(data.size() - 1);
            totalEnergyUsed = last.getEnergy() - first.getEnergy();
            GregorianCalendar start = new GregorianCalendar();
            start.setTimeInMillis(first.getTime() * 1000);
            GregorianCalendar end = new GregorianCalendar();
            start.setTimeInMillis(last.getTime() * 1000);
            duration = new TimePeriod(start, end);
            avgPowerUsed = totalEnergyUsed / (last.getTime() - first.getTime());
        }
        if (data.size() == 1) {
            avgPowerUsed = data.get(0).getPower();
        }
    }

    /**
     * This creates an historic usage record and allows every value to be
     * specified during its construction.
     *
     * @param energyUser The source of the energy usage.
     * @param avgPowerUsed The average power used.
     * @param avgCurrentUsed The average current.
     * @param avgVoltageUsed The average voltage used.
     * @param totalEnergyUsed The total amount of energy used.
     */
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
