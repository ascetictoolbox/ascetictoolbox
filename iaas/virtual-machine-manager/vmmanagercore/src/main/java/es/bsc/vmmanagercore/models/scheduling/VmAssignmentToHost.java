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

package es.bsc.vmmanagercore.models.scheduling;

import es.bsc.vmmanagercore.modellers.energy.EnergyModeller;
import es.bsc.vmmanagercore.modellers.price.PricingModeller;
import es.bsc.vmmanagercore.models.estimates.VmEstimate;
import es.bsc.vmmanagercore.models.vms.Vm;
import es.bsc.vmmanagercore.models.vms.VmDeployed;
import es.bsc.vmmanagercore.monitoring.hosts.Host;

import java.util.List;

/**
 * VM placement to a Host.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class VmAssignmentToHost {

    private final Vm vm;
    private final Host host;

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
                                    EnergyModeller energyModeller, PricingModeller pricingModeller) {
        return new VmEstimate(
                vm.getName(),
                getPowerEstimate(vmsDeployed, deploymentPlan, energyModeller),
                getPriceEstimate(pricingModeller));
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
     * Returns the predicted price of the placement.
     *
     * @param pricingModeller the Pricing Modeller responsible for calculating the price
     * @return the predicted price
     */
    private double getPriceEstimate(PricingModeller pricingModeller) {
        return pricingModeller.getVmCost(vm.getCpus(), vm.getRamMb(), vm.getDiskGb(), host.getHostname());
    }

    @Override
    public String toString() {
        return getVm().getName() + "-->" + getHost().getHostname();
    }

}
