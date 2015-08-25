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

package es.bsc.vmmanagercore.modellers.energy.ascetic;

import es.bsc.vmmanagercore.models.scheduling.DeploymentPlan;
import es.bsc.vmmanagercore.models.vms.Vm;
import es.bsc.vmmanagercore.models.vms.VmDeployed;
import es.bsc.vmmanagercore.monitoring.hosts.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.EnergyModeller;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.EnergyUsagePrediction;

import java.util.ArrayList;
import java.util.List;

/**
 * Connector for the energy modeller developed in the Ascetic project by University of Leeds and AUEB.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class AsceticEnergyModellerAdapter implements es.bsc.vmmanagercore.modellers.energy.EnergyModeller {

    private static eu.ascetic.asceticarchitecture.iaas.energymodeller.EnergyModeller energyModeller =
            eu.ascetic.asceticarchitecture.iaas.energymodeller.EnergyModeller.getInstance();

    @Override
    public double getPredictedAvgPowerVm(Vm vm, Host host, List<VmDeployed> vmsDeployed,
            DeploymentPlan deploymentPlan) {
        return getEnergyUsagePrediction(vm, host, vmsDeployed, deploymentPlan).getAvgPowerUsed();
    }

    @Override
    public double getPredictedEnergyVm(Vm vm, Host host, List<VmDeployed> vmsDeployed,
                                       DeploymentPlan deploymentPlan) {
        return getEnergyUsagePrediction(vm, host, vmsDeployed, deploymentPlan).getTotalEnergyUsed();
    }

    @Override
    public double getHostPredictedAvgPower(String hostname, List<Vm> vms) {
        return energyModeller.getHostPredictedEnergy(
                energyModeller.getHost(hostname),
                VMMToEMConversor.getVmsEnergyModFromVms(vms))
                .getAvgPowerUsed();
    }

    public void initializeVmInEnergyModellerSystem(String vmId) {
        energyModeller.setVMProfileData(energyModeller.getVM(vmId));
    }

    public static EnergyModeller getEnergyModeller() {
        return energyModeller;
    }

    /**
     * Returns the energy usage predicted for a VM if it was deployed in a specific host.
     *
     * @param vm the VM
     * @param host the host
     * @param vmsDeployed the VMs already deployed in the host
     * @return the energy usage prediction
     */
    private static EnergyUsagePrediction getEnergyUsagePrediction(Vm vm, Host host, List<VmDeployed> vmsDeployed,
            DeploymentPlan deploymentPlan) {
        // We need to send to the Energy Modeller the list of VMs that are already deployed in the host plus the
        // list of VMs that would be deployed if the deploymentPlan was executed.
        // Note: This list also needs to include the VM that we want to deploy, because the EM expects it.
        List<VM> vmsInHost = VMMToEMConversor.getVmsEnergyModFromVms(
                getVmsDeployedInHost(host.getHostname(), vmsDeployed)); // VMs already deployed

        // Add the VMs that would be deployed if the deployment plan was executed
        for (Vm vmInHost: deploymentPlan.getVmsAssignedToHost(host.getHostname())) {
            vmsInHost.add(EnergyModeller.getVM(vmInHost.getCpus(), vmInHost.getRamMb(), vmInHost.getDiskGb()));
        }

        return energyModeller.getPredictedEnergyForVM(
                eu.ascetic.asceticarchitecture.iaas.energymodeller.EnergyModeller
                        .getVM(vm.getCpus(), vm.getRamMb(), vm.getDiskGb()), vmsInHost,
                energyModeller.getHost(host.getHostname()));
    }

    /**
     * Returns the VMs deployed in a given host.
     *
     * @param hostname the host name
     * @return the VMs deployed in the host
     */
    private static List<Vm> getVmsDeployedInHost(String hostname, List<VmDeployed> vmsDeployed) {
        List<Vm> vms = new ArrayList<>();
        for (VmDeployed vm: vmsDeployed) {
            if (vm.getHostName().equals(hostname)) {
                vms.add(vm);
            }
        }
        return vms;
    }

}
