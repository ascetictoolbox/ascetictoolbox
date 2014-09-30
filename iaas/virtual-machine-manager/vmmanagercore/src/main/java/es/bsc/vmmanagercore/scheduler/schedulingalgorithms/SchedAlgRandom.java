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
import es.bsc.vmmanagercore.model.scheduling.DeploymentPlan;
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
    public DeploymentPlan chooseBestDeploymentPlan(List<DeploymentPlan> deploymentPlans, List<Host> hosts,
            String deploymentId) {
        DeploymentPlan bestDeploymentPlan = null;
        double bestDeploymentPlanScore = -1;
        for (DeploymentPlan deploymentPlan: deploymentPlans) {
            double deploymentPlanScore = Math.random();
            VMMLogger.logDeploymentPlanRandomScore(deploymentPlan, deploymentPlanScore, deploymentId);
            if (deploymentPlanScore > bestDeploymentPlanScore) {
                bestDeploymentPlan = deploymentPlan;
                bestDeploymentPlanScore = deploymentPlanScore;
            }
        }
        return bestDeploymentPlan;
    }

}
