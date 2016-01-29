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

package es.bsc.demiurge.core.models.scheduling;

import es.bsc.demiurge.core.models.vms.Vm;
import es.bsc.demiurge.core.monitoring.hosts.Host;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Deployment plan. A deployment plan is a list where each item contains a pair {vm, host}. The pair indicates on
 * which host a specific VM should be deployed.
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class DeploymentPlan {

    //TODO: I should control that in this list a single host does not appear in several assignations
    private List<VmAssignmentToHost> vmsAssignationsToHosts = new ArrayList<>();

    public DeploymentPlan(List<VmAssignmentToHost> vmsAssignationsToHosts) {
        this.vmsAssignationsToHosts = vmsAssignationsToHosts;
    }

    /**
     * Checks whether the hosts of the deployment plan have enough resources to host the VMs that they have
     * been assigned.
     *
     * @return true if the deployment plan can be applied, false otherwise
     */
    public boolean canBeApplied() {
        return allHostsHaveEnoughResourcesForTheirAssignations();
    }

    /**
     * Returns the VMs that are assigned to a given host within the deployment plan.
     *
     * @param hostname the hostname
     * @return the list of VMs
     */
    public List<Vm> getVmsAssignedToHost(String hostname) {
        List<Vm> result = new ArrayList<>();
        for (VmAssignmentToHost assignment: vmsAssignationsToHosts) {
            if (hostname.equals(assignment.getHost().getHostname())) {
                result.add(assignment.getVm());
            }
        }
        return result;
    }

    public void addVmAssignmentToPlan(VmAssignmentToHost vmAssignmentToHost) {
        vmsAssignationsToHosts.add(vmAssignmentToHost);
    }

    public List<VmAssignmentToHost> getVmsAssignationsToHosts() {
        return new ArrayList<>(vmsAssignationsToHosts);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (VmAssignmentToHost vmAssignmentToHost: vmsAssignationsToHosts) {
            stringBuilder.append(vmAssignmentToHost.toString());
            stringBuilder.append(" ");
        }
        return stringBuilder.toString();
    }

    /**
     * Checks whether a list of hosts have enough resources to deploy all the VMs that they have been assigned.
     *
     * @return true if all hosts have enough resources to deploy all the VMs that they have been assigned.
     * False otherwise
     */
    private boolean allHostsHaveEnoughResourcesForTheirAssignations() {
        for (Map.Entry<Host, List<Vm>> entry: getMapOfHostsAndTheirVmsAssignations().entrySet()) {
            if (!entry.getKey().hasEnoughResourcesToDeployVms(entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets a map of hosts and the VMs that they have been assigned. Each entry of the map contains a host (key),
     * and a list of VMs (values).
     *
     * @return the map
     */
    private Map<Host, List<Vm>> getMapOfHostsAndTheirVmsAssignations() {
        Map<Host, List<Vm> > result = new HashMap<>();
        for (VmAssignmentToHost vmAssignmentToHost: vmsAssignationsToHosts) {
            if (!result.containsKey(vmAssignmentToHost.getHost())) {
                result.put(vmAssignmentToHost.getHost(), new ArrayList<Vm>());
            }
            result.get(vmAssignmentToHost.getHost()).add(vmAssignmentToHost.getVm());
        }
        return result;
    }

}


