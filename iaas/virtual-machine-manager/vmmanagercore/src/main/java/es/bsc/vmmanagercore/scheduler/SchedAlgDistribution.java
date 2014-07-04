package es.bsc.vmmanagercore.scheduler;

import es.bsc.vmmanagercore.model.DeploymentPlan;
import es.bsc.vmmanagercore.model.ServerLoad;
import es.bsc.vmmanagercore.model.Vm;
import es.bsc.vmmanagercore.monitoring.HostInfo;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Distribution scheduling algorithm.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class SchedAlgDistribution implements SchedAlgorithm {

    Logger logger = LogManager.getLogger(SchedAlgDistribution.class);

    public SchedAlgDistribution() {}

    @Override
    public String chooseHost(List<HostInfo> hostsInfo, Vm vm) {
        logger.debug("\n [VMM] ---DISTRIBUTION ALG. START--- \n " +
                "Applying distribution algorithm to schedule VM " + vm.toString());

        ServerLoad minFutureLoad = new ServerLoad(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
        String selectedHost = null;

        //for each host
        for (HostInfo hostInfo: hostsInfo) {

            //calculate the future load (%) of the host if the VM is deployed in that host
            ServerLoad futureServerLoad = hostInfo.getFutureLoadIfVMDeployed(vm);
            logger.debug("[VMM] The load of host " + hostInfo.getHostname() + " would be "
                    + futureServerLoad.toString());

            //check if the host will have the lowest load after deploying the VM
            boolean lessCpu = futureServerLoad.getCpuLoad() < minFutureLoad.getCpuLoad();
            boolean sameCpuLessMemory = (futureServerLoad.getCpuLoad() == minFutureLoad.getCpuLoad())
                    && (futureServerLoad.getRamLoad() < minFutureLoad.getRamLoad());
            boolean sameCpuSameMemoryLessDisk = (futureServerLoad.getCpuLoad() == minFutureLoad.getCpuLoad())
                    && (futureServerLoad.getRamLoad() == minFutureLoad.getRamLoad())
                    && (futureServerLoad.getDiskLoad() < minFutureLoad.getDiskLoad());

            //if the host will be the least loaded according to the specified criteria (CPU more
            //important than memory, and memory more important than disk), save it
            if (lessCpu || sameCpuLessMemory || sameCpuSameMemoryLessDisk) {
                selectedHost = hostInfo.getHostname();
                minFutureLoad = futureServerLoad;
            }

        }

        logger.debug("[VMM] VM " + vm.toString() + " is going to be deployed in " + selectedHost +
            "\n [VMM] ---DISTRIBUTION ALG. END---");

        return selectedHost;
    }

    private boolean serversLoadAreMoreDistributed(Collection<ServerLoad> serversLoad1,
            Collection<ServerLoad> serversLoad2) {
        boolean lessStDevCpu =
                Scheduler.calculateStDevCpuLoad(serversLoad1) < Scheduler.calculateStDevCpuLoad(serversLoad2);
        boolean sameStDevCpuAndLessStDevMem =
                (Scheduler.calculateStDevCpuLoad(serversLoad1) == Scheduler.calculateStDevCpuLoad(serversLoad2))
                && (Scheduler.calculateStDevMemLoad(serversLoad1) < Scheduler.calculateStDevMemLoad(serversLoad2));
        boolean sameStDevCpuAndSameStDevMemAndLessStDevDisk =
                (Scheduler.calculateStDevCpuLoad(serversLoad1) == Scheduler.calculateStDevCpuLoad(serversLoad2))
                && (Scheduler.calculateStDevMemLoad(serversLoad1) == Scheduler.calculateStDevMemLoad(serversLoad2))
                && (Scheduler.calculateStDevDiskLoad(serversLoad1) < Scheduler.calculateStDevDiskLoad(serversLoad2));
        return lessStDevCpu || sameStDevCpuAndLessStDevMem || sameStDevCpuAndSameStDevMemAndLessStDevDisk;
    }

    public DeploymentPlan chooseBestDeploymentPlan(List<DeploymentPlan> deploymentPlans, List<HostInfo> hosts) {
        DeploymentPlan bestDeploymentPlan = null;
        Collection<ServerLoad> serversLoadBestPlan = null;
        for (DeploymentPlan deploymentPlan: deploymentPlans) {
            Collection<ServerLoad> serversLoad = new ArrayList<>
                    (Scheduler.getServersLoadsAfterDeploymentPlanExecuted(deploymentPlan, hosts).values());
            if (bestDeploymentPlan == null || serversLoadAreMoreDistributed(serversLoad, serversLoadBestPlan)) {
                bestDeploymentPlan = deploymentPlan;
                serversLoadBestPlan = new ArrayList<>(serversLoad);
            }
        }
        return bestDeploymentPlan;
    }

}
