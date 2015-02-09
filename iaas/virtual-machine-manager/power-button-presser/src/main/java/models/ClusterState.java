package models;

import java.util.ArrayList;
import java.util.List;

public class ClusterState {
    
    private final List<Vm> vms;
    private final List<Host> hosts;

    public ClusterState(List<Vm> vms, List<Host> hosts) {
        this.vms = new ArrayList<>(vms);
        
        // The hosts cannot be assigned directly.
        // The VMM returns host load according to a monitoring system (Ganglia, Zabbix, etc.)
        // We are interested in the load according to the VMs deployed in the host.
        List<Host> adaptedHosts = new ArrayList<>();
        for (Host host: hosts) {
            adaptedHosts.add(getHostWithAppropriateLoad(host));
        }
        this.hosts = adaptedHosts;
    }

    public List<Host> getSwitchedOffHosts() {
        List<Host> result = new ArrayList<>();
        for (Host host: hosts) {
            if (host.isOff()) {
                result.add(host);
            }
        }
        return result;
    }
    
    public List<Host> getHostsWithoutVmsAndSwitchedOn() {
        List<Host> result = new ArrayList<>();
        for (Host host: hosts) {
            if (!host.isOff() && filterVmsByHost(host.getHostname()).size() == 0) {
                result.add(host);
            }
        }
        return result;
    }

    private Host getHostWithAppropriateLoad(Host host) {
        return new Host(
                host.getHostname(),
                host.getTotalCpus(),
                host.getTotalMemoryMb(),
                host.getTotalDiskGb(),
                calculateAssignedCpus(host),
                calculateAssignedMemoryMb(host),
                calculateAssignedDiskGb(host),
                host.isOff());
    }
    
    private int calculateAssignedCpus(Host host) {
        int result = 0;
        List<Vm> vmsDeployedInHost = filterVmsByHost(host.getHostname());
        for (Vm vm: vmsDeployedInHost) {
            result += vm.getCpus();
        }
        return result;
    }
    
    private int calculateAssignedMemoryMb(Host host) {
        int result = 0;
        List<Vm> vmsDeployedInHost = filterVmsByHost(host.getHostname());
        for (Vm vm: vmsDeployedInHost) {
            result += vm.getRamMb();
        }
        return result;
    }
    
    private int calculateAssignedDiskGb(Host host) {
        int result = 0;
        List<Vm> vmsDeployedInHost = filterVmsByHost(host.getHostname());
        for (Vm vm: vmsDeployedInHost) {
            result += vm.getCpus();
        }
        return result;
    }
    
    private List<Vm> filterVmsByHost(String hostname) {
        List<Vm> result = new ArrayList<>();
        for (Vm vm: vms) {
            if (hostname.equals(vm.getHostname())) {
                result.add(vm);
            }
        }
        return result;
    }
    
}
