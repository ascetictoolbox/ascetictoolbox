package es.bsc.vmmanagercore.scheduler;

import java.util.ArrayList;

import es.bsc.vmmanagercore.model.Vm;
import es.bsc.vmmanagercore.monitoring.HostInfo;

/**
 *
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class SchedAlgConsolidation implements SchedAlgorithm {

    public SchedAlgConsolidation() {}

    /**
     * Decides on which host deploy a VM according to its CPU, memory and disk requirements
     * @param hostsInfo Information of the hosts of the infrastructure
     * @param vm VM that needs to be deployed
     * @return The name of the host on which the VM should be deployed. Null if none of the hosts
     * has enough resources available
     */
    public String chooseHost(ArrayList<HostInfo> hostsInfo, Vm vm) {
        double maxFutureCpuLoad, maxFutureMemoryLoad, maxFutureDiskLoad;
        maxFutureCpuLoad = maxFutureMemoryLoad = maxFutureDiskLoad = 0;
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

            //check if the host will have the highest load after deploying the VM
            boolean moreCpu = futureCpuLoad > maxFutureCpuLoad;
            boolean sameCpuMoreMemory = (futureCpuLoad == maxFutureCpuLoad) && (futureMemoryLoad > maxFutureMemoryLoad);
            boolean sameCpuSameMemoryMoreDisk = (futureCpuLoad == maxFutureCpuLoad) &&
                    (futureMemoryLoad == maxFutureMemoryLoad) && (futureDiskLoad > maxFutureDiskLoad);

            //if the host will be the most loaded according to the specified criteria (CPU more
            //important than memory, and memory more important than disk)
            if (moreCpu || sameCpuMoreMemory || sameCpuSameMemoryMoreDisk) {
                //save its information so we can compare the hosts that we have not analyzed yet against it
                selectedHost = hostInfo.getHostname();
                maxFutureCpuLoad = futureCpuLoad;
                maxFutureMemoryLoad = futureMemoryLoad;
                maxFutureDiskLoad = futureDiskLoad;
            }

        }

        return selectedHost;
    }

}
