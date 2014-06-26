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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.EnergyUsagePrediction;
import java.util.Collection;

/**
 * This is the standard interface for any energy predictor module to be loaded into
 * the ASCETiC architecture.
 * @author Richard
 */
public interface EnergyPredictorInterface {
    
    /**
     * This provides a prediction of how much energy is to be used by a host
     * @param host The host to get the energy prediction for
     * @param virtualMachines The virtual machines giving a workload on the host machine
     * @return 
     */
    public EnergyUsagePrediction getHostPredictedEnergy(Host host, Collection<VM> virtualMachines);
    
    /**
     * 
     * @param virtualMachines The virtual machines giving a workload on the host machine
     * @param host The host that the VMs will be running on
     * @return 
     */
    /**
     * This provides a prediction of how much energy is to be used by a VM
     * @param vm The vm to be deployed
     * @param virtualMachines The other Vms contributing workload to the host
     * @param host The host
     * @return 
     */
    public EnergyUsagePrediction getVMPredictedEnergy(VM vm, Collection<VM> virtualMachines, Host host);
    
}
