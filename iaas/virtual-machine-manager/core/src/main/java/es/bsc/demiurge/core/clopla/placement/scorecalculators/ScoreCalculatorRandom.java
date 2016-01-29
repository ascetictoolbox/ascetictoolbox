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
import es.bsc.demiurge.core.clopla.placement.config.VmPlacementConfig;
import org.optaplanner.core.api.score.buildin.bendable.BendableScore;
import org.optaplanner.core.impl.score.director.simple.SimpleScoreCalculator;

import java.util.Random;

/**
 * This class defines the score used in the random policy.
 * The score in this case contains 1 hard score and 3 levels of soft scores.
 * Hard score: overcapacity of the servers of the cluster
 *             plus number of fixed VMs that were moved. (minimize)
 * Soft scores: 1) Number of hosts that are off. (maximize)
 *              2) random value. (maximize)
 *              3) Number of migrations needed from initial state (minimize)
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz (david.ortiz@bsc.es)
 */
public class ScoreCalculatorRandom implements SimpleScoreCalculator<ClusterState> {

    private final Random rand = new Random();
    private final static int POSSIBLE_SCORES = 1000000; // Range of values for the random scores

    @Override
    public BendableScore calculateScore(ClusterState solution) {
        int[] hardScores = { calculateHardScore(solution) };
        int[] softScores = {
                solution.countOffHosts(),
                rand.nextInt(POSSIBLE_SCORES),
                -VmPlacementConfig.initialClusterState.get().countVmMigrationsNeeded(solution)};
        return BendableScore.valueOf(hardScores, softScores);
    }

    private int calculateHardScore(ClusterState solution) {
        return (int) (ScoreCalculatorCommon.getClusterOverCapacityScore(solution)
                + ScoreCalculatorCommon.getClusterPenaltyScoreForFixedVms(solution));
    }

}
