package es.bsc.vmplacement.domain;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author David Ortiz (david.ortiz@bsc.es)
 */
public class ClusterStateTest {

    private final ClusterState clusterState = new ClusterState();

    @Before
    public void setUp() {
        initializeTestClusterState(clusterState);
    }

    @Test
    public void hostIsIdle() {
        for (Host host: clusterState.getHosts()) {
            if (host.getId() == (long) 1) {
                assertFalse(clusterState.hostIsIdle(host));
            }
            else {
                assertTrue(clusterState.hostIsIdle(host));
            }
        }
    }

    @Test
    public void countIdleHosts() {
        assertEquals(1, clusterState.countIdleHosts());
    }

    @Test
    public void countNonIdleHosts() {
        assertEquals(1, clusterState.countNonIdleHosts());
    }

    @Test
    public void getIdsOfAppsDeployedInHost() {
        for (Host host: clusterState.getHosts()) {
            if (host.getId() == (long) 1) {
                assertTrue(clusterState.getIdsOfAppsDeployedInHost(host).contains("app1"));
                assertTrue(clusterState.getIdsOfAppsDeployedInHost(host).contains("app2"));
                assertFalse(clusterState.getIdsOfAppsDeployedInHost(host).contains("fakeApp"));
            }
            else if (host.getId() == (long) 2) {
                assertTrue(clusterState.getIdsOfAppsDeployedInHost(host).isEmpty());
            }
        }
    }

    @Test
    public void getVmsDeployedInHost() {
        for (Host host: clusterState.getHosts()) {
            if (host.getId() == (long) 1) {
                assertEquals(2, clusterState.getVmsDeployedInHost(host).size());
            }
            else if (host.getId() == (long) 2) {
                assertEquals(0, clusterState.getVmsDeployedInHost(host).size());
            }
        }
    }

    @Test
    public void avgCpusAssignedPerHost() {
        assertEquals(1.0, clusterState.avgCpusAssignedPerHost(), 0.05);
    }

    @Test
    public void cpusAssignedInHost() {
        for (Host host: clusterState.getHosts()) {
            if (host.getId() == (long) 1) {
                assertEquals(2, clusterState.cpusAssignedInHost(host));
            }
            else if (host.getId() == (long) 2) {
                assertEquals(0, clusterState.cpusAssignedInHost(host));
            }
        }
    }

    @Test
    public void calculateCumulativeUnusedCpuPerc() {
        assertEquals(1.5, clusterState.calculateCumulativeUnusedCpuPerc(), 0.05);
    }

    private void initializeTestClusterState(ClusterState clusterState) {
        // Create the hosts
        List<Host> hosts = new ArrayList<>();
        Host host1 = new Host((long) 1, "1", 4, 4096, 4);
        Host host2 = new Host((long) 2, "2", 2, 2048, 2);
        hosts.add(host1);
        hosts.add(host2);

        // Create the VMs
        List<Vm> vms = new ArrayList<>();
        Vm vm1 = new Vm((long) 1, 1, 1024, 1, "app1");
        Vm vm2 = new Vm((long) 2, 1, 1024, 1, "app2");
        vm1.setHost(host1);
        vm2.setHost(host1);
        vms.add(vm1);
        vms.add(vm2);

        // Add the VMs and the hosts to the cluster state
        clusterState.setHosts(hosts);
        clusterState.setVms(vms);
    }

}
