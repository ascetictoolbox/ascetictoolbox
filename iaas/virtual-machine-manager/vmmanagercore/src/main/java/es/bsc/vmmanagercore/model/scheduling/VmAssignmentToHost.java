/**
 Copyright (C) 2013-2014  Barcelona Supercomputing Center

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package es.bsc.vmmanagercore.model.scheduling;

import es.bsc.vmmanagercore.energymodeller.EnergyModeller;
import es.bsc.vmmanagercore.energymodeller.ascetic.AsceticEnergyModellerAdapter;
import es.bsc.vmmanagercore.model.estimations.VmEstimate;
import es.bsc.vmmanagercore.model.vms.Vm;
import es.bsc.vmmanagercore.model.vms.VmDeployed;
import es.bsc.vmmanagercore.monitoring.Host;
import es.bsc.vmmanagercore.pricingmodeller.PricingModellerConnector;

import java.util.List;

/**
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

    public VmEstimate getVmEstimate(List<VmDeployed> vmsDeployed, DeploymentPlan deploymentPlan,
                                    EnergyModeller energyModeller) {
        return new VmEstimate(
                vm.getName(),
                getPowerEstimate(vmsDeployed, deploymentPlan, energyModeller),
                getPriceEstimate(vmsDeployed, deploymentPlan));
    }

    /**
     * Returns the predicted avg power of the placement.
     *
     * @param vmsDeployed VMs deployed in the infrastructure
     * @return the predicted avg power
     */
    private double getPowerEstimate(List<VmDeployed> vmsDeployed, DeploymentPlan deploymentPlan,
                                    EnergyModeller energyModeller) {
        return energyModeller.getPredictedAvgPowerVm(vm, host, vmsDeployed, deploymentPlan);
    }

    /**
     * Returns the predicted energy estimate of the placement.
     *
     * @param vmsDeployed VMs deployed in the infrastructure
     * @return the predicted energy
     */
    private double getEnergyEstimate(List<VmDeployed> vmsDeployed, DeploymentPlan deploymentPlan) {
        return AsceticEnergyModellerAdapter.getPredictedEnergyVm(vm, host, vmsDeployed, deploymentPlan);
    }

    /**
     * Returns the predicted price of the placement.
     *
     * @param vmsDeployed VMs deployed in the infrastructure
     * @return the predicted price
     */
    private double getPriceEstimate(List<VmDeployed> vmsDeployed, DeploymentPlan deploymentPlan) {
        return PricingModellerConnector.getVmCost(getEnergyEstimate(vmsDeployed, deploymentPlan), host.getHostname());
    }

    @Override
    public String toString() {
        return getVm().getName() + "-->" + getHost().getHostname();
    }

}
