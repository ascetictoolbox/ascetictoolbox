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

package es.bsc.demiurge.core.clopla.lib;

import es.bsc.demiurge.core.clopla.placement.config.Policy;
import es.bsc.demiurge.core.clopla.placement.config.VmPlacementConfig;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * @author Mario Macias (github.com/mariomac), David Ortiz (david.ortiz@bsc.es)
 */
public class CloplaTest {
    
    private final IClopla clopla = new Clopla();
    
    @Test
    public void getConstructionHeuristics() {
        assertTrue(clopla.getConstructionHeuristics()
                .containsAll(Arrays.asList(ConstructionHeuristic.values())));
    }
    
    @Test
    public void getLocalSearchAlgorithms() {
        Map<LocalSearchHeuristic, List<LocalSearchHeuristicOption>> expectedLocalSearchAlgorithms
                = new HashMap<>();
        expectedLocalSearchAlgorithms.put(
                LocalSearchHeuristic.HILL_CLIMBING,
                Collections.<LocalSearchHeuristicOption>emptyList());
        expectedLocalSearchAlgorithms.put(
                LocalSearchHeuristic.LATE_ACCEPTANCE,
                Arrays.asList(LocalSearchHeuristicOption.SIZE));
        expectedLocalSearchAlgorithms.put(
                LocalSearchHeuristic.LATE_SIMULATED_ANNEALING,
                Arrays.asList(LocalSearchHeuristicOption.SIZE, 
                        LocalSearchHeuristicOption.ACCEPTED_COUNT_LIMIT));
        expectedLocalSearchAlgorithms.put(
                LocalSearchHeuristic.SIMULATED_ANNEALING,
                Arrays.asList(LocalSearchHeuristicOption.INITIAL_HARD_TEMPERATURE,
                        LocalSearchHeuristicOption.INITIAL_SOFT_TEMPERATURE));
        expectedLocalSearchAlgorithms.put(
                LocalSearchHeuristic.STEP_COUNTING_HILL_CLIMBING,
                Arrays.asList(LocalSearchHeuristicOption.SIZE));
        expectedLocalSearchAlgorithms.put(
                LocalSearchHeuristic.TABU_SEARCH,
                Arrays.asList(LocalSearchHeuristicOption.SIZE, 
                        LocalSearchHeuristicOption.ACCEPTED_COUNT_LIMIT));
        assertTrue(clopla.getLocalSearchAlgorithms().equals(expectedLocalSearchAlgorithms));
    }
    
    @Test
    public void firstFitTest() {
        // This is a simple test to make sure that the first fit construction heuristic is working as expected.
        // In this example there are 2 hosts and 3 VMs. All the hosts have the same RAM and Disk. Similarly,
        // all the VMs demand the same RAM and disk. This allows us to simplify the test by focusing just on the CPUs.
        // host1 has 2 CPUs and host2 has 5. 
        // The policy selected is consolidation.
        // vm1 demands 4 CPUs, vm2 demands 2 CPUs, and vm3 demands 3 CPUs.
        // vm1: the system tries to deploy vm1 in host1. It does not fit. The system tries in host2 and it fits.
        // vm2: the system tries to deploy vm2 in host1, and it fits. It does not fit in host2
        // vm3: the system tries to deploy vm3 in host1, and host2, but it does not fit in any of them. vm3 is deployed
        //      in host2 because the over commit is smaller in that host (see the consolidation score calculator).
        
        // Initialize hosts
        List<Host> hosts = new ArrayList<>();
        Host host1 = new Host((long) 1, "1", 2, 8192, 8, false);
        Host host2 = new Host((long) 2, "2", 5, 8192, 8, false);
        hosts.add(host1);
        hosts.add(host2);
        
        // Initialize VMs
        List<Vm> vms = new ArrayList<>();
        Vm vm1 = new Vm.Builder((long) 1, 4, 1024, 1).build();
        Vm vm2 = new Vm.Builder((long) 2, 2, 1024, 1).build();
        Vm vm3 = new Vm.Builder((long) 3, 3, 1024, 1).build();
        vms.add(vm1);
        vms.add(vm2);
        vms.add(vm3);

        // Set the configuration
        VmPlacementConfig config = new VmPlacementConfig.Builder(
                Policy.CONSOLIDATION, 5, ConstructionHeuristic.FIRST_FIT, null, false).build();

        // Get the best planning solution
        ClusterState clusterState = clopla.getBestSolution(hosts, vms, config);
        List<Vm> solutionVms = clusterState.getVms();
        
        // Check that the VMs have been deployed applying first fit
        assertEquals(host2, findVmById(solutionVms, 1).getHost());
        assertEquals(host1, findVmById(solutionVms, 2).getHost());
        assertEquals(host2, findVmById(solutionVms, 3).getHost());
    }
    
    @Test
    public void firstFitDecreasingTest() {
        // This is a simple test to make sure that the first fit decreasing construction heuristic is working correctly.
        // In this example there are 2 hosts and 3 VMs. All the hosts have the same RAM and Disk. Similarly,
        // all the VMs demand the same RAM and disk. This allows us to simplify the test by focusing just on the CPUs.
        // Both hosts have 3 CPUs.
        // The policy selected is consolidation.
        // vm1 demands 1 CPU, vm2 demands 2 CPUs, and vm3 demands 3 CPUs.
        // The first fit decreasing heuristics orders the VMs according to their size. Therefore, the 3 VMs are going
        // to be scheduled in this order: vm3, vm2, vm1.
        // vm3: fits in both hosts. Gets scheduled in the first one (host1).
        // vm2: fits only in host2.
        // vm1: fits only in host1.
        
        // Initialize hosts
        List<Host> hosts = new ArrayList<>();
        Host host1 = new Host((long) 1, "1", 3, 8192, 8, false);
        Host host2 = new Host((long) 2, "2", 3, 8192, 8, false);
        hosts.add(host1);
        hosts.add(host2);

        // Initialize VMs
        List<Vm> vms = new ArrayList<>();
        Vm vm1 = new Vm.Builder((long) 1, 1, 1024, 1).build();
        Vm vm2 = new Vm.Builder((long) 2, 2, 1024, 1).build();
        Vm vm3 = new Vm.Builder((long) 3, 3, 1024, 1).build();
        vms.add(vm1);
        vms.add(vm2);
        vms.add(vm3);

        // Set the configuration
        VmPlacementConfig config = new VmPlacementConfig.Builder(
                Policy.CONSOLIDATION, 5, ConstructionHeuristic.FIRST_FIT_DECREASING, null, false).build();

        // Get the best planning solution
        ClusterState clusterState = clopla.getBestSolution(hosts, vms, config);
        List<Vm> solutionVms = clusterState.getVms();

        // Check that the VMs have been deployed applying first fit decreasing
        assertEquals(host2, findVmById(solutionVms, 1).getHost());
        assertEquals(host2, findVmById(solutionVms, 2).getHost());
        assertEquals(host1, findVmById(solutionVms, 3).getHost());
    }
    
    @Test
    public void bestFitTest() {
        // TODO
    }
    
    @Test
    public void bestFitDecreasingTest() {
        // TODO
    }
    
    @Test
    public void firstFitDeploysLastInOffServer() {
        // Initialize hosts
        List<Host> hosts = new ArrayList<>();
        Host host1 = new Host((long) 1, "1", 1, 8192, 8, false);
        Host host2 = new Host((long) 2, "2", 1, 8192, 8, true);
        Host host3 = new Host((long) 3, "3", 1, 8192, 8, false);
        hosts.add(host1);
        hosts.add(host2);
        hosts.add(host3);

        // Initialize VMs
        List<Vm> vms = new ArrayList<>();
        Vm vm1 = new Vm.Builder((long) 1, 1, 1024, 1).build();
        Vm vm2 = new Vm.Builder((long) 2, 1, 1024, 1).build();
        vms.add(vm1);
        vms.add(vm2);

        // Set the configuration
        VmPlacementConfig config = new VmPlacementConfig.Builder(
                Policy.CONSOLIDATION, 5, ConstructionHeuristic.FIRST_FIT, null, false).build();

        // Get the best planning solution
        ClusterState clusterState = clopla.getBestSolution(hosts, vms, config);
        List<Vm> solutionVms = clusterState.getVms();

        // Check that the none of the VMs have been deployed in host2, the one that was off.
        assertNotEquals(host2, findVmById(solutionVms, 1).getHost());
        assertNotEquals(host2, findVmById(solutionVms, 2).getHost());
    }
    
    @Test
    public void bestFirstDeploysLastInOffServer() {
        // Initialize hosts
        List<Host> hosts = new ArrayList<>();
        Host host1 = new Host((long) 1, "1", 1, 8192, 8, false);
        Host host2 = new Host((long) 2, "2", 1, 8192, 8, true);
        Host host3 = new Host((long) 3, "3", 1, 8192, 8, false);
        hosts.add(host1);
        hosts.add(host2);
        hosts.add(host3);
        
        // Initialize VMs
        List<Vm> vms = new ArrayList<>();
        Vm vm1 = new Vm.Builder((long) 1, 1, 1024, 1).build();
        Vm vm2 = new Vm.Builder((long) 2, 1, 1024, 1).build();
        vms.add(vm1);
        vms.add(vm2);

        // Set the configuration
        VmPlacementConfig config = new VmPlacementConfig.Builder(
                Policy.CONSOLIDATION, 5, ConstructionHeuristic.BEST_FIT_DECREASING, null, false).build();

        // Get the best planning solution
        ClusterState clusterState = clopla.getBestSolution(hosts, vms, config);
        List<Vm> solutionVms = clusterState.getVms();

        // Check that the none of the VMs have been deployed in host2, the one that was off.
        assertNotEquals(host2, findVmById(solutionVms, 1).getHost());
        assertNotEquals(host2, findVmById(solutionVms, 2).getHost());
    }
    
    private Vm findVmById(List<Vm> vms, long id) {
        for (Vm vm: vms) {
            if (vm.getId() == id) {
                return vm;
            }
        }
        return null;
    }
}
