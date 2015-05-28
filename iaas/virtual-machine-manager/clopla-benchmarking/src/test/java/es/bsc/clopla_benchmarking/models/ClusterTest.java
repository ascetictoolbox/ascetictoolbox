package es.bsc.clopla_benchmarking.models;

import es.bsc.clopla.domain.Host;
import es.bsc.clopla.domain.Vm;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ClusterTest {

    @Test
    public void avgLoadOfClusterIsCorrectOnStandardCase() {
        List<Vm> vms = new ArrayList<>();
        vms.add(new Vm.Builder((long) 1, 1, 1024, 1).build()); // 1 CPU, 1GB RAM, 1GB Disk
        vms.add(new Vm.Builder((long) 2, 2, 2048, 2).build()); // 2 CPU, 2GB RAM, 2GB Disk

        List<Host> hosts = new ArrayList<>();
        hosts.add(new Host((long) 1, "1", 4, 6144, 8, false)); // 4 CPU, 6GB RAM, 4GB Disk

        Cluster cluster = new Cluster(vms, hosts);

        assertEquals(0.75, cluster.getClusterLoad().getCpuLoad(), 0.05);
        assertEquals(0.5, cluster.getClusterLoad().getRamLoad(), 0.05);
        assertEquals(0.375, cluster.getClusterLoad().getDiskLoad(), 0.05);
    }

    @Test
    public void avgLoadOfClusterIsZeroWhenItHasNoVms() {
        List<Vm> vms = new ArrayList<>();
        List<Host> hosts = new ArrayList<>();
        hosts.add(new Host((long) 1, "1", 4, 6144, 8, false)); // 4 CPU, 6GB RAM, 4GB Disk
        Cluster cluster = new Cluster(vms, hosts);

        assertEquals(0, cluster.getClusterLoad().getCpuLoad(), 0.05);
        assertEquals(0, cluster.getClusterLoad().getRamLoad(), 0.05);
        assertEquals(0, cluster.getClusterLoad().getDiskLoad(), 0.05);
    }

    @Test
    public void avgLoadOfClusterCanBe1() {
        List<Vm> vms = new ArrayList<>();
        vms.add(new Vm.Builder((long) 1, 1, 1024, 1).build()); // 1 CPU, 1GB RAM, 1GB Disk
        vms.add(new Vm.Builder((long) 2, 2, 2048, 2).build()); // 2 CPU, 2GB RAM, 2GB Disk

        List<Host> hosts = new ArrayList<>();
        hosts.add(new Host((long) 1, "1", 3, 3072, 3, false)); // 4 CPU, 6GB RAM, 4GB Disk

        Cluster cluster = new Cluster(vms, hosts);

        assertEquals(1, cluster.getClusterLoad().getCpuLoad(), 0.05);
        assertEquals(1, cluster.getClusterLoad().getRamLoad(), 0.05);
        assertEquals(1, cluster.getClusterLoad().getDiskLoad(), 0.05);
    }

    @Test
    public void avgLoadOfClusterCanBeMoreThan1() {
        List<Vm> vms = new ArrayList<>();
        vms.add(new Vm.Builder((long) 1, 1, 1024, 1).build()); // 1 CPU, 1GB RAM, 1GB Disk
        vms.add(new Vm.Builder((long) 2, 1, 2048, 3).build()); // 1 CPU, 2GB RAM, 3GB Disk

        List<Host> hosts = new ArrayList<>();
        hosts.add(new Host((long) 1, "1", 1, 1024, 1, false)); // 1 CPU, 1GB RAM, 1GB Disk

        Cluster cluster = new Cluster(vms, hosts);

        assertEquals(2, cluster.getClusterLoad().getCpuLoad(), 0.05);
        assertEquals(3, cluster.getClusterLoad().getRamLoad(), 0.05);
        assertEquals(4, cluster.getClusterLoad().getDiskLoad(), 0.05);
    }

}
