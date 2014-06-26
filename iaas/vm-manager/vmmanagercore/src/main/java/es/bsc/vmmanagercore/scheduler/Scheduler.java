package es.bsc.vmmanagercore.scheduler;

import es.bsc.vmmanagercore.model.SchedulingAlgorithm;
import es.bsc.vmmanagercore.model.ServerLoad;
import es.bsc.vmmanagercore.model.Vm;
import es.bsc.vmmanagercore.model.VmDeployed;
import es.bsc.vmmanagercore.monitoring.HostInfo;

import java.util.ArrayList;
import java.util.HashMap;


/**
 *  Scheduler that decides where to place the VMs that need to be deployed.
 *  This scheduler can be configured to use different scheduling algorithms (consolidation, distribution, etc.)
 * 
 *  @author David Ortiz Lopez (david.ortiz@bsc.es)
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
            case ENERGY_AWARE:
                schedAlgorithm = new SchedAlgEnergyAware(vmsDeployed);
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
     * Chooses the host where a VM should be deployed. If none of the hosts has enough resources available,
     * then chooses one randomly. Otherwise, selects a host according to the scheduling algorithm used.
     *
     * @param allHosts all the hosts of the cluster
     * @param hostsWithEnoughResources the hosts of the clusted with enough resources available to deploy the VM
     * @param vm the VM
     * @return the name of the host where the VM should be deployed
     */
    private String chooseHost(ArrayList<HostInfo> allHosts, ArrayList<HostInfo> hostsWithEnoughResources, Vm vm) {
        String selectedHost;
        if (hostsWithEnoughResources.isEmpty()) {
            selectedHost = new SchedAlgRandom().chooseHost(allHosts, vm);
        }
        else {
            selectedHost = schedAlgorithm.chooseHost(hostsWithEnoughResources, vm);
        }
        return selectedHost;
    }

    /**
     * Decides on which host deploy each of the VMs that need to be deployed. When there are no hosts that
     * satisfy the requirements for a specific VM, that VM is deployed in a host chosen randomly.
     *
     * @param vms the description of the VMs that need to be scheduled
     * @param hosts information of the hosts of the infrastructure where the VMs need to be deployed
     * @return HashMap that contains for each VM description, the name of the host where
     * the VM should be deployed according to the scheduling algorithm
     */
    public HashMap<Vm, String> schedule(ArrayList<Vm> vms, ArrayList<HostInfo> hosts) {
        HashMap<Vm, String> scheduling = new HashMap<>(); // HashMap VM -> host where it is going to be deployed

        // For each of the VMs to be scheduled
        for (Vm vm: vms) {
            // Get hosts with enough resources
            ArrayList<HostInfo> hostsWithEnoughResources = HostFilter.filter(hosts, vm.getCpus(),
                    vm.getRamMb(), vm.getDiskGb());

            // Chose the host to deploy the VM
            String selectedHost = chooseHost(hosts, hostsWithEnoughResources, vm);

            // Add the host to the result
            scheduling.put(vm, selectedHost);

            // Reserve the resources that the VM needs
            reserveResourcesForVmInHost(vm, selectedHost, hostsWithEnoughResources);
        }

        return scheduling;
    }

    /**
     * Returns the load that a host would have if a VM was deployed in it.
     *
     * @param vm the VM to deploy
     * @param host the host where the VM would be deployed
     * @return the future load
     */
    public static ServerLoad getFutureLoadIfVMDeployedInHost(Vm vm, HostInfo host) {
        double cpus = host.getAssignedCpus() + host.getReservedCpus() + vm.getCpus();
        double ramMb = host.getAssignedMemoryMb() + host.getReservedMemoryMb() + vm.getRamMb();
        double diskGb = host.getAssignedDiskGb() + host.getReservedDiskGb() + vm.getDiskGb();
        return new ServerLoad(cpus/host.getTotalCpus(), ramMb/host.getTotalMemoryMb(), diskGb/host.getTotalDiskGb());
    }

}