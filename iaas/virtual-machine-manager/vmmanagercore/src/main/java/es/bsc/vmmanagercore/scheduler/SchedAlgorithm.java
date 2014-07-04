package es.bsc.vmmanagercore.scheduler;

import es.bsc.vmmanagercore.model.DeploymentPlan;
import es.bsc.vmmanagercore.model.Vm;
import es.bsc.vmmanagercore.monitoring.HostInfo;

import java.util.List;

/**
 *  Interface for scheduling algorithms.
 *
 *  @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public interface SchedAlgorithm {

    /**
     * Decides on which host deploy a VM according to its CPU, memory and disk requirements.
     *
     * @param hostsInfo Information of the hosts of the infrastructure
     * @param vm VM that needs to be deployed
     * @return The name of the host on which the VM should be deployed.
     */
    public String chooseHost(List<HostInfo> hostsInfo, Vm vm);

    /**
     * Checks whether a deployment plan is considered better than other deployment plan
     *
     * @param deploymentPlan1 first deployment plan
     * @param deploymentPlan2 second deployment plan
     * @param hosts the hosts in the infrastructure
     * @return true if deploymentPlan1 is better than deploymentPlan2, false otherwise
     */
    public boolean isBetterDeploymentPlan(DeploymentPlan deploymentPlan1, DeploymentPlan deploymentPlan2,
                                          List<HostInfo> hosts);
}