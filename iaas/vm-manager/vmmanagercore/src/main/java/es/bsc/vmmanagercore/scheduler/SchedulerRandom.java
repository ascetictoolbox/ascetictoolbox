package es.bsc.vmmanagercore.scheduler;

import java.util.ArrayList;
import java.util.HashMap;

import es.bsc.vmmanagercore.model.Vm;
import es.bsc.vmmanagercore.monitoring.HostInfo;

public class SchedulerRandom implements Scheduler {

    private String chooseHost(ArrayList<HostInfo> hostsInfo) {
        int randomHostIndex = (int)(Math.random()*hostsInfo.size());
        return hostsInfo.get(randomHostIndex).getHostname();
    }

    @Override
    public HashMap<Vm, String> schedule(ArrayList<Vm> vmDescriptions,
            ArrayList<HostInfo> hostsInfo) {
        HashMap<Vm, String> scheduling = new HashMap<>();

        // For each of the VMs to be scheduled
        for (Vm vmDescription: vmDescriptions) {

            // Get hosts with enough resources
            ArrayList<HostInfo> hostsWithEnoughResources = HostFilter.filter(
                    hostsInfo, vmDescription.getCpus(), vmDescription.getRamMb(),
                    vmDescription.getDiskGb());

            // From the hosts with enough available resources, get one randomly
            String selectedHost = chooseHost(hostsWithEnoughResources);

            // Add the host to the result
            scheduling.put(vmDescription, selectedHost);

            // Reserve the resources that the VM needs
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
