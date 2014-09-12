package es.bsc.vmmanagercore.scheduler.schedulingalgorithms;

import es.bsc.vmmanagercore.energymodeller.EnergyModellerConnector;
import es.bsc.vmmanagercore.logging.VMMLogger;
import es.bsc.vmmanagercore.model.DeploymentPlan;
import es.bsc.vmmanagercore.model.VmAssignmentToHost;
import es.bsc.vmmanagercore.model.VmDeployed;
import es.bsc.vmmanagercore.monitoring.Host;

import java.util.ArrayList;
import java.util.List;

/**
 * Energy-aware scheduling algorithm.
 * This scheduling algorithm chooses the host where the energy consumed will be the lowest.
 * This decision is taken according to the predictions performed by the Energy Modeller.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class SchedAlgEnergyAware implements SchedAlgorithm {

    List<VmDeployed> vmsDeployed = new ArrayList<>();

    public SchedAlgEnergyAware(List<VmDeployed> vmsDeployed) {
        this.vmsDeployed = vmsDeployed;
    }

    private double getPredictedAvgPowerDeploymentPlan(DeploymentPlan deploymentPlan) {
        double result = 0;
        for (VmAssignmentToHost vmAssignmentToHost: deploymentPlan.getVmsAssignationsToHosts()) {
            double predictedAvgPower = EnergyModellerConnector.getPredictedAvgPowerVm(vmAssignmentToHost.getVm(),
                    vmAssignmentToHost.getHost(), vmsDeployed);
            result += predictedAvgPower;
            VMMLogger.logPredictedAvgPowerOfVmInHost(vmAssignmentToHost.getVm().getName(),
                    vmAssignmentToHost.getHost().getHostname(), predictedAvgPower);
        }
        return result;
    }

    @Override
    public DeploymentPlan chooseBestDeploymentPlan(List<DeploymentPlan> deploymentPlans, List<Host> hosts) {
        DeploymentPlan bestDeploymentPlan = null;
        double avgPowerBestDeploymentPlan = Double.MAX_VALUE;
        for (DeploymentPlan deploymentPlan: deploymentPlans) {
            double predictedAvgPower = getPredictedAvgPowerDeploymentPlan(deploymentPlan);
            VMMLogger.logPredictedAvgPowerForDeploymentPlan(deploymentPlan, predictedAvgPower);
            if (predictedAvgPower < avgPowerBestDeploymentPlan) {
                bestDeploymentPlan = deploymentPlan;
                avgPowerBestDeploymentPlan = predictedAvgPower;
            }
            // If the score is the same, choose randomly
            else if (predictedAvgPower == avgPowerBestDeploymentPlan) {
                if (Math.random() > 0.5) {
                    bestDeploymentPlan = deploymentPlan;
                }
            }
        }
        return bestDeploymentPlan;
    }

}
