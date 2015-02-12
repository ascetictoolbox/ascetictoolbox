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

package es.bsc.power_button_presser.strategies;

import es.bsc.power_button_presser.models.ClusterState;
import es.bsc.power_button_presser.models.Host;
import es.bsc.power_button_presser.models.Vm;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JustInTimeStrategyTest {

    private final JustInTimeStrategy justInTimeStrategy = new JustInTimeStrategy();

    @Test
    public void getPowerButtonsToPressReturnsTheServersThatAreOnAndDoNotContainVms() {
        List<Host> hostsToPress = justInTimeStrategy.getPowerButtonsToPress(
                getClusterStateWithSomeOnServersWithoutVms());
        List<String> hostNamesPowerButtonsToPress = new ArrayList<>();
        for (Host host: hostsToPress) {
            hostNamesPowerButtonsToPress.add(host.getHostname());
        }
        assertTrue(hostNamesPowerButtonsToPress.contains("host3"));
        assertTrue(hostNamesPowerButtonsToPress.contains("host4"));
    }
    
    @Test
    public void getPowerButtonsToPressReturnsEmptyListWhenThereAreNoOnHostsWithoutVms() {
        assertEquals(0, justInTimeStrategy.getPowerButtonsToPress(getClusterStateWithoutServersOnWithoutVms()).size());
    }
    
    private ClusterState getClusterStateWithSomeOnServersWithoutVms() {
        List<Vm> vms = new ArrayList<>();
        vms.add(new Vm("vm1", 1, 1, 1, "host1"));
        
        List<Host> hosts = new ArrayList<>();
        hosts.add(new Host("host1", 1, 1, 1, 0, 0, 0, false)); // on and 1 VM
        hosts.add(new Host("host2", 1, 1, 1, 0, 0, 0, true)); // off
        hosts.add(new Host("host3", 1, 1, 1, 0, 0, 0, false)); // on and no VMs
        hosts.add(new Host("host4", 1, 1, 1, 0, 0, 0, false)); // on and no VMs
        
        return new ClusterState(vms, hosts);
    }
    
    private ClusterState getClusterStateWithoutServersOnWithoutVms() {
        List<Vm> vms = new ArrayList<>();
        vms.add(new Vm("vm1", 1, 1, 1, "host1"));

        List<Host> hosts = new ArrayList<>();
        hosts.add(new Host("host1", 1, 1, 1, 0, 0, 0, false)); // has 1 VM
        hosts.add(new Host("host2", 1, 1, 1, 0, 0, 0, true)); // is off

        return new ClusterState(vms, hosts);
    }
    
}
