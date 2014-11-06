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
import java.util.Objects;

/**
 * This stores the result of a prediction that has been made regarding energy
 * usage.
 *
 * The main values store are:
 *
 * Avg Watts that is expected to use over time by the VM Predicted energy used
 * (kWh) during life of VM or during the duration of a specified period
 *
 * @author Richard
 */
public class EnergyUsagePrediction extends EnergyUsageRecord implements Comparable<EnergyUsagePrediction> {

    private double avgPowerUsed; // units Watts
    private double totalEnergyUsed; //units kWh
    private TimePeriod timePeriod; // The time period to which the results correspond.

    /**
     * This creates a blank energy prediction record.
     */
    public EnergyUsagePrediction() {
    }

    /**
     * This creates a energy prediction record for a given energy user.
     *
     * @param energyUser The energy user, either a VM, a host or a VM -> host
     * mapping
     */
    public EnergyUsagePrediction(EnergyUsageSource energyUser) {
        addEnergyUser(energyUser);
    }

    /**
     * This creates a energy prediction record for a set of energy users.
     *
     * @param energyUser The energy users, either a VM, a host or a VM -> host
     * mapping
     */
    public EnergyUsagePrediction(HashSet<EnergyUsageSource> energyUser) {
        addEnergyUser(energyUser);
    }

    /**
     * This creates a energy prediction record for a set of energy users.
     *
     * @param energyUser The energy user, either a VM, a host or a VM -> host
     * mapping
     * @param avgPowerUsed The average power used
     * @param totalEnergyUsed The total energy used
     */
    public EnergyUsagePrediction(HashSet<EnergyUsageSource> energyUser, double avgPowerUsed, double totalEnergyUsed) {
        addEnergyUser(energyUser);
        this.avgPowerUsed = avgPowerUsed;
        this.totalEnergyUsed = totalEnergyUsed;
    }

    /**
     * This provides the average power used.
     *
     * @return The average power used.
     */
    public double getAvgPowerUsed() {
        return avgPowerUsed;
    }

    /**
     * This sets the average power used.
     *
     * @param avgPowerUsed The average power used.
     */
    public void setAvgPowerUsed(double avgPowerUsed) {
        this.avgPowerUsed = avgPowerUsed;
    }

    /**
     * This provides the total energy used.
     *
     * @return the total energy used.
     */
    public double getTotalEnergyUsed() {
        return totalEnergyUsed;
    }

    /**
     * This sets the total energy used.
     *
     * @param totalEnergyUsed the new value for the total energy used.
     */
    public void setTotalEnergyUsed(double totalEnergyUsed) {
        this.totalEnergyUsed = totalEnergyUsed;
    }

    /**
     * This provides the predicted start time for this energy prediction.
     *
     * @return the time this prediction takes effect.
     */
    public Calendar getPredictionStartTime() {
        return timePeriod.getStartTime();
    }

    /**
     * This provides the predicted end time for this energy prediction.
     *
     * @return the time this prediction finishes.
     */
    public Calendar getPredictionEndTime() {
        return timePeriod.getEndTime();
    }

    /**
     * This provides the duration of time this prediction represents.
     *
     * @return The duration of time this prediction represents.
     */
    public TimePeriod getDuration() {
        return timePeriod;
    }

    /**
     * This sets the duration of time this prediction represents.
     *
     * @param timePeriod The duration of time this prediction represents.
     */
    public void setDuration(TimePeriod timePeriod) {
        this.timePeriod = timePeriod;
    }

    @Override
    /**
     * An Energy usage prediction is considered to be equal if the average power
     * used and the total energy used is equal. The equality ignores the 
     * respective time period.
     */
    public boolean equals(Object obj) {
        if (obj instanceof EnergyUsagePrediction) {
            EnergyUsagePrediction energyPrediction = (EnergyUsagePrediction) obj;
            return this.avgPowerUsed == energyPrediction.getAvgPowerUsed()&& 
                    this.totalEnergyUsed == energyPrediction.getTotalEnergyUsed() &&
                    this.timePeriod.equals(energyPrediction.getDuration());
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + (int) (Double.doubleToLongBits(this.avgPowerUsed) ^ (Double.doubleToLongBits(this.avgPowerUsed) >>> 32));
        hash = 83 * hash + (int) (Double.doubleToLongBits(this.totalEnergyUsed) ^ (Double.doubleToLongBits(this.totalEnergyUsed) >>> 32));
        hash = 83 * hash + Objects.hashCode(this.timePeriod);
        return hash;
    }

    /**
     * This compares two energy prediction values and places them in the natural
     * ordering of power consumption for each of the predictions. The ordering
     * produced by this comparator is ascending.
     * @param anotherPrediction The prediction to compare to
     * @return The value 0 if anotherPrediction's power value is numerically 
     * equal to this energy usage records power value; a value less than 0 
     * if this energy usage records power value is numerically less than 
     * anotherPrediction's power value; and a value greater than 0 if this 
     * energy prediction's power value is numerically greater than 
     * anotherPrediction's power value.
     */
    @Override
    public int compareTo(EnergyUsagePrediction anotherPrediction) {
        return Double.valueOf(avgPowerUsed).compareTo(anotherPrediction.avgPowerUsed);
    }
    
}
