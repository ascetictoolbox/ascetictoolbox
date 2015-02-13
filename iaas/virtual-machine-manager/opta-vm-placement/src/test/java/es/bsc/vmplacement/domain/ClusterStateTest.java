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
        assertEquals(150, clusterState.calculateCumulativeUnusedCpuPerc());
    }

    @Test
    public void calculateStdDevCpusAssignedPerHost() {
        assertEquals(1.0, clusterState.calculateStdDevCpusAssignedPerHost(), 0.05);
    }
    
    @Test
    public void countOffHostsReturns0WhenNoneOfTheHostsWereOff() {
        ClusterState clusterState = new ClusterState();
        List<Host> hosts = new ArrayList<>();
        Host host1 = new Host((long) 1, "1", 4, 4096, 4, false);
        Host host2 = new Host((long) 2, "2", 2, 2048, 2, false);
        hosts.add(host1);
        hosts.add(host2);
        clusterState.setHosts(hosts);
        clusterState.setVms(new ArrayList<Vm>());
        assertEquals(0, clusterState.countOffHosts());
    }
    
    @Test
    public void countOffHostsWhenOneOfTheHostsWasOffButNowHasVms() {
        ClusterState clusterState = new ClusterState();
        List<Host> hosts = new ArrayList<>();
        Host host1 = new Host((long) 1, "1", 4, 4096, 4, false);
        hosts.add(host1);
        clusterState.setHosts(hosts);
        
        List<Vm> vms = new ArrayList<>();
        Vm vm1 = new Vm.Builder((long) 1, 1, 1024, 1).appId("app1").build();
        vm1.setHost(hosts.get(0));
        clusterState.setVms(vms);
        
        assertEquals(0, clusterState.countOffHosts());
    }
    
    @Test
    public void countOffHostsWhenThereWereSwitchedOffHosts() {
        ClusterState clusterState = new ClusterState();
        List<Host> hosts = new ArrayList<>();
        Host host1 = new Host((long) 1, "1", 4, 4096, 4, true);
        Host host2 = new Host((long) 2, "2", 2, 2048, 2, false);
        hosts.add(host1);
        hosts.add(host2);
        clusterState.setHosts(hosts);
        clusterState.setVms(new ArrayList<Vm>());
        assertEquals(1, clusterState.countOffHosts());
    }

    private void initializeTestClusterState(ClusterState clusterState) {
        List<Host> hosts = getTestHosts();
        clusterState.setHosts(hosts);
        clusterState.setVms(getTestVms(hosts));
    }

    private List<Host> getTestHosts() {
        List<Host> hosts = new ArrayList<>();
        Host host1 = new Host((long) 1, "1", 4, 4096, 4, false);
        Host host2 = new Host((long) 2, "2", 2, 2048, 2, false);
        hosts.add(host1);
        hosts.add(host2);
        return hosts;
    }

    private List<Vm> getTestVms(List<Host> hosts) {
        List<Vm> vms = new ArrayList<>();
        Vm vm1 = new Vm.Builder((long) 1, 1, 1024, 1).appId("app1").build();
        Vm vm2 = new Vm.Builder((long) 2, 1, 1024, 1).appId("app2").build();
        vm1.setHost(hosts.get(0));
        vm2.setHost(hosts.get(0));
        vms.add(vm1);
        vms.add(vm2);
        return vms;
    }

}
