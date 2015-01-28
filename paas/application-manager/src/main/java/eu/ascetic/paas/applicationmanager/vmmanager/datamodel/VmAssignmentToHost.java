package eu.ascetic.paas.applicationmanager.vmmanager.datamodel;

import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.monitoring.Host;

/**
 * 
 * Copyright 2014 ATOS SPAIN S.A. 
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author: David Rojo. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.rojoa@atos.net 
 *
 * VM placement to a Host.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class VmAssignmentToHost {

    private Vm vm;
    private Host host;

    /**
     * Class constructor.
     *
     * @param vm the VM
     * @param host the host
     */
    public VmAssignmentToHost(Vm vm, Host host) {
        this.vm = vm;
        this.host = host;
    }

    public Vm getVm() {
        return vm;
    }

    public Host getHost() {
        return host;
    }

//    public VmEstimate getVmEstimate(List<VmDeployed> vmsDeployed) {
//        return new VmEstimate(vm.getName(), getPowerEstimate(vmsDeployed), getPriceEstimate(vmsDeployed));
//    }
//
//    /**
//     * Returns the predicted avg power of the placement.
//     *
//     * @param vmsDeployed VMs deployed in the infrastructure
//     * @return the predicted avg power
//     */
//    private double getPowerEstimate(List<VmDeployed> vmsDeployed) {
//        return EnergyModellerConnector.getPredictedAvgPowerVm(vm, host, vmsDeployed);
//    }
//
//    /**
//     * Returns the predicted energy estimate of the placement.
//     *
//     * @param vmsDeployed VMs deployed in the infrastructure
//     * @return the predicted energy
//     */
//    private double getEnergyEstimate(List<VmDeployed> vmsDeployed) {
//        return EnergyModellerConnector.getPredictedEnergyVm(vm, host, vmsDeployed);
//    }
//
//    /**
//     * Returns the predicted price of the placement.
//     *
//     * @param vmsDeployed VMs deployed in the infrastructure
//     * @return the predicted price
//     */
//    private double getPriceEstimate(List<VmDeployed> vmsDeployed) {
//        return PricingModellerConnector.getVmCost(getEnergyEstimate(vmsDeployed), host.getHostname());
//    }

}
