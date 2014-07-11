package es.bsc.vmmanagercore.scheduler.schedulingalgorithms;

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
    public boolean isBetterDeploymentPlan(DeploymentPlan deploymentPlan1, DeploymentPlan deploymentPlan2,
            List<Host> hosts) {
        return Math.random() >= 0.5;
    }

}
