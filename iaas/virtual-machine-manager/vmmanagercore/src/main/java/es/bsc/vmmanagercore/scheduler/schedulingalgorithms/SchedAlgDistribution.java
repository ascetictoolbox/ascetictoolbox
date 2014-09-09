package es.bsc.vmmanagercore.scheduler.schedulingalgorithms;

import es.bsc.vmmanagercore.logging.VMMLogger;
import es.bsc.vmmanagercore.model.DeploymentPlan;
import es.bsc.vmmanagercore.model.ServerLoad;
import es.bsc.vmmanagercore.monitoring.Host;
import es.bsc.vmmanagercore.scheduler.Scheduler;

import java.util.Collection;
import java.util.List;

/**
 * Distribution scheduling algorithm.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class SchedAlgDistribution implements SchedAlgorithm {

    public SchedAlgDistribution() {}

    private void logServersLoadsInfo(Collection<ServerLoad> serversLoad1, Collection<ServerLoad> serversLoad2) {
        VMMLogger.logServersLoadsAfterDeploymentPlan(1, countIdleServers(serversLoad1),
                Scheduler.calculateStDevCpuLoad(serversLoad1), Scheduler.calculateStDevMemLoad(serversLoad1),
                Scheduler.calculateStDevDiskLoad(serversLoad1));
        VMMLogger.logServersLoadsAfterDeploymentPlan(2, countIdleServers(serversLoad2),
                Scheduler.calculateStDevCpuLoad(serversLoad2), Scheduler.calculateStDevMemLoad(serversLoad2),
                Scheduler.calculateStDevDiskLoad(serversLoad2));
    }

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
        logServersLoadsInfo(serversLoad1, serversLoad2);
        return usesMoreHosts(serversLoad1, serversLoad2) ||
                (!usesLessHosts(serversLoad1, serversLoad2) &&
                (hasLessStdDevCpu(serversLoad1, serversLoad2)
                || hasSameStDevCpuAndLessStdDevMem(serversLoad1, serversLoad2)
                || hasSameStdDevCpuAndSameStdDevMemAndLessStdDevDisk(serversLoad1, serversLoad2)));
    }

    @Override
    public boolean isBetterDeploymentPlan(DeploymentPlan deploymentPlan1, DeploymentPlan deploymentPlan2,
            List<Host> hosts) {
        return serverLoadsAreMoreDistributed(
                Scheduler.getServersLoadsAfterDeploymentPlanExecuted(deploymentPlan1, hosts).values(),
                Scheduler.getServersLoadsAfterDeploymentPlanExecuted(deploymentPlan2, hosts).values());
    }

}
