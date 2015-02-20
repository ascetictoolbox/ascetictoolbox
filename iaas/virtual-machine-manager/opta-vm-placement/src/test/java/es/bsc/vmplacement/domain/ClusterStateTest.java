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
    public void calculateStdDevCpuPercUsedPerHost() {
        assertEquals(0.25, clusterState.calculateStdDevCpuPercUsedPerHost(), 0.1);
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

    @Test
    public void countVmMigrationsNeededWhenResultIsZero() {
        ClusterState clusterState = new ClusterState();
        
        List<Host> hosts = new ArrayList<>();
        Host host1 = new Host((long) 1, "1", 4, 4096, 4, true);
        hosts.add(host1);
        
        List<Vm> vms = new ArrayList<>();
        Vm vm1 = new Vm.Builder((long) 1, 1, 1024, 1).build();
        vm1.setHost(host1);
        vms.add(vm1);
        
        clusterState.setHosts(hosts);
        clusterState.setVms(vms);
        
        assertEquals(0, clusterState.countVmMigrationsNeeded(clusterState));
    }
    
    @Test
    public void countVmMigrationsNeededWhenResultIsGreaterThanZero() {
        // Prepare first cluster
        ClusterState clusterState1 = new ClusterState();
        
        List<Host> hosts = new ArrayList<>();
        Host host1 = new Host((long) 1, "1", 4, 4096, 4, true);
        Host host2 = new Host((long) 2, "2", 2, 2048, 4, true);
        hosts.add(host1);
        hosts.add(host2);
        
        List<Vm> vms1 = new ArrayList<>();
        Vm vm1 = new Vm.Builder((long) 1, 1, 1024, 1).build();
        vm1.setHost(host1);
        vms1.add(vm1);
        
        clusterState1.setHosts(hosts);
        clusterState1.setVms(vms1);
        
        // Prepare second cluster
        ClusterState clusterState2 = new ClusterState();
        
        List<Vm> vms2 = new ArrayList<>();
        Vm vm2 = new Vm.Builder((long) 1, 1, 1024, 1).build(); // VM with same ID
        vm2.setHost(host2); // VM on different cluster
        vms2.add(vm2);
        
        clusterState2.setHosts(hosts); // Hosts are the same
        clusterState2.setVms(vms2);
        
        assertEquals(1, clusterState1.countVmMigrationsNeeded(clusterState2));
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
