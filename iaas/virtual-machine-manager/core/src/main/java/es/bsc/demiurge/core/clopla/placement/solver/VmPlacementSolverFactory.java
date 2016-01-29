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

package es.bsc.demiurge.core.clopla.placement.solver;

import com.google.common.collect.ImmutableMap;
import es.bsc.demiurge.core.clopla.domain.ConstructionHeuristic;
import es.bsc.demiurge.core.clopla.placement.config.VmPlacementConfig;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.constructionheuristic.ConstructionHeuristicSolverPhaseConfig;
import org.optaplanner.core.config.localsearch.LocalSearchSolverPhaseConfig;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.XmlSolverFactory;
import org.optaplanner.core.impl.score.director.simple.SimpleScoreCalculator;

import java.util.Map;

/**
 * This class creates an instance of an OptaPlanner SolverFactory from an instance of VmPlacementConfig.
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz (david.ortiz@bsc.es)
 */
public class VmPlacementSolverFactory {

    private static final String BASE_SOLVER_XML_PATH = "/vmplacementSolverConfig.xml";

    private Map<String, Class<? extends SimpleScoreCalculator>> policyScoreCalculatorImplementations;

	private static final Map<ConstructionHeuristic, ConstructionHeuristicSolverPhaseConfig.ConstructionHeuristicType>
            optaPlannerConstructionHeuristics =
            ImmutableMap.<ConstructionHeuristic, ConstructionHeuristicSolverPhaseConfig.ConstructionHeuristicType>
                    builder()
                    .put(ConstructionHeuristic.FIRST_FIT,
                            ConstructionHeuristicSolverPhaseConfig.ConstructionHeuristicType.FIRST_FIT)
                    .put(ConstructionHeuristic.FIRST_FIT_DECREASING,
                            ConstructionHeuristicSolverPhaseConfig.ConstructionHeuristicType.FIRST_FIT_DECREASING)
                    .put(ConstructionHeuristic.BEST_FIT,
                            ConstructionHeuristicSolverPhaseConfig.ConstructionHeuristicType.BEST_FIT)
                    .put(ConstructionHeuristic.BEST_FIT_DECREASING,
                            ConstructionHeuristicSolverPhaseConfig.ConstructionHeuristicType.BEST_FIT_DECREASING)
                    .build();
    
    private final VmPlacementConfig vmPlacementConfig;

	public VmPlacementSolverFactory(VmPlacementConfig vmPlacementConfig, Map<String, Class<? extends SimpleScoreCalculator>> policyScoreCalculatorImplementations) {
		this.vmPlacementConfig = vmPlacementConfig;
		this.policyScoreCalculatorImplementations = policyScoreCalculatorImplementations;
	}

	public SolverFactory getSolverFactory() {
        // The solver is built from an XML that contains a basic configuration.
        // This should be a bit simpler than building the solver from scratch.
        SolverFactory solverFactory = new XmlSolverFactory(BASE_SOLVER_XML_PATH);
        configureSolver(solverFactory.getSolverConfig(), vmPlacementConfig);
        return solverFactory;
    }

    /**
     * Modifies the given SolverConfig according to the given placement configuration.
     * This function sets the policy, the timeout, the construction heuristic, and the local search
     * algorithm for the given SolverConfig.
     *
     * @param solverConfig the instance of SolverConfig
     * @param vmPlacementConfig the configuration for the VM placement problem
     */
    private void configureSolver(SolverConfig solverConfig, VmPlacementConfig vmPlacementConfig) {
        configurePolicy(solverConfig, vmPlacementConfig);
        configureTimeout(solverConfig, vmPlacementConfig);
        configureConstructionHeuristic(solverConfig, vmPlacementConfig);
        configureLocalSearch(solverConfig, vmPlacementConfig);
    }

    private void configurePolicy(SolverConfig solverConfig, VmPlacementConfig vmPlacementConfig) {
        solverConfig.getScoreDirectorFactoryConfig().setSimpleScoreCalculatorClass(
                policyScoreCalculatorImplementations.get(vmPlacementConfig.getPolicy()));
    }

    private void configureTimeout(SolverConfig solverConfig, VmPlacementConfig vmPlacementConfig) {
        solverConfig.getTerminationConfig().setMaximumSecondsSpend((long) vmPlacementConfig.getTimeLimitSeconds());
    }

    private void configureConstructionHeuristic(SolverConfig solverConfig, VmPlacementConfig vmPlacementConfig) {
        // If we do not want to use a const. heuristic, remove the default one
        if (vmPlacementConfig.getConstructionHeuristic() == null) {
            solverConfig.getSolverPhaseConfigList().remove(0);
        }
        else {
            ConstructionHeuristicSolverPhaseConfig heuristicConfig =
                    (ConstructionHeuristicSolverPhaseConfig) solverConfig.getSolverPhaseConfigList().toArray()[0];
            heuristicConfig.setConstructionHeuristicType(optaPlannerConstructionHeuristics.get(
                    vmPlacementConfig.getConstructionHeuristic()));
        }
    }

    private void configureLocalSearch(SolverConfig solverConfig, VmPlacementConfig vmPlacementConfig) {
        // remove the local search alg included by default. It is in position 0 if the const.heuristic
        // was removed, and 1 otherwise
        solverConfig.getSolverPhaseConfigList().remove(solverConfig.getSolverPhaseConfigList().size() - 1);

        // Local search can be null if we are only interested in applying the construction step
        if (vmPlacementConfig.getLocalSearch() != null) {
            LocalSearchSolverPhaseConfig localSearchSolverPhaseConfig = new LocalSearchSolverPhaseConfig();
            localSearchSolverPhaseConfig.setAcceptorConfig(vmPlacementConfig.getLocalSearch().getAcceptorConfig());
            localSearchSolverPhaseConfig.setForagerConfig(vmPlacementConfig.getLocalSearch().getForagerConfig());
            solverConfig.getSolverPhaseConfigList().add(localSearchSolverPhaseConfig);
        }
    }
    
}
