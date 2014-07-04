package es.bsc.vmmanagercore.scheduler;

import es.bsc.vmmanagercore.model.DeploymentPlan;
import es.bsc.vmmanagercore.model.ServerLoad;
import es.bsc.vmmanagercore.model.Vm;
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

    Logger logger = LogManager.getLogger(SchedAlgDistribution.class);

    public SchedAlgConsolidation() {}

    @Override
    public String chooseHost(List<Host> hostsInfo, Vm vm) {
        logger.debug("\n [VMM] ---CONSOLIDATION ALG. START--- \n " +
                "Applying consolidation algorithm to schedule VM " + vm.toString());

        ServerLoad maxFutureLoad = new ServerLoad(0, 0, 0);
        String selectedHost = null;

        //for each host
        for (Host host : hostsInfo) {

            //calculate the future load (%) of the host if the VM is deployed in that host
            ServerLoad futureServerLoad = host.getFutureLoadIfVMDeployed(vm);
            logger.debug("[VMM] The load of host " + host.getHostname() + " would be "
                    + futureServerLoad.toString());

            //check if the host will have the highest load after deploying the VM
            boolean moreCpu = futureServerLoad.getCpuLoad() > maxFutureLoad.getCpuLoad();
            boolean sameCpuMoreMemory = (futureServerLoad.getCpuLoad() == maxFutureLoad.getCpuLoad())
                    && (futureServerLoad.getRamLoad() > maxFutureLoad.getRamLoad());
            boolean sameCpuSameMemoryMoreDisk = (futureServerLoad.getCpuLoad() == maxFutureLoad.getCpuLoad())
                    && (futureServerLoad.getRamLoad() == maxFutureLoad.getRamLoad())
                    && (futureServerLoad.getDiskLoad() > maxFutureLoad.getDiskLoad());

            //if the host will be the most loaded according to the specified criteria (CPU more
            //important than memory, and memory more important than disk)
            if (moreCpu || sameCpuMoreMemory || sameCpuSameMemoryMoreDisk) {
                //save its information so we can compare the hosts that we have not analyzed yet against it
                selectedHost = host.getHostname();
                maxFutureLoad = futureServerLoad;
            }

        }

        logger.debug("[VMM] VM " + vm.toString() + " is going to be deployed in " + selectedHost +
                "\n [VMM] ---CONSOLIDATION ALG. END---");

        return selectedHost;
    }

    private boolean serverLoadsAreMoreConsolidated(Collection<ServerLoad> serversLoad1,
            Collection<ServerLoad> serversLoad2) {
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
