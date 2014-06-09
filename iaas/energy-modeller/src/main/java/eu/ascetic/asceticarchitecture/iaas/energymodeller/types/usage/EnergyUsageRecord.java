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

import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.EnergyUsageSource;
import java.util.Collection;
import java.util.HashSet;

/**
 *
 * @author Richard
 */
public abstract class EnergyUsageRecord {

    private HashSet<EnergyUsageSource> energyUser = new HashSet<>();    

    /**
     * @return the energyUser
     */
    public HashSet<EnergyUsageSource> getEnergyUser() {
        return energyUser;
    }
    
    protected void addEnergyUser(EnergyUsageSource EnergyUser) {
        energyUser.add(EnergyUser);
    }        
    
    protected void addEnergyUser(Collection<EnergyUsageSource> EnergyUsers) {
        energyUser.addAll(EnergyUsers);
    }    
    
    /**
     * Indicates if this record details energy usage for a given user.
     * @param energyUser The energy user.
     * @return 
     */
    public boolean containsEnergyUser(EnergyUsageSource energyUser) {
        return this.energyUser.contains(energyUser);
    }    
    
}
