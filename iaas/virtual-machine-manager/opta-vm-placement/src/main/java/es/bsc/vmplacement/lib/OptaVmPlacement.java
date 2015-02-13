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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
        List<ConstructionHeuristic> result = new ArrayList<>();
        result.addAll(Arrays.asList((ConstructionHeuristic.values())));
        return result;
    }

    @Override
    public List<LocalSearchAlgorithm> getLocalSearchAlgorithms() {
        List<LocalSearchAlgorithm> result = new ArrayList<>();
        result.add(new LocalSearchAlgorithm("Hill Climbing", Collections.<String>emptyList()));
        result.add(new LocalSearchAlgorithm("Late Acceptance", Arrays.asList("Size")));
        result.add(new LocalSearchAlgorithm("Late Simulated Annealing", Arrays.asList("Size", "Accepted Count Limit")));
        result.add(new LocalSearchAlgorithm("Simulated Annealing", Arrays.asList("Initial Hard Temperature",
                "Initial Soft Temperature")));
        result.add(new LocalSearchAlgorithm("Step Counting Hill Climbing", Arrays.asList("Size")));
        result.add(new LocalSearchAlgorithm("Tabu Search", Arrays.asList("Size", "Accepted Count Limit")));
        return result;
    }

}
