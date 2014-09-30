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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.vmenergyshare;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM;
import java.util.Collection;

/**
 * This allocates energy used by a host machine into the VMs that run upon it.
 * @author Richard
 */
public interface EnergyShareRule {
    
    /**
     * Translates a hosts energy usage into the VMs energy usage. This method
     * generates the fractions by which to allocate energy, to each VM.
     * @param host The host to analyse
     * @param vms The VMs that are on/to be on the host
     * @return The fraction of energy used per host.
     */
    public EnergyDivision getEnergyUsage(Host host, Collection<VM> vms);
    
}
