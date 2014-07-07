package es.bsc.vmmanagercore.scheduler;

import es.bsc.vmmanagercore.logging.VMMLogger;
import es.bsc.vmmanagercore.model.DeploymentPlan;
import es.bsc.vmmanagercore.model.ServerLoad;
import es.bsc.vmmanagercore.monitoring.Host;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

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
        VMMLogger.logServersLoadsAfterDeploymentPlan(1, Scheduler.calculateStDevCpuLoad(serversLoad1),
                Scheduler.calculateStDevMemLoad(serversLoad1), Scheduler.calculateStDevDiskLoad(serversLoad1));
        VMMLogger.logServersLoadsAfterDeploymentPlan(2, Scheduler.calculateStDevCpuLoad(serversLoad2),
                Scheduler.calculateStDevMemLoad(serversLoad2), Scheduler.calculateStDevDiskLoad(serversLoad2));
    }

    private boolean serverLoadsAreMoreConsolidated(Collection<ServerLoad> serversLoad1,
            Collection<ServerLoad> serversLoad2) {
        logServersLoadsInfo(serversLoad1, serversLoad2);
        boolean moreStDevCpu =
                Scheduler.calculateStDevCpuLoad(serversLoad1) > Scheduler.calculateStDevCpuLoad(serversLoad2);
        boolean sameStDevCpuAndMoreStDevMem =
                (Scheduler.calculateStDevCpuLoad(serversLoad1) == Scheduler.calculateStDevCpuLoad(serversLoad2))
                && (Scheduler.calculateStDevMemLoad(serversLoad1) > Scheduler.calculateStDevMemLoad(serversLoad2));
        boolean sameStDevCpuAndSameStDevMemAndMoreStDevDisk =
                (Scheduler.calculateStDevCpuLoad(serversLoad1) == Scheduler.calculateStDevCpuLoad(serversLoad2))
                && (Scheduler.calculateStDevMemLoad(serversLoad1) == Scheduler.calculateStDevMemLoad(serversLoad2))
                && (Scheduler.calculateStDevDiskLoad(serversLoad1) > Scheduler.calculateStDevDiskLoad(serversLoad2));
        return moreStDevCpu || sameStDevCpuAndMoreStDevMem || sameStDevCpuAndSameStDevMemAndMoreStDevDisk;
    }

    @Override
    public boolean isBetterDeploymentPlan(DeploymentPlan deploymentPlan1, DeploymentPlan deploymentPlan2,
            List<Host> hosts) {
        return serverLoadsAreMoreConsolidated(
                Scheduler.getServersLoadsAfterDeploymentPlanExecuted(deploymentPlan1, hosts).values(),
                Scheduler.getServersLoadsAfterDeploymentPlanExecuted(deploymentPlan2, hosts).values());
    }

}
