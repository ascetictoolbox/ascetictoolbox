package es.bsc.vmmanagercore.scheduler.schedulingalgorithms;

import es.bsc.vmmanagercore.logging.VMMLogger;
import es.bsc.vmmanagercore.model.DeploymentPlan;
import es.bsc.vmmanagercore.model.VmAssignmentToHost;
import es.bsc.vmmanagercore.model.VmDeployed;
import es.bsc.vmmanagercore.monitoring.Host;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Scheduling algorithm that groups VMs by the application which they belong to.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class SchedAlgGroupByApp implements SchedAlgorithm {

    private List<VmDeployed> vmsDeployed;

    public SchedAlgGroupByApp(List<VmDeployed> vmsDeployed) {
        this.vmsDeployed = vmsDeployed;
    }

    /**
     * Calculates the total number of "friends" in a deployment friend.
     * In this context, two VMs are considered to be friends if they belong to the same app and are deployed
     * in the same host. For example, in a cluster where host1 = {app1, app1, app2} and host2 = {app1, app2}, the
     * result would be 2, because each of the VMs that belong to app1 in host1 has one friend and all the rest
     * have 0.
     *
     * @param deploymentPlan the deployment plan
     * @return the total number of friends in the deployment plan
     */
    private int countVmsFriendsForDeploymentPlan(DeploymentPlan deploymentPlan) {
        HashMap<String, List<String>> appsInHosts = new HashMap<>(); // hostname -> list of app IDs

        // Classify VMs already deployed
        for (VmDeployed vmDeployed: vmsDeployed) {
            if (!appsInHosts.containsKey(vmDeployed.getHostName())) {
                appsInHosts.put(vmDeployed.getHostName(), new ArrayList<String>());
            }
            appsInHosts.get(vmDeployed.getHostName()).add(vmDeployed.getApplicationId());
        }

        // Classify VMs in deployment plan
        for (VmAssignmentToHost vmAssignmentToHost: deploymentPlan.getVmsAssignationsToHosts()) {
            if (!appsInHosts.containsKey(vmAssignmentToHost.getHost().getHostname())) {
                appsInHosts.put(vmAssignmentToHost.getHost().getHostname(), new ArrayList<String>());
            }
            appsInHosts.get(vmAssignmentToHost.getHost().getHostname()).add(
                    vmAssignmentToHost.getVm().getApplicationId());
        }

        // Count total number of "friends"
        int result = 0;
        for (Map.Entry<String, List<String>> entry: appsInHosts.entrySet()) {
            List<String> appsInHost = entry.getValue();
            for (int i = 0; i < appsInHost.size(); ++i) {
                int friends = 0;
                for (int j = 0; j < appsInHost.size(); ++j) {
                    if (i != j && appsInHost.get(i).equals(appsInHost.get(j))) {
                        ++friends;
                    }
                }
                result += friends;
            }
        }
        return result;
    }

    @Override
    public boolean isBetterDeploymentPlan(DeploymentPlan deploymentPlan1, DeploymentPlan deploymentPlan2,
            List<Host> hosts) {
        int vmsSameAppInSameHostPlan1 = countVmsFriendsForDeploymentPlan(deploymentPlan1);
        int vmsSameAppInSameHostPlan2 = countVmsFriendsForDeploymentPlan(deploymentPlan2);
        VMMLogger.logVmsSameAppInSameHost(1, vmsSameAppInSameHostPlan1);
        VMMLogger.logVmsSameAppInSameHost(2, vmsSameAppInSameHostPlan2);
        return vmsSameAppInSameHostPlan1 >= vmsSameAppInSameHostPlan2;
    }
}
