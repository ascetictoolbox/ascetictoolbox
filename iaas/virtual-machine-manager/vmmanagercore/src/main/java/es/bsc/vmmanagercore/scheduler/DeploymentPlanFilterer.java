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

import java.util.ArrayList;
import java.util.List;

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

        /* Ugly hack: in the Ascetic project I should not consider Asok10 */
        List<DeploymentPlan> plansWithoutExcludedHosts = filterDeploymentPlansThatUseHost(deploymentPlans, "Asok10");

        for (DeploymentPlan deploymentPlan: plansWithoutExcludedHosts) {
            if (deploymentPlan.canBeApplied()) {
                result.add(deploymentPlan);
            }
        }

        return result;
    }

    private static List<DeploymentPlan> filterDeploymentPlansThatUseHost(List<DeploymentPlan> deploymentPlans,
                                                                  String hostname) {
        List<DeploymentPlan> result = new ArrayList<>();
        for (DeploymentPlan deploymentPlan: deploymentPlans) {
            boolean asok10Used = false;
            for (VmAssignmentToHost vmAssignment: deploymentPlan.getVmsAssignationsToHosts()) {
                if (vmAssignment.getHost().getHostname().equals(hostname)) {
                    asok10Used = true;
                }
            }
            if (!asok10Used) {
                result.add(deploymentPlan);
            }
        }
        return result;
    }

}
