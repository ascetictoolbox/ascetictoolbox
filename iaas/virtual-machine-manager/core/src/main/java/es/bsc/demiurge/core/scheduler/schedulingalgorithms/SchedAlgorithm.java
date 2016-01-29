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

package es.bsc.demiurge.core.scheduler.schedulingalgorithms;

import es.bsc.demiurge.core.manager.components.EstimatesManager;
import es.bsc.demiurge.core.models.scheduling.DeploymentPlan;
import es.bsc.demiurge.core.models.vms.VmDeployed;
import es.bsc.demiurge.core.monitoring.hosts.Host;

import java.util.List;

/**
 *  Interface for scheduling algorithms.
 *
 *  @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 * @deprecated Scheduler and SchedAlgorithm classes must be replaced by Clopla
 */
@Deprecated()
public interface SchedAlgorithm {

    /**
     * Given a list of deployment plans, chooses the best according to the scheduling algorithm.
     *
     * @param deploymentPlans the list of deployment plans
     * @param hosts the list of hosts
     * @param deploymentId ID used to identify the deployment in the log messages
     * @return the best deployment plan
     */
    DeploymentPlan chooseBestDeploymentPlan(List<VmDeployed> vmsDeployed, EstimatesManager estimatorsManager, List<DeploymentPlan> deploymentPlans, List<Host> hosts,
                                            String deploymentId);

    /**
     * Returns the enumerated name of the scheduling algorithm
     * @return
     */
    String getName();

}