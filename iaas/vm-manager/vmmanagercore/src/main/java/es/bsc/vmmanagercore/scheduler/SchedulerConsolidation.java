package es.bsc.vmmanagercore.scheduler;

import java.util.ArrayList;
import java.util.HashMap;

import es.bsc.vmmanagercore.model.Vm;
import es.bsc.vmmanagercore.monitoring.HostInfo;

/**
 *
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class SchedulerConsolidation implements Scheduler {

    public SchedulerConsolidation() {}

    /**
     * Decides on which host deploy a VM according to its CPU, memory and disk requirements
     * @param hostsInfo Information of the hosts of the infrastructure
     * @param vmCpus Number of available CPUs required
     * @param vmMemory Amount of memory required
     * @param vmDisk Amount of disk space required
     * @return The name of the host on which the VM should be deployed. Null if none of the hosts
     * has enough resources available
     */
    private String chooseHost(ArrayList<HostInfo> hostsInfo, int vmCpus, int vmMemory, int vmDisk) {
        double maxFutureCpuLoad, maxFutureMemoryLoad, maxFutureDiskLoad;
        maxFutureCpuLoad = maxFutureMemoryLoad = maxFutureDiskLoad = 0;
        String selectedHost = null;

        //for each host
        for (HostInfo hostInfo: hostsInfo) {

            //calculate the future usage of the host if the VM was deployed in that host
            double futureCpus = hostInfo.getAssignedCpus() + hostInfo.getReservedCpus() + vmCpus;
            double futureRamMb = hostInfo.getAssignedMemoryMb() +
                    hostInfo.getReservedMemoryMb() + vmMemory;
            double futureDiskGb = hostInfo.getAssignedDiskGb() +
                    hostInfo.getReservedDiskGb() + vmDisk;

            //calculate the future load (%) of the host if the VM is deployed in that host
            double futureCpuLoad = futureCpus/hostInfo.getTotalCpus();
            double futureMemoryLoad = futureRamMb/hostInfo.getTotalMemoryMb();
            double futureDiskLoad = futureDiskGb/hostInfo.getTotalDiskGb();

            //check if the host will have the highest load after deploying the VM
            boolean moreCpu = futureCpuLoad > maxFutureCpuLoad;
            boolean sameCpuMoreMemory = (futureCpuLoad == maxFutureCpuLoad) &&
                    (futureMemoryLoad > maxFutureMemoryLoad);
            boolean sameCpuSameMemoryMoreDisk = (futureCpuLoad == maxFutureCpuLoad) &&
                    (futureMemoryLoad == maxFutureMemoryLoad) &&
                    (futureDiskLoad > maxFutureDiskLoad);

            //if the host will be the most loaded according to the specified criteria (CPU more
            //important than memory, and memory more important than disk)
            if (moreCpu || sameCpuMoreMemory || sameCpuSameMemoryMoreDisk) {
                //save its information so we can compare the
                //hosts that we have not analyzed yet against it
                selectedHost = hostInfo.getHostname();
                maxFutureCpuLoad = futureCpuLoad;
                maxFutureMemoryLoad = futureMemoryLoad;
                maxFutureDiskLoad = futureDiskLoad;
            }

        }

        return selectedHost;
    }

    @Override
    public HashMap<Vm, String> schedule(ArrayList<Vm> vmDescriptions,
            ArrayList<HostInfo> hostsInfo) {
        HashMap<Vm, String> scheduling = new HashMap<>();

        //for each of the VMs to be scheduled
        for (Vm vmDescription: vmDescriptions) {

            //get hosts with enough resources
            ArrayList<HostInfo> hostsWithEnoughResources = HostFilter.filter(
                    hostsInfo, vmDescription.getCpus(), vmDescription.getRamMb(),
                    vmDescription.getDiskGb());

            //from the hosts with enough available resources, get the host that will have
            //less free resources after deploying the VM
            String selectedHost = chooseHost(hostsWithEnoughResources, vmDescription.getCpus(),
                    vmDescription.getRamMb(), vmDescription.getDiskGb());

            //add the host to the result
            scheduling.put(vmDescription, selectedHost);

            //reserve the resources that the VM needs
            for (HostInfo host: hostsWithEnoughResources) {
                if (host.getHostname().equals(selectedHost)) {
                    host.setReservedCpus(vmDescription.getCpus());
                    host.setReservedMemoryMb(vmDescription.getRamMb());
                    host.setReservedDiskGb(vmDescription.getDiskGb());
                }
            }

        }

        return scheduling;
    }

}
