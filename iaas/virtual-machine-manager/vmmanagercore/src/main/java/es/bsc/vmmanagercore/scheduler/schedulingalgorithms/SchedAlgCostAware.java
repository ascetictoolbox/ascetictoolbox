package es.bsc.vmmanagercore.scheduler.schedulingalgorithms;

import es.bsc.vmmanagercore.energymodeller.EnergyModellerConnector;
import es.bsc.vmmanagercore.logging.VMMLogger;
import es.bsc.vmmanagercore.model.DeploymentPlan;
import es.bsc.vmmanagercore.model.VmAssignmentToHost;
import es.bsc.vmmanagercore.model.VmDeployed;
import es.bsc.vmmanagercore.monitoring.Host;
import es.bsc.vmmanagercore.pricingmodeller.PricingModellerConnector;

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

    List<VmDeployed> vmsDeployed = new ArrayList<>();

    public SchedAlgCostAware(List<VmDeployed> vmsDeployed) {
        this.vmsDeployed = vmsDeployed;
    }

    private double getPredictedCostDeploymentPlan(DeploymentPlan deploymentPlan) {
        double result = 0;
        for (VmAssignmentToHost vmAssignmentToHost: deploymentPlan.getVmsAssignationsToHosts()) {
            double energyEstimate = EnergyModellerConnector.getPredictedEnergyVm(vmAssignmentToHost.getVm(),
                    vmAssignmentToHost.getHost(), vmsDeployed);
            double costEstimate = PricingModellerConnector.getVmCost(energyEstimate,
                    vmAssignmentToHost.getHost().getHostname());
            result += costEstimate;
            VMMLogger.logPredictedCostOfVmInHost(vmAssignmentToHost.getVm().getName(),
                    vmAssignmentToHost.getHost().getHostname(), costEstimate);
        }
        return result;
    }

    @Override
    public DeploymentPlan chooseBestDeploymentPlan(List<DeploymentPlan> deploymentPlans, List<Host> hosts) {
        DeploymentPlan bestDeploymentPlan = null;
        double costBestDeploymentPlan = Double.MAX_VALUE;
        for (DeploymentPlan deploymentPlan: deploymentPlans) {
            double deploymentPlanCost = getPredictedCostDeploymentPlan(deploymentPlan);
            VMMLogger.logPredictedCostForDeploymentPlan(deploymentPlan, deploymentPlanCost);
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

}
