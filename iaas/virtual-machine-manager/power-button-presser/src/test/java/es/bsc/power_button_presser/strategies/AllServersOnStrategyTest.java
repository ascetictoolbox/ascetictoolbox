package es.bsc.power_button_presser.strategies;

import es.bsc.power_button_presser.models.ClusterState;
import es.bsc.power_button_presser.models.Host;
import es.bsc.power_button_presser.models.Vm;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class AllServersOnStrategyTest {
    
    private final AllServersOnStrategy allServersOnStrategy = new AllServersOnStrategy();
    
    @Test
    public void getPowerButtonsToPressReturnsTheServersThatAreOff() {
        List<Host> hostsToPress = allServersOnStrategy.getPowerButtonsToPress(getClusterStateWithSomeServersOff());
        List<String> hostnamesOffServers = new ArrayList<>();
        for (Host host: hostsToPress) {
            hostnamesOffServers.add(host.getHostname());
        }
        assertTrue(hostnamesOffServers.contains("host3"));
        assertTrue(hostnamesOffServers.contains("host4"));
    }
    
    @Test
    public void getPowerButtonsToPressReturnsEmptyListWhenAllTheServersAreOn() {
        assertEquals(0, allServersOnStrategy.getPowerButtonsToPress(getClusterStateWithAllServersOn()).size());
    }

    private ClusterState getClusterStateWithSomeServersOff() {
        List<Host> hosts = new ArrayList<>();
        hosts.add(new Host("host1", 1, 1, 1, 0, 0, 0, false));
        hosts.add(new Host("host2", 1, 1, 1, 0, 0, 0, false));
        hosts.add(new Host("host3", 1, 1, 1, 0, 0, 0, true));
        hosts.add(new Host("host4", 1, 1, 1, 0, 0, 0, true));
        return new ClusterState(new ArrayList<Vm>(), hosts);
    }
    
    private ClusterState getClusterStateWithAllServersOn() {
        List<Host> hosts = new ArrayList<>();
        hosts.add(new Host("host1", 1, 1, 1, 0, 0, 0, false));
        hosts.add(new Host("host2", 1, 1, 1, 0, 0, 0, false));
        return new ClusterState(new ArrayList<Vm>(), hosts);
    }

}
