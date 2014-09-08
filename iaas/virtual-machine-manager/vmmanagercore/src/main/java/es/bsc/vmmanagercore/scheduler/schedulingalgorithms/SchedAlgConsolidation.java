package es.bsc.vmmanagercore.scheduler.schedulingalgorithms;

import es.bsc.vmmanagercore.logging.VMMLogger;
import es.bsc.vmmanagercore.model.DeploymentPlan;
import es.bsc.vmmanagercore.model.ServerLoad;
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

    private void logServersLoadsInfo(Collection<ServerLoad> serversLoad1, Collection<ServerLoad> serversLoad2) {
        VMMLogger.logUnusedServerLoadsAfterDeploymentPlan(1, Scheduler.getTotalUnusedCpuPerc(serversLoad1),
                Scheduler.getTotalUnusedMemPerc(serversLoad1), Scheduler.getTotalUnusedDiskPerc(serversLoad1));
        VMMLogger.logUnusedServerLoadsAfterDeploymentPlan(2, Scheduler.getTotalUnusedCpuPerc(serversLoad2),
                Scheduler.getTotalUnusedMemPerc(serversLoad2), Scheduler.getTotalUnusedDiskPerc(serversLoad2));
    }

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
        logServersLoadsInfo(serversLoad1, serversLoad2);
        return (usesLessHosts(serversLoad1, serversLoad2))
                || (!usesMoreHosts(serversLoad1, serversLoad2) && usesLessResources(serversLoad1, serversLoad2));
    }

    @Override
    public boolean isBetterDeploymentPlan(DeploymentPlan deploymentPlan1, DeploymentPlan deploymentPlan2,
            List<Host> hosts) {
        return serverLoadsAreMoreConsolidated(
                Scheduler.getServersLoadsAfterDeploymentPlanExecuted(deploymentPlan1, hosts).values(),
                Scheduler.getServersLoadsAfterDeploymentPlanExecuted(deploymentPlan2, hosts).values());
    }

}
