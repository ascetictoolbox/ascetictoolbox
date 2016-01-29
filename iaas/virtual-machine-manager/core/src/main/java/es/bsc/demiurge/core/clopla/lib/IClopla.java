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
import es.bsc.demiurge.core.clopla.domain.*;
import es.bsc.demiurge.core.clopla.placement.config.VmPlacementConfig;
import es.bsc.demiurge.core.clopla.domain.*;

import java.util.List;
import java.util.Map;

/**
 * @author Mario Macias (github.com/mariomac), David Ortiz (david.ortiz@bsc.es)
 */
public interface IClopla {

    /**
     * Given a list of hosts, a list of VMs, applies the best placement that can be found according to the
     * configuration parameters specified.
     *
     * @param hosts the hosts
     * @param vms the VMs
     * @param config the configuration parameters for the placement
     * @return the state of the cluster after applying the best placement that could be found
     */
    ClusterState getBestSolution(List<Host> hosts, List<Vm> vms, VmPlacementConfig config);

    /**
     * Returns a list of the construction heuristics that are supported by the library.
     *
     * @return List of the construction heuristics
     */
    List<ConstructionHeuristic> getConstructionHeuristics();

    /**
     * Returns a map with the local search algorithms supported by the library and the options they use.
     *
     * @return the map of local search algorithms
     */
    Map<LocalSearchHeuristic, List<LocalSearchHeuristicOption>> getLocalSearchAlgorithms();

}
