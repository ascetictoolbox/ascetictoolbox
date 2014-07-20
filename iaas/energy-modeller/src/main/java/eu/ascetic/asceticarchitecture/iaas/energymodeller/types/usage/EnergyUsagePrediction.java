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
 * This stores the result of a prediction that has been made regarding energy
 * usage.
 *
 * The main values store are:
 *
 * Avg Watts that is expected to use over time by the VM Predicted energy used
 * (kWh) during life of VM
 *
 * @author Richard
 */
public class EnergyUsagePrediction extends EnergyUsageRecord {

    private double avgPowerUsed; // units Watts
    private double totalEnergyUsed; //units kWh
    private TimePeriod timePeriod; // The time period to which the results correspond.

    public EnergyUsagePrediction(EnergyUsageSource energyUser) {
        addEnergyUser(energyUser);
    }

    public EnergyUsagePrediction(HashSet<EnergyUsageSource> energyUser) {
        addEnergyUser(energyUser);
    }

    public EnergyUsagePrediction(HashSet<EnergyUsageSource> energyUser, double avgPowerUsed, double totalEnergyUsed) {
        addEnergyUser(energyUser);
        this.avgPowerUsed = avgPowerUsed;
        this.totalEnergyUsed = totalEnergyUsed;
    }
    
    public EnergyUsagePrediction(){
    	
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
     * @return the predictionStartTime
     */
    public Calendar getPredictionStartTime() {
        return timePeriod.getStartTime();
    }

    /**
     * @return the predictionEndTime
     */
    public Calendar getPredictionEndTime() {
        return timePeriod.getEndTime();
    }

    /**
     * @return the duration
     */
    public TimePeriod getDuration() {
        return timePeriod;
    }

    /**
     * @param timePeriod the duration to set
     */
    public void setDuration(TimePeriod timePeriod) {
        this.timePeriod = timePeriod;
    }
    
}
