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

package es.bsc.vmm.ascetic.scheduler.legacy;

import es.bsc.vmm.ascetic.modellers.price.ascetic.AsceticPricingModellerAdapter;
import es.bsc.demiurge.core.manager.components.EstimatesManager;
import es.bsc.demiurge.core.models.scheduling.DeploymentPlan;
import es.bsc.demiurge.core.models.scheduling.VmAssignmentToHost;
import es.bsc.demiurge.core.models.vms.Vm;
import es.bsc.demiurge.core.models.vms.VmDeployed;
import es.bsc.demiurge.core.monitoring.hosts.Host;
import es.bsc.demiurge.core.scheduler.schedulingalgorithms.SchedAlgorithm;
import org.apache.log4j.LogManager;

import java.util.List;

/**
 * Price-aware scheduling algorithm.
 * This scheduling algorithm chooses the hosts where the cost will be the lowest.
 * This decision is taken according to the predictions performed by the Pricing Modeller.
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
@Deprecated
public class SchedAlgCostAware implements SchedAlgorithm {



    private double getPredictedCostDeploymentPlan(EstimatesManager estimatesManager, DeploymentPlan deploymentPlan) {
		AsceticPricingModellerAdapter pricingModeller =
				(AsceticPricingModellerAdapter) estimatesManager.get(AsceticPricingModellerAdapter.class);
        double result = 0.0;
        for (VmAssignmentToHost vmAssignmentToHost: deploymentPlan.getVmsAssignationsToHosts()) {
            Vm vm = vmAssignmentToHost.getVm();
            Host host = vmAssignmentToHost.getHost();
            result += pricingModeller.getVMChargesPrediction(
                    vm.getCpus(), vm.getRamMb(), vm.getDiskGb(),
                    host.getHostname());
        }
        return result;
    }

    @Override
    public DeploymentPlan chooseBestDeploymentPlan(List<VmDeployed> vmsDeployed, EstimatesManager estimatorsManager, List<DeploymentPlan> deploymentPlans, List<Host> hosts,
            String deploymentId) {
        DeploymentPlan bestDeploymentPlan = null;
        double costBestDeploymentPlan = Double.MAX_VALUE;
        for (DeploymentPlan deploymentPlan: deploymentPlans) {
            double deploymentPlanCost = getPredictedCostDeploymentPlan(estimatorsManager, deploymentPlan);
            LogManager.getLogger(SchedAlgCostAware.class).debug("[VMM] predicted cost for deployment plan [ " + deploymentPlan.toString()
                    + "]: " + deploymentPlanCost + " euros --id:" + deploymentId);
            if (deploymentPlanCost < costBestDeploymentPlan) {
                bestDeploymentPlan = deploymentPlan;
                costBestDeploymentPlan = deploymentPlanCost;
            }
            // If the score is the same, choose randomly
            else if (deploymentPlanCost == costBestDeploymentPlan) {
                if (Math.random() > 0.5) {
                    bestDeploymentPlan = deploymentPlan;
                }
            }
        }
        return bestDeploymentPlan;
    }

    @Override
    public String getName() {
        return "costAware";
    }



}
