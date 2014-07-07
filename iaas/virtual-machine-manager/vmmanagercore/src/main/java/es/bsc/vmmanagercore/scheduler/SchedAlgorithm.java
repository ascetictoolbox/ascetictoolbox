package es.bsc.vmmanagercore.scheduler;

import es.bsc.vmmanagercore.model.DeploymentPlan;
import es.bsc.vmmanagercore.monitoring.Host;

import java.util.List;

/**
 *  Interface for scheduling algorithms.
 *
 *  @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public interface SchedAlgorithm {

    /**
     * Checks whether a deployment plan is considered better than other deployment plan
     *
     * @param deploymentPlan1 first deployment plan
     * @param deploymentPlan2 second deployment plan
     * @param hosts the hosts in the infrastructure
     * @return true if deploymentPlan1 is better than deploymentPlan2, false otherwise
     */
    public boolean isBetterDeploymentPlan(DeploymentPlan deploymentPlan1, DeploymentPlan deploymentPlan2,
                                          List<Host> hosts);
}