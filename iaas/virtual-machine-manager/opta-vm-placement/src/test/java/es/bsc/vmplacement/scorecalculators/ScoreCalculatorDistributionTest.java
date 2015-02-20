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

package es.bsc.vmplacement.scorecalculators;

import es.bsc.vmplacement.domain.ClusterState;
import es.bsc.vmplacement.domain.Host;
import es.bsc.vmplacement.domain.Vm;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author David Ortiz (david.ortiz@bsc.es)
 */
public class ScoreCalculatorDistributionTest {

    private final ScoreCalculatorDistribution scoreCalculatorDistribution = new ScoreCalculatorDistribution();

    @Test
    public void scoreTest() {
        ClusterState clusterState = getTestClusterState();
        assertEquals(-4, scoreCalculatorDistribution.calculateScore(clusterState).getHardScore());
        assertEquals(2, scoreCalculatorDistribution.calculateScore(clusterState).getMediumScore());
        assertEquals(-1, scoreCalculatorDistribution.calculateScore(clusterState).getSoftScore()); // rounding
    }

    private ClusterState getTestClusterState() {
        // Create hosts
        List<Host> hosts = new ArrayList<>();
        Host host1 = new Host((long) 1, "1", 8, 8192, 8, false);
        Host host2 = new Host((long) 2, "2", 4, 4096, 4, false);
        Host host3 = new Host((long) 3, "3", 2, 2048, 2, false);
        hosts.add(host1);
        hosts.add(host2);
        hosts.add(host3);

        // Create VMs
        List<Vm> vms = new ArrayList<>();
        Vm vm1 = new Vm.Builder((long) 1, 1, 1024, 1).build();
        Vm vm2 = new Vm.Builder((long) 2, 1, 1024, 1).build();
        Vm vm3 = new Vm.Builder((long) 3, 5, 5120, 5).build();
        vm1.setHost(host1);
        vm2.setHost(host2);
        vm3.setHost(host2);
        vms.add(vm1);
        vms.add(vm2);
        vms.add(vm3);

        // Build the solution
        ClusterState result = new ClusterState();
        result.setHosts(hosts);
        result.setVms(vms);
        return result;
    }

}
