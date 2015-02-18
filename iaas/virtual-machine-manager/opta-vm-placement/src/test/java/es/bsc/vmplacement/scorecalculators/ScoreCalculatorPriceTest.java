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
import es.bsc.vmplacement.modellers.PriceModeller;
import es.bsc.vmplacement.placement.config.VmPlacementConfig;
import org.junit.AfterClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author David Ortiz (david.ortiz@bsc.es)
 */
public class ScoreCalculatorPriceTest {

    private final Host host1 = new Host((long) 1, "1", 8, 8192, 8, false);
    private final Host host2 = new Host((long) 2, "2", 4, 4096, 4, false);
    private final Vm vm1 = new Vm.Builder((long) 1, 2, 2048, 2).build();
    private final Vm vm2 = new Vm.Builder((long) 2, 1, 1024, 1).build();

    @AfterClass
    public static void tearDown() {
        VmPlacementConfig.priceModeller = null; // It was mocked, we need to null it again so it does
                                                 // interfere with other tests.
    }
    
    @Test
    public void scoreTest() {
        ClusterState testClusterState = getTestClusterState();
        List<Vm> vmsInHost1 = new ArrayList<>();
        vmsInHost1.add(vm1);
        List<Vm> vmsInHost2 = new ArrayList<>();
        vmsInHost2.add(vm2);

        mockPriceModeller(vmsInHost1, vmsInHost2);

        ScoreCalculatorPrice scoreCalculatorPrice = new ScoreCalculatorPrice();

        assertEquals(0, scoreCalculatorPrice.calculateScore(testClusterState).getHardScore());
        assertEquals(-30, scoreCalculatorPrice.calculateScore(testClusterState).getSoftScore());
    }

    private void mockPriceModeller(List<Vm> vmsInHost1, List<Vm> vmsInHost2) {
        VmPlacementConfig.priceModeller = Mockito.mock(PriceModeller.class);
        Mockito.when(VmPlacementConfig.priceModeller.getCost(host1, vmsInHost1, null))
                .thenReturn(20.0);
        Mockito.when(VmPlacementConfig.priceModeller.getCost(host2, vmsInHost2, null))
                .thenReturn(10.0);
    }

    private ClusterState getTestClusterState() {
        List<Host> hosts = new ArrayList<>();
        hosts.add(host1);
        hosts.add(host2);

        List<Vm> vms = new ArrayList<>();
        vm1.setHost(host1);
        vm2.setHost(host2);
        vms.add(vm1);
        vms.add(vm2);

        ClusterState result = new ClusterState();
        result.setHosts(hosts);
        result.setVms(vms);
        return result;
    }
    
}
