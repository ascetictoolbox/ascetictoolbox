package es.bsc.vmmanagercore.scheduler;

import es.bsc.vmmanagercore.model.SchedulingAlgorithm;
import es.bsc.vmmanagercore.model.Vm;
import es.bsc.vmmanagercore.model.VmDeployed;
import es.bsc.vmmanagercore.monitoring.HostInfo;

import java.util.ArrayList;
import java.util.HashMap;


/**
 *
 * 
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class Scheduler {

    private SchedAlgorithm schedAlgorithm;
    private ArrayList<VmDeployed> vmsDeployed;

    public Scheduler(SchedulingAlgorithm schedAlg, ArrayList<VmDeployed> vmsDeployed) {
        this.vmsDeployed = vmsDeployed;
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
            case GROUP_BY_APP:
                schedAlgorithm = new SchedAlgGroupByApp(vmsDeployed);
                break;
            case RANDOM:
                schedAlgorithm = new SchedAlgRandom();
                break;
        }
    }

    private void reserveResourcesForVmInHost(Vm vm, String hostForDeployment, ArrayList<HostInfo> hosts) {
        for (HostInfo host: hosts) {
            if (host.getHostname().equals(hostForDeployment)) {
                host.setReservedCpus(vm.getCpus());
                host.setReservedMemoryMb(vm.getRamMb());
                host.setReservedDiskGb(vm.getDiskGb());
            }
        }
    }

    /**
     * Decides on which host deploy each of the VMs that need to be deployed
     * @param vms the description of the VMs that need to be scheduled
     * @param hostsInfo information of the hosts of the infrastructure where the VMs need to be deployed
     * @return HashMap that contains for each VM description, the name of the host where
     * the VM should be deployed according to the scheduling algorithm
     */
    public HashMap<Vm, String> schedule(ArrayList<Vm> vms, ArrayList<HostInfo> hostsInfo) {
        HashMap<Vm, String> scheduling = new HashMap<>(); // HashMap VM -> host where it is going to be deployed

        // For each of the VMs to be scheduled
        for (Vm vm: vms) {
            // Get hosts with enough resources
            ArrayList<HostInfo> hostsWithEnoughResources = HostFilter.filter(hostsInfo, vm.getCpus(),
                    vm.getRamMb(), vm.getDiskGb());

            // From the hosts with enough available resources, select one according to the scheduling algorithm used
            String selectedHost = schedAlgorithm.chooseHost(hostsWithEnoughResources, vm);

            // Add the host to the result
            scheduling.put(vm, selectedHost);

            // Reserve the resources that the VM needs
            reserveResourcesForVmInHost(vm, selectedHost, hostsWithEnoughResources);
        }

        return scheduling;
    }
}