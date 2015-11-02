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

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ClusterStateTest {
    
    @Test
    public void getTotalNumberOfCpusOnServers() {
        assertEquals(5, getTestClusterState().getTotalNumberOfCpusInOnServers());
    }
    
    @Test
    public void getTurnedOffHosts() {
        assertEquals(1, getTestClusterState().getTurnedOffHosts().size());
        assertEquals("host3", getTestClusterState().getTurnedOffHosts().get(0).getHostname());
    }
    
    @Test
    public void getHostsWithoutVmsAndSwitchedOn() {
        assertEquals(1, getTestClusterState().getHostsWithoutVmsAndSwitchedOn().size());
        assertEquals("host2", getTestClusterState().getHostsWithoutVmsAndSwitchedOn().get(0).getHostname());
    }
    
    private ClusterState getTestClusterState() {
        List<Vm> vms = new ArrayList<>();
        vms.add(new Vm("vm1", 1, 1024, 1, "host1"));
        
        List<Host> hosts = new ArrayList<>();
        hosts.add(new Host("host1", 4, 1024, 1, 0, 0, 0, false));
        hosts.add(new Host("host2", 1, 1024, 1, 1, 1024, 1, false));
        hosts.add(new Host("host3", 1, 1024, 1, 0, 0, 0, true));
                
        return new ClusterState(vms, hosts);
    }
}
