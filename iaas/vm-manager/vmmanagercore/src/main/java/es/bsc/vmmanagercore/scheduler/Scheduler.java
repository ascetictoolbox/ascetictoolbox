package es.bsc.vmmanagercore.scheduler;

import es.bsc.vmmanagercore.model.SchedulingAlgorithm;
import es.bsc.vmmanagercore.model.ServerLoad;
import es.bsc.vmmanagercore.model.Vm;
import es.bsc.vmmanagercore.model.VmDeployed;
import es.bsc.vmmanagercore.monitoring.HostInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *  Scheduler that decides where to place the VMs that need to be deployed.
 *  This scheduler can be configured to use different scheduling algorithms (consolidation, distribution, etc.)
 * 
 *  @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class Scheduler {

    private SchedAlgorithm schedAlgorithm;
    private List<VmDeployed> vmsDeployed;

    public Scheduler(SchedulingAlgorithm schedAlg, List<VmDeployed> vmsDeployed) {
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

    private void reserveResourcesForVmInHost(Vm vm, String hostForDeployment, List<HostInfo> hosts) {
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
    private String chooseHost(List<HostInfo> allHosts, List<HostInfo> hostsWithEnoughResources, Vm vm) {
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
    public Map<Vm, String> schedule(List<Vm> vms, List<HostInfo> hosts) {
        Map<Vm, String> scheduling = new HashMap<>(); // HashMap VM -> host where it is going to be deployed

        // For each of the VMs to be scheduled
        for (Vm vm: vms) {
            // Get hosts with enough resources
            List<HostInfo> hostsWithEnoughResources = HostFilter.filter(hosts, vm.getCpus(),
                    vm.getRamMb(), vm.getDiskGb());

            // Choose the host to deploy the VM
            String selectedHost = chooseHost(hosts, hostsWithEnoughResources, vm);

            // Add the host to the result
            scheduling.put(vm, selectedHost);

            // Reserve the resources that the VM needs
            reserveResourcesForVmInHost(vm, selectedHost, hostsWithEnoughResources);
        }

        return scheduling;
    }

}