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

package es.bsc.vmmanagercore.scheduler;

import es.bsc.vmmanagercore.energymodeller.EnergyModeller;
import es.bsc.vmmanagercore.model.estimations.ListVmEstimates;
import es.bsc.vmmanagercore.model.estimations.VmEstimate;
import es.bsc.vmmanagercore.model.scheduling.DeploymentPlan;
import es.bsc.vmmanagercore.model.scheduling.VmAssignmentToHost;
import es.bsc.vmmanagercore.model.vms.VmDeployed;
import es.bsc.vmmanagercore.pricingmodeller.PricingModeller;

import java.util.ArrayList;
import java.util.List;

/**
 *
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class EstimatesGenerator {

    /**
     * Returns price and energy estimates for each VM in a deployment plan.
     *
     * @param deploymentPlan the deployment plan
     * @param vmsDeployed VMs deployed in the infrastructure
     * @return the price and energy estimates for each VM
     */
    public ListVmEstimates getVmEstimates(DeploymentPlan deploymentPlan, List<VmDeployed> vmsDeployed,
                                          EnergyModeller energyModeller, PricingModeller pricingModeller) {
        List<VmEstimate> vmEstimates = new ArrayList<>();
        for (VmAssignmentToHost vmAssignmentToHost: deploymentPlan.getVmsAssignationsToHosts()) {
            vmEstimates.add(vmAssignmentToHost.getVmEstimate(vmsDeployed, deploymentPlan,
                    energyModeller, pricingModeller));
        }
        return new ListVmEstimates(vmEstimates);
    }

}
