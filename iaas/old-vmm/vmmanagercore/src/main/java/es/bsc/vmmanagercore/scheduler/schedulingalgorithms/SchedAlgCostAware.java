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

package es.bsc.vmmanagercore.scheduler.schedulingalgorithms;

import es.bsc.vmmanagercore.logging.VMMLogger;
import es.bsc.vmmanagercore.modellers.energy.EnergyModeller;
import es.bsc.vmmanagercore.modellers.price.PricingModeller;
import es.bsc.vmmanagercore.models.scheduling.DeploymentPlan;
import es.bsc.vmmanagercore.models.scheduling.SchedAlgorithmNameEnum;
import es.bsc.vmmanagercore.models.scheduling.VmAssignmentToHost;
import es.bsc.vmmanagercore.models.vms.Vm;
import es.bsc.vmmanagercore.models.vms.VmDeployed;
import es.bsc.vmmanagercore.monitoring.hosts.Host;

import java.util.ArrayList;
import java.util.List;

/**
 * Price-aware scheduling algorithm.
 * This scheduling algorithm chooses the hosts where the cost will be the lowest.
 * This decision is taken according to the predictions performed by the Pricing Modeller.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class SchedAlgCostAware implements SchedAlgorithm {

    private List<VmDeployed> vmsDeployed = new ArrayList<>();
    private PricingModeller pricingModeller;
    private EnergyModeller energyModeller;

    public SchedAlgCostAware(List<VmDeployed> vmsDeployed, PricingModeller pricingModeller,
                             EnergyModeller energyModeller) {
        this.vmsDeployed = vmsDeployed;
        this.pricingModeller = pricingModeller;
        this.energyModeller = energyModeller;
    }

    private double getPredictedCostDeploymentPlan(DeploymentPlan deploymentPlan) {
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
    public DeploymentPlan chooseBestDeploymentPlan(List<DeploymentPlan> deploymentPlans, List<Host> hosts,
            String deploymentId) {
        DeploymentPlan bestDeploymentPlan = null;
        double costBestDeploymentPlan = Double.MAX_VALUE;
        for (DeploymentPlan deploymentPlan: deploymentPlans) {
            double deploymentPlanCost = getPredictedCostDeploymentPlan(deploymentPlan);
            VMMLogger.logPredictedCostForDeploymentPlan(deploymentPlan, deploymentPlanCost, deploymentId);
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
    public SchedAlgorithmNameEnum getNameEnum() {
        return SchedAlgorithmNameEnum.COST_AWARE;
    }

}
