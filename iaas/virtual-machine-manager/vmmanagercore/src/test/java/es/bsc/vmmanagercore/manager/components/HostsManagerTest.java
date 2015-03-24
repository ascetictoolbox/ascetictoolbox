package es.bsc.vmmanagercore.manager.components;

import es.bsc.vmmanagercore.monitoring.hosts.Host;
import es.bsc.vmmanagercore.monitoring.hosts.HostFake;
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
