/**
 Copyright (C) 2013-2014  Barcelona Supercomputing Center

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package es.bsc.vmmanagercore.scheduler.schedulingalgorithms;

import es.bsc.vmmanagercore.logging.VMMLogger;
import es.bsc.vmmanagercore.models.scheduling.DeploymentPlan;
import es.bsc.vmmanagercore.models.scheduling.SchedAlgorithmNameEnum;
import es.bsc.vmmanagercore.models.scheduling.VmAssignmentToHost;
import es.bsc.vmmanagercore.models.vms.VmDeployed;
import es.bsc.vmmanagercore.monitoring.hosts.Host;

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
            if (vmDeployed.belongsToAnApp()) {
                appsInHosts.get(vmDeployed.getHostName()).add(vmDeployed.getApplicationId());
            }
        }

        // Classify VMs in deployment plan
        for (VmAssignmentToHost vmAssignmentToHost: deploymentPlan.getVmsAssignationsToHosts()) {
            if (!appsInHosts.containsKey(vmAssignmentToHost.getHost().getHostname())) {
                appsInHosts.put(vmAssignmentToHost.getHost().getHostname(), new ArrayList<String>());
            }
            if (vmAssignmentToHost.getVm().belongsToAnApp()) {
                appsInHosts.get(vmAssignmentToHost.getHost().getHostname()).add(
                        vmAssignmentToHost.getVm().getApplicationId());
            }
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
    public DeploymentPlan chooseBestDeploymentPlan(List<DeploymentPlan> deploymentPlans, List<Host> hosts,
            String deploymentId) {
        DeploymentPlan bestDeploymentPlan = null;
        int vmsFriendsForBestDeploymentPlan = -1;
        for (DeploymentPlan deploymentPlan: deploymentPlans) {
            int vmsFriendsForDeploymentPlan = countVmsFriendsForDeploymentPlan(deploymentPlan);
            VMMLogger.logVmsSameAppInSameHost(deploymentPlan, vmsFriendsForDeploymentPlan, deploymentId);
            if (vmsFriendsForDeploymentPlan > vmsFriendsForBestDeploymentPlan) {
                bestDeploymentPlan = deploymentPlan;
                vmsFriendsForBestDeploymentPlan = vmsFriendsForDeploymentPlan;
            }
            // If the score is the same, choose randomly
            else if (vmsFriendsForDeploymentPlan == vmsFriendsForBestDeploymentPlan) {
                if (Math.random() > 0.5) {
                    bestDeploymentPlan = deploymentPlan;
                }
            }
        }
        return bestDeploymentPlan;
    }

    @Override
    public SchedAlgorithmNameEnum getNameEnum() {
        return SchedAlgorithmNameEnum.GROUP_BY_APP;
    }

}
