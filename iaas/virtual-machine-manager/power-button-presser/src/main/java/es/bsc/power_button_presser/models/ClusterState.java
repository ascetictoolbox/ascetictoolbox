/**
 Copyright (C) 2013-2014  Barcelona Supercomputing Center

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package es.bsc.power_button_presser.models;

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

    public List<Host> getTurnedOffHosts() {
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

    public int getTotalNumberOfCpusInOnServers() {
        int result = 0;
        for (Host host: getOnHosts()) {
            result += host.getTotalCpus();
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
    
    private List<Host> getOnHosts() {
        List<Host> result = new ArrayList<>();
        for (Host host: hosts) {
            if (!host.isOff()) {
                result.add(host);
            }
        }
        return result;
    }
    
}
