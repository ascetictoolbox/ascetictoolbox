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
import es.bsc.vmmanagercore.model.hosts.ServerLoad;
import es.bsc.vmmanagercore.model.scheduling.DeploymentPlan;
import es.bsc.vmmanagercore.monitoring.Host;
import es.bsc.vmmanagercore.scheduler.Scheduler;

import java.util.Collection;
import java.util.List;

/**
 * Consolidation scheduling algorithm.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class SchedAlgConsolidation implements SchedAlgorithm {

    public SchedAlgConsolidation() {}

    private boolean hasLessUnusedCpu(Collection<ServerLoad> serversLoad1, Collection<ServerLoad> serversLoad2) {
        return Scheduler.getTotalUnusedCpuPerc(serversLoad1) < Scheduler.getTotalUnusedCpuPerc(serversLoad2);
    }

    private boolean hasSameUnusedCpuAndLessUnusedMem (Collection<ServerLoad> serversLoad1,
            Collection<ServerLoad> serversLoad2) {
        return (Scheduler.getTotalUnusedCpuPerc(serversLoad1) == Scheduler.getTotalUnusedCpuPerc(serversLoad2))
                && (Scheduler.getTotalUnusedMemPerc(serversLoad1) < Scheduler.getTotalUnusedMemPerc(serversLoad2));
    }

    private boolean hasSameUnusedCpuAndMemAndLessUnusedDisk (Collection<ServerLoad> serversLoad1,
            Collection<ServerLoad> serversLoad2) {
        return (Scheduler.getTotalUnusedCpuPerc(serversLoad1) == Scheduler.getTotalUnusedCpuPerc(serversLoad2))
                && (Scheduler.getTotalUnusedMemPerc(serversLoad1) == Scheduler.getTotalUnusedMemPerc(serversLoad2))
                && (Scheduler.getTotalUnusedDiskPerc(serversLoad1) < Scheduler.getTotalUnusedDiskPerc(serversLoad2));
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

    private boolean usesLessResources(Collection<ServerLoad> serversLoad1, Collection<ServerLoad> serversLoad2) {
        return hasLessUnusedCpu(serversLoad1, serversLoad2)
                || hasSameUnusedCpuAndLessUnusedMem(serversLoad1, serversLoad2)
                || hasSameUnusedCpuAndMemAndLessUnusedDisk(serversLoad1, serversLoad2);
    }

    private boolean serverLoadsAreMoreConsolidated(Collection<ServerLoad> serversLoad1,
            Collection<ServerLoad> serversLoad2) {
        return (usesLessHosts(serversLoad1, serversLoad2))
                || (!usesMoreHosts(serversLoad1, serversLoad2) && usesLessResources(serversLoad1, serversLoad2));
    }

    private boolean isBetterDeploymentPlan(DeploymentPlan deploymentPlan1, DeploymentPlan deploymentPlan2,
            List<Host> hosts) {
        return serverLoadsAreMoreConsolidated(
                Scheduler.getServersLoadsAfterDeploymentPlanExecuted(deploymentPlan1, hosts).values(),
                Scheduler.getServersLoadsAfterDeploymentPlanExecuted(deploymentPlan2, hosts).values());
    }

    @Override
    public DeploymentPlan chooseBestDeploymentPlan(List<DeploymentPlan> deploymentPlans, List<Host> hosts,
            String deploymentId) {
        DeploymentPlan bestDeploymentPlan = null;
        for (DeploymentPlan deploymentPlan: deploymentPlans) {
            Collection<ServerLoad> serversLoad =
                    Scheduler.getServersLoadsAfterDeploymentPlanExecuted(deploymentPlan, hosts).values();
            VMMLogger.logUnusedServerLoadsAfterDeploymentPlan(deploymentPlan, countIdleServers(serversLoad),
                    Scheduler.getTotalUnusedCpuPerc(serversLoad), Scheduler.getTotalUnusedMemPerc(serversLoad),
                    Scheduler.getTotalUnusedDiskPerc(serversLoad), deploymentId);
            if (bestDeploymentPlan == null || isBetterDeploymentPlan(deploymentPlan, bestDeploymentPlan, hosts)) {
                bestDeploymentPlan = deploymentPlan;
            }
        }
        return bestDeploymentPlan;
    }

}
