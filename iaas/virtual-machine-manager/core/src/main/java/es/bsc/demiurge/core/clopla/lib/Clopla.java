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

import es.bsc.demiurge.core.clopla.domain.*;
import es.bsc.demiurge.core.clopla.placement.VmPlacementProblem;
import es.bsc.demiurge.core.clopla.placement.config.VmPlacementConfig;

import java.util.*;

/**
 * This class exposes the functionality of the library.
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz (david.ortiz@bsc.es)
 */
public class Clopla implements IClopla {

    @Override
    public ClusterState getBestSolution(List<Host> hosts, List<Vm> vms, VmPlacementConfig config) {
        return new VmPlacementProblem(hosts, vms, config).getBestSolution();
    }

    @Override
    public List<ConstructionHeuristic> getConstructionHeuristics() {
        return new ArrayList<>(Arrays.asList(ConstructionHeuristic.values()));
    }

    @Override
    public Map<LocalSearchHeuristic, List<LocalSearchHeuristicOption>> getLocalSearchAlgorithms() {
        Map<LocalSearchHeuristic, List<LocalSearchHeuristicOption>> result = new HashMap<>();
        result.put(LocalSearchHeuristic.HILL_CLIMBING, 
                Collections.<LocalSearchHeuristicOption>emptyList());
        result.put(LocalSearchHeuristic.LATE_ACCEPTANCE, 
                Arrays.asList(LocalSearchHeuristicOption.SIZE));
        result.put(LocalSearchHeuristic.LATE_SIMULATED_ANNEALING, 
                Arrays.asList(LocalSearchHeuristicOption.SIZE,
                        LocalSearchHeuristicOption.ACCEPTED_COUNT_LIMIT));
        result.put(LocalSearchHeuristic.SIMULATED_ANNEALING,
                Arrays.asList(LocalSearchHeuristicOption.INITIAL_HARD_TEMPERATURE,
                        LocalSearchHeuristicOption.INITIAL_SOFT_TEMPERATURE));
        result.put(LocalSearchHeuristic.STEP_COUNTING_HILL_CLIMBING,
                Arrays.asList(LocalSearchHeuristicOption.SIZE));
        result.put(LocalSearchHeuristic.TABU_SEARCH,
                Arrays.asList(LocalSearchHeuristicOption.SIZE,
                        LocalSearchHeuristicOption.ACCEPTED_COUNT_LIMIT));
        return result;
    }

}
