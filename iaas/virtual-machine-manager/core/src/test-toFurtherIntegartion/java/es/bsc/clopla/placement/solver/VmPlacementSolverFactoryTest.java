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

import es.bsc.demiurge.core.clopla.domain.ConstructionHeuristic;
import es.bsc.demiurge.core.clopla.placement.config.Policy;
import es.bsc.demiurge.core.clopla.placement.config.VmPlacementConfig;
import es.bsc.demiurge.core.clopla.placement.config.localsearch.SimulatedAnnealing;
import es.bsc.demiurge.core.clopla.placement.scorecalculators.ScoreCalculatorDistribution;
import org.junit.Test;
import org.optaplanner.core.config.constructionheuristic.ConstructionHeuristicSolverPhaseConfig;
import org.optaplanner.core.config.localsearch.LocalSearchSolverPhaseConfig;
import org.optaplanner.core.config.localsearch.decider.acceptor.AcceptorConfig;
import org.optaplanner.core.config.localsearch.decider.forager.ForagerConfig;
import org.optaplanner.core.config.solver.SolverConfig;

import static org.junit.Assert.assertEquals;

/**
 * @author Mario Macias (github.com/mariomac), David Ortiz (david.ortiz@bsc.es)
 */
public class VmPlacementSolverFactoryTest {
    
    @Test
    public void getTestFactoryConfiguresAccordingToConfig() {
        // Instantiate solver factory and get its configuration
        VmPlacementSolverFactory vmPlacementSolverFactory = 
                new VmPlacementSolverFactory(getTestVmPlacementConfig());
        SolverConfig solverConfig = vmPlacementSolverFactory.getSolverFactory().getSolverConfig();
        
        // Check policy
        assertEquals(ScoreCalculatorDistribution.class,
                solverConfig.getScoreDirectorFactoryConfig().getSimpleScoreCalculatorClass());
        
        // Check timeout
        assertEquals(60, (long) solverConfig.getTerminationConfig().getMaximumSecondsSpend());

        // Check construction heuristic
        ConstructionHeuristicSolverPhaseConfig constructionHeuristicSolverPhaseConfig =
                (ConstructionHeuristicSolverPhaseConfig) (solverConfig.getSolverPhaseConfigList().toArray()[0]);
        assertEquals(ConstructionHeuristicSolverPhaseConfig.ConstructionHeuristicType.FIRST_FIT_DECREASING,
                constructionHeuristicSolverPhaseConfig.getConstructionHeuristicType());
        
        // Check local search heuristic
        LocalSearchSolverPhaseConfig localSearchSolverPhaseConfig =
                (LocalSearchSolverPhaseConfig) solverConfig.getSolverPhaseConfigList().toArray()[1];
        AcceptorConfig acceptorConfig = localSearchSolverPhaseConfig.getAcceptorConfig();
        ForagerConfig foragerConfig = localSearchSolverPhaseConfig.getForagerConfig();
        assertEquals("10hard/20soft", acceptorConfig.getSimulatedAnnealingStartingTemperature());
        assertEquals(1, (long) foragerConfig.getAcceptedCountLimit());
        
    }
    
    private VmPlacementConfig getTestVmPlacementConfig() {
        return new VmPlacementConfig.Builder(
                Policy.DISTRIBUTION,
                60,
                ConstructionHeuristic.FIRST_FIT_DECREASING,
                new SimulatedAnnealing(10, 20),
                false).build();
    }
    
}