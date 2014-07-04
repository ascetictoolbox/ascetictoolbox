package es.bsc.vmmanagercore.scheduler;

import es.bsc.vmmanagercore.model.DeploymentPlan;
import es.bsc.vmmanagercore.model.Vm;
import es.bsc.vmmanagercore.monitoring.HostInfo;

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
    public String chooseHost(List<HostInfo> hostsInfo, Vm vm) {
        int randomHostIndex = (int)(Math.random()*hostsInfo.size());
        return hostsInfo.get(randomHostIndex).getHostname();
    }

    @Override
    public boolean isBetterDeploymentPlan(DeploymentPlan deploymentPlan1, DeploymentPlan deploymentPlan2,
            List<HostInfo> hosts) {
        return false;
    }

}
