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

import es.bsc.vmmanagercore.model.scheduling.DeploymentPlan;
import es.bsc.vmmanagercore.model.vms.Vm;
import es.bsc.vmmanagercore.model.vms.VmDeployed;
import es.bsc.vmmanagercore.monitoring.hosts.Host;

import java.util.List;

/**
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public interface EnergyModeller {

    /**
     * Returns the predicted avg. power for a VM if it was deployed in a specific host.
     *
     * @param vm the VM
     * @param host the host
     * @param vmsDeployed the VMs already deployed in the host
     * @param deploymentPlan where it is defined that the vm has been assigned to the host
     * @return the predicted avg. power in Watts
     */
    double getPredictedAvgPowerVm(Vm vm, Host host, List<VmDeployed> vmsDeployed, DeploymentPlan deploymentPlan);

    /**
     * Returns the predicted energy that will be consumed by a VM if it was deployed in a specific host.
     *
     * @param vm the VM
     * @param host the host
     * @param vmsDeployed the VMs already deployed in the host
     * @return the predicted energy in Joules
     */
    double getPredictedEnergyVm(Vm vm, Host host, List<VmDeployed> vmsDeployed, DeploymentPlan deploymentPlan);


    /**
     * Returns the average power to be consumed by a given host.
     *
     * @param hostname The name of the host that the prediction is for
     * @param vms The VMs that are deployed or expected to be deployed in the host.
     * @return the predicted avg. power in Watts
     */
    double getHostPredictedAvgPower(String hostname, List<Vm> vms);

}
