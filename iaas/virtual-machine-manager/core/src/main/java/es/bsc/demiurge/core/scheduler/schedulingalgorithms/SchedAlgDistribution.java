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
import es.bsc.demiurge.core.models.hosts.ServerLoad;
import es.bsc.demiurge.core.monitoring.hosts.Host;
import es.bsc.demiurge.core.scheduler.Scheduler;
import org.apache.log4j.LogManager;

import java.util.Collection;
import java.util.List;

/**
 * Distribution scheduling algorithm.
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 *
 * @deprecated Scheduler and SchedAlgorithm classes must be replaced by Clopla
 */
@Deprecated()
public class SchedAlgDistribution implements SchedAlgorithm {

    public SchedAlgDistribution() {}

    private boolean hasLessStdDevCpu(Collection<ServerLoad> serversLoad1, Collection<ServerLoad> serversLoad2) {
        return Scheduler.calculateStDevCpuLoad(serversLoad1) < Scheduler.calculateStDevCpuLoad(serversLoad2);
    }

    private boolean hasSameStDevCpuAndLessStdDevMem(Collection<ServerLoad> serversLoad1,
            Collection<ServerLoad> serversLoad2) {
        return (Scheduler.calculateStDevCpuLoad(serversLoad1) == Scheduler.calculateStDevCpuLoad(serversLoad2))
                && (Scheduler.calculateStDevMemLoad(serversLoad1) < Scheduler.calculateStDevMemLoad(serversLoad2));
    }

    private boolean hasSameStdDevCpuAndSameStdDevMemAndLessStdDevDisk(Collection<ServerLoad> serversLoad1,
            Collection<ServerLoad> serversLoad2) {
        return (Scheduler.calculateStDevCpuLoad(serversLoad1) == Scheduler.calculateStDevCpuLoad(serversLoad2))
                && (Scheduler.calculateStDevMemLoad(serversLoad1) == Scheduler.calculateStDevMemLoad(serversLoad2))
                && (Scheduler.calculateStDevDiskLoad(serversLoad1) < Scheduler.calculateStDevDiskLoad(serversLoad2));
    }

    private int countIdleServers(Collection<ServerLoad> serversLoad) {
        int result = 0;
        for (ServerLoad serverLoad: serversLoad) {
            if (serverLoad.isIdle()) {
                ++result;
            }
        }
        return result;
    }

    // Note: important consideration. For now, I assume that if a server has load_cpu, load_ram, and load_disk
    // < 5%, then it is idle ("does not have any VMs").
    private boolean usesLessHosts(Collection<ServerLoad> serversLoad1, Collection<ServerLoad> serversLoad2) {
        return countIdleServers(serversLoad1) > countIdleServers(serversLoad2);
    }

    private boolean usesMoreHosts(Collection<ServerLoad> serversLoad1, Collection<ServerLoad> serversLoad2) {
        return countIdleServers(serversLoad1) < countIdleServers(serversLoad2);
    }

    /**
     * Compares two sets of server loads.
     *
     * @param serversLoad1 first set of server loads
     * @param serversLoad2 second set of server loads
     * @return True if serversLoad1 is more distributed than serversLoad2, false otherwise
     */
    private boolean serverLoadsAreMoreDistributed(Collection<ServerLoad> serversLoad1,
            Collection<ServerLoad> serversLoad2) {
        return usesMoreHosts(serversLoad1, serversLoad2) ||
                (!usesLessHosts(serversLoad1, serversLoad2) &&
                (hasLessStdDevCpu(serversLoad1, serversLoad2)
                || hasSameStDevCpuAndLessStdDevMem(serversLoad1, serversLoad2)
                || hasSameStdDevCpuAndSameStdDevMemAndLessStdDevDisk(serversLoad1, serversLoad2)));
    }

    private boolean isBetterDeploymentPlan(DeploymentPlan deploymentPlan1, DeploymentPlan deploymentPlan2,
                                           List<Host> hosts) {
        return serverLoadsAreMoreDistributed(
                Scheduler.getServersLoadsAfterDeploymentPlanExecuted(deploymentPlan1, hosts).values(),
                Scheduler.getServersLoadsAfterDeploymentPlanExecuted(deploymentPlan2, hosts).values());
    }

    @Override
    public DeploymentPlan chooseBestDeploymentPlan(List<VmDeployed> vmsDeployed, EstimatesManager estimatorsManager, List<DeploymentPlan> deploymentPlans, List<Host> hosts,
                                                   String deploymentId) {
        DeploymentPlan bestDeploymentPlan = null;
        for (DeploymentPlan deploymentPlan: deploymentPlans) {
            Collection<ServerLoad> serversLoad =
                    Scheduler.getServersLoadsAfterDeploymentPlanExecuted(deploymentPlan, hosts).values();
            LogManager.getLogger(SchedAlgDistribution.class).debug("[VMM] Server loads for deployment plan [ " + deploymentPlan.toString() + "]: idle servers:"
                    + countIdleServers(serversLoad) + ", stdDevCpu:" + Scheduler.calculateStDevCpuLoad(serversLoad) + ", stdDevRam: " + Scheduler.calculateStDevMemLoad(serversLoad) + ", stdDevDisk: "
                    + Scheduler.calculateStDevDiskLoad(serversLoad) + " --id:" + deploymentId);
            if (bestDeploymentPlan == null || isBetterDeploymentPlan(deploymentPlan, bestDeploymentPlan, hosts)) {
                bestDeploymentPlan = deploymentPlan;
            }
        }
        return bestDeploymentPlan;
    }

    @Override
    public String getName() {
        return "distribution";
    }

}
