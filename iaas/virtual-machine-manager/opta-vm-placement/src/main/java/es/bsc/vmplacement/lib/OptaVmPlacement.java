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

package es.bsc.vmplacement.lib;

import es.bsc.vmplacement.domain.*;
import es.bsc.vmplacement.placement.VmPlacementProblem;
import es.bsc.vmplacement.placement.config.VmPlacementConfig;

import java.util.*;

/**
 * This class exposes the functionality of the library.
 *
 * @author David Ortiz (david.ortiz@bsc.es)
 */
public class OptaVmPlacement implements IOptaVmPlacement {

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
