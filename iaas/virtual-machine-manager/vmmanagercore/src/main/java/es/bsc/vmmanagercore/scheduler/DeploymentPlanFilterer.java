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

package es.bsc.vmmanagercore.scheduler;

import es.bsc.vmmanagercore.model.scheduling.DeploymentPlan;
import es.bsc.vmmanagercore.model.scheduling.VmAssignmentToHost;
import es.bsc.vmmanagercore.model.vms.Vm;
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
        Map<Host, List<Vm> > vmsOfEachHost =
                getMapOfHostsAndTheirVmsAssignations(deploymentPlan.getVmsAssignationsToHosts());
        return allHostsHaveEnoughResourcesForTheirAssignations(vmsOfEachHost);
    }

    /**
     * Get a map of hosts and the VMs that they have been assigned. Each entry of the map contains a host (key),
     * and a list of VMs (values).
     *
     * @param vmAssignments a list of VMs assignations to hosts
     * @return the map
     */
    private static Map<Host, List<Vm>> getMapOfHostsAndTheirVmsAssignations(List<VmAssignmentToHost> vmAssignments) {
        Map<Host, List<Vm> > result = new HashMap<>();
        for (VmAssignmentToHost vmAssignmentToHost: vmAssignments) {
            if (!result.containsKey(vmAssignmentToHost.getHost())) {
                result.put(vmAssignmentToHost.getHost(), new ArrayList<Vm>());
            }
            result.get(vmAssignmentToHost.getHost()).add(vmAssignmentToHost.getVm());
        }
        return result;
    }

    /**
     * Checks whether a list of hosts have enough resources to deploy all the VMs that they have been assigned.
     *
     * @param vmsInEachHost list of VMs that are going to be deployed in each host
     * @return true if all hosts have enough resources to deploy all the VMs that they have been assigned.
     * False otherwise
     */
    private static boolean allHostsHaveEnoughResourcesForTheirAssignations(Map<Host, List<Vm> > vmsInEachHost) {
        for (Map.Entry<Host, List<Vm>> entry: vmsInEachHost.entrySet()) {
            if (!entry.getKey().hasEnoughResourcesToDeployVms(entry.getValue())) {
                return false;
            }
        }
        return true;
    }

}
