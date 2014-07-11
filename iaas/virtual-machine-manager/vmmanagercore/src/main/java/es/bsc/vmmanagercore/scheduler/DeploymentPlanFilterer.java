package es.bsc.vmmanagercore.scheduler;

import es.bsc.vmmanagercore.model.DeploymentPlan;
import es.bsc.vmmanagercore.model.Vm;
import es.bsc.vmmanagercore.model.VmAssignmentToHost;
import es.bsc.vmmanagercore.monitoring.Host;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Deployment plan filterer. Rejects the deployment plans that cannot be applied, that is, the deployment
 * plans where there are hosts that have been assigned VMs that need more resources than the resources
 * that are available in the host.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class DeploymentPlanFilterer {

    /**
     * From a list of deployment plans, returns only the ones that can be applied.
     * A deployment plan contains for each VM the host where it should be deployed.
     * In this context, we consider that a deployment plan can be applied if its hosts
     * contain enough resources to host the VMs that they have been assigned.
     *
     * @param deploymentPlans the deployment plans to filter
     * @return the filtered list of deployment plans
     */
    public static List<DeploymentPlan> filterDeploymentPlans(List<DeploymentPlan> deploymentPlans) {
        List<DeploymentPlan> result = new ArrayList<>();
        for (DeploymentPlan deploymentPlan: deploymentPlans) {
            if (deploymentPlanCanBeApplied(deploymentPlan)) {
                result.add(deploymentPlan);
            }
        }
        return result;
    }

    /**
     * Checks whether the hosts of the deployment plan have enough resources to host the VMs that they have
     * been assigned.
     *
     * @param deploymentPlan the deployment plan
     * @return true if the deployment plan can be applied, false otherwise
     */
    private static boolean deploymentPlanCanBeApplied(DeploymentPlan deploymentPlan) {
        // Build a map that contains for each host, the VMs that are going to be deployed in it
        Map<Host, List<Vm>> vmsOfEachHost = new HashMap<>();
        for (VmAssignmentToHost vmAssignmentToHost: deploymentPlan.getVmsAssignationsToHosts()) {
            Host host = vmAssignmentToHost.getHost();
            if (!vmsOfEachHost.containsKey(host)) {
                vmsOfEachHost.put(host, new ArrayList<Vm>());
            }
            vmsOfEachHost.get(host).add(vmAssignmentToHost.getVm());
        }

        // For each of the host, check whether it has enough resources to host all the VMs that it
        // has been assigned
        for (Map.Entry<Host, List<Vm>> entry : vmsOfEachHost.entrySet()) {
            if (!entry.getKey().hasEnoughResourcesToDeployVms(entry.getValue())) {
                return false;
            }
        }
        return true;
    }

}
