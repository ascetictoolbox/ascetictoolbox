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

import es.bsc.power_button_presser.hostselectors.RandomHostSelector;
import es.bsc.power_button_presser.models.ClusterState;
import es.bsc.power_button_presser.models.Host;
import es.bsc.power_button_presser.models.Vm;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class NBackupHostsStrategyTest {

    private final static int N_BACKUP_HOSTS = 2;
    private final NBackupHostsStrategy nBackupHostsStrategy = 
            new NBackupHostsStrategy(N_BACKUP_HOSTS, new RandomHostSelector());
    
    @Test
    public void getPowerButtonsReturnsAnEmptyListWhenThereAreNBackupHosts() {
        assertEquals(0, nBackupHostsStrategy.getPowerButtonsToPress(
                getClusterStateWithTheDesiredNumberOfBackupHosts()).size());
    }
    
    @Test
    public void getPowerButtonReturnsHostsWhenThereAreMoreBackupsThanNeeded() {
        assertEquals(1, nBackupHostsStrategy.getPowerButtonsToPress(
                getClusterStateWithMoreBackupsThanNeeded()).size());
    }
    
    @Test
    public void getPowerButtonReturnsHostsWhenThereAreLessBackupsThanNeeded() {
        assertEquals(1, nBackupHostsStrategy.getPowerButtonsToPress(
                getClusterStateWithLessBackupsThanNeeded()).size());
    }
    
    private ClusterState getClusterStateWithTheDesiredNumberOfBackupHosts() {
        List<Vm> vms = new ArrayList<>();
        vms.add(new Vm("vm1", 1, 1, 1, "host1"));
        
        List<Host> hosts = new ArrayList<>();
        hosts.add(new Host("host1", 1, 1, 1, 0, 0, 0, false));
        hosts.add(new Host("host2", 1, 1, 1, 0, 0, 0, false)); // backup host
        hosts.add(new Host("host3", 1, 1, 1, 0, 0, 0, false)); // backup host
        
        return new ClusterState(vms, hosts);
    }
    
    private ClusterState getClusterStateWithMoreBackupsThanNeeded() {
        List<Host> hosts = new ArrayList<>();
        hosts.add(new Host("host1", 1, 1, 1, 0, 0, 0, false)); // backup host
        hosts.add(new Host("host2", 1, 1, 1, 0, 0, 0, false)); // backup host
        hosts.add(new Host("host3", 1, 1, 1, 0, 0, 0, false)); // backup host

        return new ClusterState(new ArrayList<Vm>(), hosts);
    }
    
    private ClusterState getClusterStateWithLessBackupsThanNeeded() {
        List<Vm> vms = new ArrayList<>();
        vms.add(new Vm("vm1", 1, 1, 1, "host1"));
        vms.add(new Vm("vm2", 1, 1, 1, "host2"));

        List<Host> hosts = new ArrayList<>();
        hosts.add(new Host("host1", 1, 1, 1, 0, 0, 0, false));
        hosts.add(new Host("host2", 1, 1, 1, 0, 0, 0, false)); 
        hosts.add(new Host("host3", 1, 1, 1, 0, 0, 0, false)); // backup host
        hosts.add(new Host("host4", 1, 1, 1, 0, 0, 0, true)); // off host
        
        return new ClusterState(vms, hosts);
    }
    
}
