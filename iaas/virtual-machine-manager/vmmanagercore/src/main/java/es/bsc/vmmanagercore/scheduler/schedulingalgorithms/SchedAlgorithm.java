package es.bsc.vmmanagercore.scheduler.schedulingalgorithms;

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
     * Given a list of deployment plans, chooses the best according to the scheduling algorithm.
     *
     * @param deploymentPlans the list of deployment plans
     * @param hosts the list of hosts
     * @return the best deployment plan
     */
    public DeploymentPlan chooseBestDeploymentPlan(List<DeploymentPlan> deploymentPlans, List<Host> hosts);

}