package es.bsc.vmmanagercore.scheduler;

import java.util.ArrayList;
import java.util.HashMap;

import es.bsc.vmmanagercore.model.SchedulingAlgorithm;
import es.bsc.vmmanagercore.model.Vm;
import es.bsc.vmmanagercore.monitoring.HostInfo;


/**
 *
 * 
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class Scheduler {

    private SchedAlgorithm schedAlgorithm;

    public Scheduler(SchedulingAlgorithm schedAlg) {
        setSchedAlgorithm(schedAlg);
    }

    private void setSchedAlgorithm(SchedulingAlgorithm schedAlg) {
        switch (schedAlg) {
            case CONSOLIDATION:
                schedAlgorithm = new SchedAlgConsolidation();
                break;
            case DISTRIBUTION:
                schedAlgorithm = new SchedAlgDistribution();
                break;
            case RANDOM:
                schedAlgorithm = new SchedAlgRandom();
                break;
        }
    }

    /**
     * Decides on which host deploy each of the VMs that need to be deployed
     * @param vmDescriptions the description of the VMs that need to be scheduled
     * @param hostsInfo information of the hosts of the infrastructure where the VMs need to be deployed
     * @return HashMap that contains for each VM description, the name of the host where
     * the VM should be deployed according to the scheduling algorithm
     */
    public HashMap<Vm, String> schedule(ArrayList<Vm> vmDescriptions, ArrayList<HostInfo> hostsInfo) {
        // HashMap VM -> host where it is going to be deployed
        HashMap<Vm, String> scheduling = new HashMap<>();

        // For each of the VMs to be scheduled
        for (Vm vmDescription: vmDescriptions) {

            // Get hosts with enough resources
            ArrayList<HostInfo> hostsWithEnoughResources = HostFilter.filter(hostsInfo, vmDescription.getCpus(),
                    vmDescription.getRamMb(), vmDescription.getDiskGb());

            // From the hosts with enough available resources, get one according to the scheduling algorithm selected
            String selectedHost = schedAlgorithm.chooseHost(hostsWithEnoughResources, vmDescription);

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
