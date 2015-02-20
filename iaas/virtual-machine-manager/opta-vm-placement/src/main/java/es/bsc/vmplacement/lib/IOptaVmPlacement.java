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
import es.bsc.vmplacement.placement.config.VmPlacementConfig;

import java.util.List;
import java.util.Map;

/**
 * @author David Ortiz (david.ortiz@bsc.es)
 */
public interface IOptaVmPlacement {

    /**
     * Given a list of hosts, a list of VMs, applies the best placement that can be found according to the
     * configuration parameters specified.
     *
     * @param hosts the hosts
     * @param vms the VMs
     * @param config the configuration parameters for the placement
     * @return the state of the cluster after applying the best placement that could be found
     */
    public ClusterState getBestSolution(List<Host> hosts, List<Vm> vms, VmPlacementConfig config);

    /**
     * Returns a list of the construction heuristics that are supported by the library.
     *
     * @return List of the construction heuristics
     */
    public List<ConstructionHeuristic> getConstructionHeuristics();

    /**
     * Returns a map with the local search algorithms supported by the library and the options they use.
     *
     * @return the map of local search algorithms
     */
    public Map<LocalSearchHeuristic, List<LocalSearchHeuristicOption>> getLocalSearchAlgorithms();

}
