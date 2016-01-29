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
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Mario Macias (github.com/mariomac), David Ortiz (david.ortiz@bsc.es)
 */
public class ScoreCalculatorCommonTest {
    
    private static final double DOUBLE_COMPARISON_DELTA = 0.1;
    
    @Test
    public void overcapacityScoreIsPositiveWhenThereIsOverBooking() {
        assertEquals(-12, ScoreCalculatorCommon.getClusterOverCapacityScore(
                getClusterWithOverbooking()), DOUBLE_COMPARISON_DELTA);
    }

    @Test
    public void overcapacityScoreIsZeroWhenThereIsNoOverBooking() {
        assertEquals(0, ScoreCalculatorCommon.getClusterOverCapacityScore(
                getClusterWithoutOverbooking()), DOUBLE_COMPARISON_DELTA);
    }

    @Test
    public void penaltyScoreForFixedVmsIsZeroWhenFixedVmsHaveNotBeenMoved() {
        assertEquals(0, ScoreCalculatorCommon.getClusterPenaltyScoreForFixedVms(
                getClusterWithFixedVmsNotMigrated()), DOUBLE_COMPARISON_DELTA);
    }
    
    @Test
    public void penaltyScoreForFixedVmsIsPositiveWhenFixedVmsHaveBeenMoved() {
        assertEquals(-ScoreCalculatorCommon.PENALTY_FOR_MOVING_FIXED_VMS,
                ScoreCalculatorCommon.getClusterPenaltyScoreForFixedVms(getClusterWithFixedVmsMigrated()),
                DOUBLE_COMPARISON_DELTA);
    }
    
    @Test
    public void penaltyScoreForFixedVmsIsZeroWhenThereAreNotFixedVms() {
        assertEquals(0, ScoreCalculatorCommon.getClusterPenaltyScoreForFixedVms(
                getClusterWithoutFixedVms()), DOUBLE_COMPARISON_DELTA);
    }
    
    private ClusterState getClusterWithOverbooking() {
        List<Host> hosts = new ArrayList<>();
        Host host1 = new Host((long) 1, "1", 1, 1024, 1, false);
        Host host2 = new Host((long) 2, "2", 1, 1024, 1, false);
        hosts.add(host1);
        hosts.add(host2);

        List<Vm> vms = new ArrayList<>();
        Vm vm1 = new Vm.Builder((long) 1, 2, 2048, 2).build();
        Vm vm2 = new Vm.Builder((long) 2, 2, 2048, 2).build();
        vm1.setHost(host1);
        vm2.setHost(host2);
        vms.add(vm1);
        vms.add(vm2);

        ClusterState result = new ClusterState();
        result.setHosts(hosts);
        result.setVms(vms);
        return result;
    }
    
    private ClusterState getClusterWithoutOverbooking() {
        List<Host> hosts = new ArrayList<>();
        Host host1 = new Host((long) 1, "1", 2, 2048, 2, false);
        hosts.add(host1);

        List<Vm> vms = new ArrayList<>();
        Vm vm1 = new Vm.Builder((long) 1, 1, 1024, 1).build();
        vm1.setHost(host1);
        vms.add(vm1);

        ClusterState result = new ClusterState();
        result.setHosts(hosts);
        result.setVms(vms);
        return result;
    }
    
    private ClusterState getClusterWithFixedVmsMigrated() {
        List<Host> hosts = new ArrayList<>();
        Host host1 = new Host((long) 1, "1", 2, 2048, 2, false);
        Host host2 = new Host((long) 2, "2", 1, 1024, 1, false);
        hosts.add(host1);
        hosts.add(host2);

        List<Vm> vms = new ArrayList<>();
        Vm vm1 = new Vm.Builder((long) 1, 1, 1024, 1).build();
        vm1.setHost(host2);
        vms.add(vm1);

        host1.addFixedVm((long) 1);

        ClusterState result = new ClusterState();
        result.setHosts(hosts);
        result.setVms(vms);
        return result;
    }
    
    private ClusterState getClusterWithFixedVmsNotMigrated() {
        List<Host> hosts = new ArrayList<>();
        Host host1 = new Host((long) 1, "1", 2, 2048, 2, false);
        hosts.add(host1);

        List<Vm> vms = new ArrayList<>();
        Vm vm1 = new Vm.Builder((long) 1, 1, 1024, 1).build();
        vm1.setHost(host1);
        vms.add(vm1);
        
        host1.addFixedVm((long) 1);

        ClusterState result = new ClusterState();
        result.setHosts(hosts);
        result.setVms(vms);
        return result;
    }
    
    private ClusterState getClusterWithoutFixedVms() {
        return getClusterWithoutOverbooking();
    }
    
}
