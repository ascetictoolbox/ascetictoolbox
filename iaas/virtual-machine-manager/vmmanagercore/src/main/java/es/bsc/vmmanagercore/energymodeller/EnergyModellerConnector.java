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

package es.bsc.vmmanagercore.energymodeller;

import es.bsc.vmmanagercore.model.Vm;
import es.bsc.vmmanagercore.model.VmDeployed;
import es.bsc.vmmanagercore.monitoring.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.EnergyModeller;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.EnergyUsagePrediction;

import java.util.ArrayList;
import java.util.List;

/**
 * Connector for the energy modeller developed by University of Leeds and AUEB.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class EnergyModellerConnector {

    private static EnergyModeller energyModeller = new EnergyModeller();

    /**
     * Returns the predicted avg. power for a VM if it was deployed in a specific host.
     *
     * @param vm the VM
     * @param host the host
     * @param vmsDeployed the VMs already deployed in the host
     * @return the predicted avg. power in Watts
     */
    public static double getPredictedAvgPowerVm(Vm vm, Host host, List<VmDeployed> vmsDeployed) {
        return getEnergyUsagePrediction(vm, host, vmsDeployed).getAvgPowerUsed();
    }

    /**
     * Returns the predicted energy that will be consumed by a VM if it was deployed in a specific host.
     *
     * @param vm the VM
     * @param host the host
     * @param vmsDeployed the VMs already deployed in the host
     * @return the predicted energy in Joules
     */
    public static double getPredictedEnergyVm(Vm vm, Host host, List<VmDeployed> vmsDeployed) {
        return getEnergyUsagePrediction(vm, host, vmsDeployed).getTotalEnergyUsed();
    }

    /**
     * Returns the energy usage predicted for a VM if it was deployed in a specific host.
     *
     * @param vm the VM
     * @param host the host
     * @param vmsDeployed the VMs already deployed in the host
     * @return the energy usage prediction
     */
    private static EnergyUsagePrediction getEnergyUsagePrediction(Vm vm, Host host, List<VmDeployed> vmsDeployed) {
        return energyModeller.getPredictedEnergyForVM(
                EnergyModeller.getVM(vm.getCpus(), vm.getRamMb(), vm.getDiskGb()),
                VMMToEMConversor.getVmsEnergyModFromVms(getVmsDeployedInHost(host.getHostname(), vmsDeployed)),
                energyModeller.getHost(host.getHostname()));
    }

    /**
     * Returns the VMs deployed in a given host.
     *
     * @param hostname the host name
     * @return the VMs deployed in the host
     */
    //TODO Right now this is returning only the VMs managed by the VM Manager. Maybe it should return all
    //so the energy modeller can function correctly.
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
