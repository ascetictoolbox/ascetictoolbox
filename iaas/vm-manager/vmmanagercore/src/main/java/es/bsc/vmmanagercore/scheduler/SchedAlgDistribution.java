package es.bsc.vmmanagercore.scheduler;

import es.bsc.vmmanagercore.model.ServerLoad;
import es.bsc.vmmanagercore.model.Vm;
import es.bsc.vmmanagercore.monitoring.HostInfo;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.List;

/**
 *
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class SchedAlgDistribution implements SchedAlgorithm {

    Logger logger = LogManager.getLogger(SchedAlgDistribution.class);

    public SchedAlgDistribution() {}

    /**
     * Decides on which host deploy a VM according to its CPU, memory and disk requirements
     * @param hostsInfo Information of the hosts of the infrastructure
     * @param vm VM that needs to be deployed
     * @return The name of the host on which the VM should be deployed. Null if none of the hosts
     * has enough resources available (hostsInfo is an empty list).
     */
    @Override
    public String chooseHost(List<HostInfo> hostsInfo, Vm vm) {
        logger.debug("\n [VMM] ---DISTRIBUTION ALG. START--- \n " +
                "Applying distribution algorithm to schedule VM " + vm.toString());

        ServerLoad minFutureLoad = new ServerLoad(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
        String selectedHost = null;

        //for each host
        for (HostInfo hostInfo: hostsInfo) {

            //calculate the future load (%) of the host if the VM is deployed in that host
            ServerLoad futureServerLoad = Scheduler.getFutureLoadIfVMDeployedInHost(vm, hostInfo);
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
        LogManager.shutdown();

        return selectedHost;
    }

}
