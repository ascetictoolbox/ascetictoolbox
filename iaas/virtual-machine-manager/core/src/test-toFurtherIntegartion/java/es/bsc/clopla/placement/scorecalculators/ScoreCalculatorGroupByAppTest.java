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

package es.bsc.demiurge.core.clopla.placement.scorecalculators;

import es.bsc.demiurge.core.clopla.domain.ClusterState;
import es.bsc.demiurge.core.clopla.domain.Host;
import es.bsc.demiurge.core.clopla.domain.Vm;
import es.bsc.demiurge.core.clopla.placement.config.VmPlacementConfig;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Mario Macias (github.com/mariomac), David Ortiz (david.ortiz@bsc.es)
 */
public class ScoreCalculatorGroupByAppTest {

    private final ScoreCalculatorGroupByApp scoreCalculatorGroupByApp = new ScoreCalculatorGroupByApp();

    @BeforeClass
    public static void onceExecutedBeforeAll() {
        ClusterState initialClusterState = new ClusterState();
        initialClusterState.setVms(new ArrayList<Vm>());
        initialClusterState.setHosts(new ArrayList<Host>());
        VmPlacementConfig.initialClusterState.set(initialClusterState);
    }

    @AfterClass
    public static void onceExecutedAfterAll() {
        VmPlacementConfig.initialClusterState.set(null);
    }
    
    @Test
    public void scoreTest() {
        ClusterState clusterState = getTestClusterState();
        assertEquals(2, scoreCalculatorGroupByApp.calculateScore(clusterState).getSoftScore(1));
        assertEquals(1, scoreCalculatorGroupByApp.calculateScore(clusterState).getSoftScore(0));
        assertEquals(-4, scoreCalculatorGroupByApp.calculateScore(clusterState).getHardScore(0));
    }

    private ClusterState getTestClusterState() {
        // Create hosts
        List<Host> hosts = new ArrayList<>();
        Host host1 = new Host((long) 1, "1", 8, 8192, 8, false);
        Host host2 = new Host((long) 2, "2", 4, 4096, 4, false);
        Host host3 = new Host((long) 3, "3", 2, 2048, 2, false);
        Host host4 = new Host((long) 4, "4", 1, 1024, 1, true);
        hosts.add(host1);
        hosts.add(host2);
        hosts.add(host3);
        hosts.add(host4);

        // Create VMs
        List<Vm> vms = new ArrayList<>();
        Vm vm1 = new Vm.Builder((long) 1, 1, 1024, 1).appId("app1").build();
        Vm vm2 = new Vm.Builder((long) 2, 1, 1024, 1).appId("app1").build();
        Vm vm3 = new Vm.Builder((long) 3, 5, 5120, 5).appId("app1").build();
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
