package es.bsc.vmmanagercore.scheduler;

import es.bsc.vmmanagercore.model.Vm;
import es.bsc.vmmanagercore.monitoring.HostInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 *
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class SchedAlgDistribution implements SchedAlgorithm {

    Logger logger = LoggerFactory.getLogger(SchedAlgDistribution.class);

    public SchedAlgDistribution() {}

    /**
     * Decides on which host deploy a VM according to its CPU, memory and disk requirements
     * @param hostsInfo Information of the hosts of the infrastructure
     * @param vm VM that needs to be deployed
     * @return The name of the host on which the VM should be deployed. Null if none of the hosts
     * has enough resources available (hostsInfo is an empty list).
     */
    public String chooseHost(ArrayList<HostInfo> hostsInfo, Vm vm) {
        logger.debug("Applying distribution algorithm to schedule VM { name: " + vm.getName() + ", cpus: " +
                vm.getCpus() + ", ram(MB): " + vm.getRamMb() + ", disk(GB): " + vm.getDiskGb() + " }");

        double minFutureCpuLoad, minFutureMemoryLoad, minFutureDiskLoad;
        minFutureCpuLoad = minFutureMemoryLoad = minFutureDiskLoad = Double.MAX_VALUE;
        String selectedHost = null;

        //for each host
        for (HostInfo hostInfo: hostsInfo) {

            //calculate the future usage of the host if the VM was deployed in that host
            double futureCpus = hostInfo.getAssignedCpus() + hostInfo.getReservedCpus() + vm.getCpus();
            double futureRamMb = hostInfo.getAssignedMemoryMb() + hostInfo.getReservedMemoryMb() + vm.getRamMb();
            double futureDiskGb = hostInfo.getAssignedDiskGb() + hostInfo.getReservedDiskGb() + vm.getDiskGb();

            //calculate the future load (%) of the host if the VM is deployed in that host
            double futureCpuLoad = futureCpus/hostInfo.getTotalCpus();
            double futureMemoryLoad = futureRamMb/hostInfo.getTotalMemoryMb();
            double futureDiskLoad = futureDiskGb/hostInfo.getTotalDiskGb();

            //check if the host will have the lowest load after deploying the VM
            boolean lessCpu = futureCpuLoad < minFutureCpuLoad;
            boolean sameCpuLessMemory = (futureCpuLoad == minFutureCpuLoad)
                    && (futureMemoryLoad < minFutureMemoryLoad);
            boolean sameCpuSameMemoryLessDisk = (futureCpuLoad == minFutureCpuLoad)
                    && (futureMemoryLoad == minFutureMemoryLoad) && (futureDiskLoad < minFutureDiskLoad);

            //if the host will be the least loaded according to the specified criteria (CPU more
            //important than memory, and memory more important than disk)
            if (lessCpu || sameCpuLessMemory || sameCpuSameMemoryLessDisk) {
                //save its information so we can compare the hosts that we have not analyzed yet against it
                selectedHost = hostInfo.getHostname();
                minFutureCpuLoad = futureCpuLoad;
                minFutureMemoryLoad = futureMemoryLoad;
                minFutureDiskLoad = futureDiskLoad;
            }

        }

        return selectedHost;
    }

}
