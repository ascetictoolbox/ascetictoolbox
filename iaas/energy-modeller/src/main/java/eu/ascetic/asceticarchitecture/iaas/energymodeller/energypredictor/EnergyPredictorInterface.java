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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.TimePeriod;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.EnergyUsagePrediction;
import java.util.Collection;

/**
 * This is the standard interface for any energy predictor module to be loaded
 * into the ASCETiC architecture.
 *
 * @author Richard Kavanagh
 */
public interface EnergyPredictorInterface {

    /**
     * This provides a prediction of how much energy is to be used by a host
     * in the next hour.
     *
     * @param host The host to get the energy prediction for
     * @param virtualMachines The virtual machines giving a workload on the host
     * machine
     * @return The prediction of the energy to be used.
     */
    public EnergyUsagePrediction getHostPredictedEnergy(Host host, Collection<VM> virtualMachines);

    /**
     * This provides a prediction of how much energy is to be used by a host
     * in a specified period of time.
     *
     * @param host The host to get the energy prediction for
     * @param virtualMachines The virtual machines giving a workload on the host
     * machine
     * @param timePeriod The time period to run the prediction for
     * @return The prediction of the energy to be used.
     */
    public EnergyUsagePrediction getHostPredictedEnergy(Host host, Collection<VM> virtualMachines, TimePeriod timePeriod);    
    
    /**
     * This provides a prediction of how much energy is to be used by a VM in 
     * the next hour.
     *
     * @param vm The vm to be deployed
     * @param virtualMachines The virtual machines that are expected to be on
     * the physical host that therefore induce a workload on the host
     * @param host The host that the VMs will be running on
     * @return The prediction of the energy to be used.
     */
    public EnergyUsagePrediction getVMPredictedEnergy(VM vm, Collection<VM> virtualMachines, Host host);

    /**
     * This provides a prediction of how much energy is to be used by a VM, in 
     * a specified period of time.
     *
     * @param vm The vm to be deployed
     * @param virtualMachines The virtual machines giving a workload on the host
     * machine
     * @param host The host that the VMs will be running on
     * @param timePeriod The time period to run the prediction for
     * @return The prediction of the energy to be used.
     */
    public EnergyUsagePrediction getVMPredictedEnergy(VM vm, Collection<VM> virtualMachines, Host host, TimePeriod timePeriod);

    /**
     * This determines how good the fit of the model is in regards to a particular named host 
     * @param host The host that the energy predictions are for
     * @return The sum of the square error
     */
    public double getSumOfSquareError(Host host);
}
