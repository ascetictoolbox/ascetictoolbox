/**
 *  Copyright 2014 University of Leeds
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package eu.ascetic.asceticarchitecture.iaas.energymodeller.types;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.EnergyUsageSource;

/**
 * An aggregation of energy usage values is needed. This will encompass 
 * all data record entries and also give energy used values.
 * i.e. Energy used (kWh) instead of just power (Watts in use (kW)).
 * @author Richard
 * @deprecated a first draft? Still needed?
 */
public class EnergyUsageSummary {
 
    private EnergyUsageSource energyUsageSource;

    public EnergyUsageSummary(EnergyUsageSource energyUsageSource) {
        this.energyUsageSource = energyUsageSource;
        //TODO complete here.
    }
    
    /**
     * The average amount of power a VM/Machine has used.
     * @return The amount of Watts used by a VM/Machine. Units W.
     */
    public double getAvgPower() {
        return 0.0; 
    }
    
    /**
     * The amount of energy used by a VM/Machine.
     * @return Energy used since boot/time immemorial/VM instantiation. Units (kWh). 
     */
    public double getEnergyUsed() {
        return 0.0;
    }     
    
    public double getAvgCurrent() {
        return 0.0;
    }
    
    public double getAvgVoltage() {
        return 0.0;
    }    

    /**
     * @return the energyUsageSource
     */
    public EnergyUsageSource getEnergyUsageSource() {
        return energyUsageSource;
    }

    /**
     * This allows the summary to be set for either a VM or a physical machine.
     * @param energyUsageSource the energyUsageSource to set
     */
    public void setEnergyUsageSource(EnergyUsageSource energyUsageSource) {
        this.energyUsageSource = energyUsageSource;
    }
    
    
    
}
