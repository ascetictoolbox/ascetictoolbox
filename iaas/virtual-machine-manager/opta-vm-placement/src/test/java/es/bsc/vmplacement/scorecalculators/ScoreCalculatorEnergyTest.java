/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package es.bsc.vmplacement.scorecalculators;

import es.bsc.vmplacement.domain.ClusterState;
import es.bsc.vmplacement.domain.Host;
import es.bsc.vmplacement.domain.Vm;
import es.bsc.vmplacement.modellers.EnergyModeller;
import es.bsc.vmplacement.placement.config.VmPlacementConfig;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author David Ortiz (david.ortiz@bsc.es)
 */
public class ScoreCalculatorEnergyTest {

    private final Host host1 = new Host((long) 1, "1", 8, 8192, 8, false);
    private final Host host2 = new Host((long) 2, "2", 4, 4096, 4, false);
    private final Vm vm1 = new Vm.Builder((long) 1, 2, 2048, 2).build();
    private final Vm vm2 = new Vm.Builder((long) 2, 1, 1024, 1).build();

    @BeforeClass
    public static void onceExecutedBeforeAll() {
        ClusterState initialClusterState = new ClusterState();
        initialClusterState.setVms(new ArrayList<Vm>());
        initialClusterState.setHosts(new ArrayList<Host>());
        VmPlacementConfig.initialClusterState = initialClusterState;
    }
    
    @AfterClass
    public static void tearDown() {
        VmPlacementConfig.energyModeller = null; // It was mocked, we need to null it again so it does
                                                // interfere with other tests.
    }
    
    @Test
    public void scoreTest() {
        ClusterState testClusterState = getTestClusterState();
        List<Vm> vmsInHost1 = new ArrayList<>();
        vmsInHost1.add(vm1);
        List<Vm> vmsInHost2 = new ArrayList<>();
        vmsInHost2.add(vm2);
        
        mockEnergyModeller(vmsInHost1, vmsInHost2);

        ScoreCalculatorEnergy scoreCalculatorEnergy = new ScoreCalculatorEnergy();
        
        assertEquals(0, scoreCalculatorEnergy.calculateScore(testClusterState).getHardScore());
        assertEquals(-30, scoreCalculatorEnergy.calculateScore(testClusterState).getMediumScore());
    }
    
    private void mockEnergyModeller(List<Vm> vmsInHost1, List<Vm> vmsInHost2) {
        VmPlacementConfig.energyModeller = Mockito.mock(EnergyModeller.class);
        Mockito.when(VmPlacementConfig.energyModeller.getPowerConsumption(host1, vmsInHost1))
                .thenReturn(20.0);
        Mockito.when(VmPlacementConfig.energyModeller.getPowerConsumption(host2, vmsInHost2))
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
