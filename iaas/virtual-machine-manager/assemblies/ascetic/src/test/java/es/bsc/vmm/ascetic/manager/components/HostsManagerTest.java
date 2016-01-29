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

package es.bsc.vmm.ascetic.manager.components;

import es.bsc.demiurge.core.manager.components.HostsManager;
import es.bsc.demiurge.core.monitoring.hosts.Host;
import es.bsc.demiurge.fake.HostFake;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.*;

public class HostsManagerTest {
    
    @Test
    public void getHosts() {
        List<Host> hosts = new ArrayList<>();
        HostFake host1 = new HostFake("host1", 1, 1024, 10, 0, 0, 0);
        HostFake host2 = new HostFake("host2", 2, 2048, 20, 0, 0, 0);
        hosts.add(host1);
        hosts.add(host2);
        
        HostsManager hostsManager = new HostsManager(hosts);
        assertTrue(hostsManager.getHosts().contains(host1) 
                && hostsManager.getHosts().contains(host2));
    }
    
    @Test
    public void getHostsReturnsEmptyWhenThereAreNoHosts() {
        HostsManager hostsManager = new HostsManager(new ArrayList<Host>());
        assertTrue(hostsManager.getHosts().isEmpty());
    }
    
    @Test
    public void getHost() {
        List<Host> hosts = new ArrayList<>();
        HostFake host1 = new HostFake("host1", 1, 1024, 10, 0, 0, 0);
        HostFake host2 = new HostFake("host2", 2, 2048, 20, 0, 0, 0);
        hosts.add(host1);
        hosts.add(host2);

        HostsManager hostsManager = new HostsManager(hosts);
        assertEquals(host1, hostsManager.getHost("host1"));
        assertEquals(host2, hostsManager.getHost("host2"));
    }
    
    @Test
    public void getHostReturnsNullWhenDoesNotExist() {
        HostsManager hostsManager = new HostsManager(new ArrayList<Host>());
        assertNull(hostsManager.getHost("NonExistingHost"));
    }
    
    // Missing tests for power button of on/off hosts.
    // Take into account the power on/off delay time.
    
}
