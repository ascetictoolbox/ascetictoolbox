package es.bsc.clopla_benchmarking.models;

import es.bsc.clopla.domain.Host;
import es.bsc.clopla.domain.Vm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Cluster {

    private final List<Vm> vms = new ArrayList<>();
    private final List<Host> hosts = new ArrayList<>();

    public Cluster(List<Vm> vms, List<Host> hosts) {
        this.vms.addAll(vms);
        this.hosts.addAll(hosts);
    }

    public List<Vm> getVms() {
        return Collections.unmodifiableList(vms);
    }

    public List<Host> getHosts() {
        return Collections.unmodifiableList(hosts);
    }

    /**
     * Returns the cluster load without taking into account the placement of the VMs.
     * This means that this function returns total_size_VMs / total_size_Hosts, where
     * size includes CPUs, RAM, and disk.
     *
     * @return the cluster load
     */
    public ClusterLoad getClusterLoad() {
        return new ClusterLoad(getCpuLoad(), getRamLoad(), getDiskLoad());
    }

    private double getCpuLoad() {
        return (double)getTotalVmsCpus() / (double)getTotalHostsCpus();
    }

    private double getRamLoad() {
        return (double)getTotalVmsRam() / (double)getTotalHostsRam();
    }

    private double getDiskLoad() {
        return (double)getTotalVmsDisk() / (double)getTotalHostsDisk();
    }

    private int getTotalHostsCpus() {
        int result = 0;
        for (Host host : hosts) {
            result += host.getNcpus();
        }
        return result;
    }

    private int getTotalVmsCpus() {
        int result = 0;
        for (Vm vm : vms) {
            result += vm.getNcpus();
        }
        return result;
    }

    private int getTotalHostsRam() {
        int result = 0;
        for (Host host : hosts) {
            result += host.getRamMb();
        }
        return result;
    }

    private int getTotalVmsRam() {
        int result = 0;
        for (Vm vm : vms) {
            result += vm.getRamMb();
        }
        return result;
    }

    private int getTotalHostsDisk() {
        int result = 0;
        for (Host host : hosts) {
            result += host.getDiskGb();
        }
        return result;
    }

    private int getTotalVmsDisk() {
        int result = 0;
        for (Vm vm : vms) {
            result += vm.getDiskGb();
        }
        return result;
    }

}
