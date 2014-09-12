package es.bsc.vmmanagercore.scheduler.schedulingalgorithms;

import es.bsc.vmmanagercore.logging.VMMLogger;
import es.bsc.vmmanagercore.model.DeploymentPlan;
import es.bsc.vmmanagercore.monitoring.Host;

import java.util.List;

/**
 * Scheduling algorithms that places VMs at random hosts.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class SchedAlgRandom implements SchedAlgorithm {

    public SchedAlgRandom() { }

    @Override
    public DeploymentPlan chooseBestDeploymentPlan(List<DeploymentPlan> deploymentPlans, List<Host> hosts) {
        DeploymentPlan bestDeploymentPlan = null;
        double bestDeploymentPlanScore = -1;
        for (DeploymentPlan deploymentPlan: deploymentPlans) {
            double deploymentPlanScore = Math.random();
            VMMLogger.logDeploymentPlanRandomScore(deploymentPlan, deploymentPlanScore);
            if (deploymentPlanScore > bestDeploymentPlanScore) {
                bestDeploymentPlan = deploymentPlan;
                bestDeploymentPlanScore = deploymentPlanScore;
            }
        }
        return bestDeploymentPlan;
    }

}
