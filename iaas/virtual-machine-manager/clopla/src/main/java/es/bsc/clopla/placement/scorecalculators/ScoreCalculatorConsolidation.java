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

package es.bsc.clopla.placement.scorecalculators;

import es.bsc.clopla.domain.ClusterState;
import es.bsc.clopla.domain.Host;
import es.bsc.clopla.domain.Vm;
import es.bsc.clopla.placement.config.VmPlacementConfig;
import org.optaplanner.core.api.score.buildin.bendable.BendableScore;
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import org.optaplanner.core.impl.score.director.simple.SimpleScoreCalculator;

/**
 * This class defines the score used in the consolidation policy.
 * The score in this case contains 1 hard score and 4 levels of soft scores.
 * Hard score: overcapacity of the servers of the cluster 
 *             plus number of fixed VMs that were moved. (minimize)
 * Soft scores: 1) Number of hosts that are off. (maximize)
 *              2) Number of hosts that are idle. (maximize)
 *              3) Total unused CPU %. (minimize)
 *              4) Number of migrations needed from initial state (minimize)              
 *  
 * @author David Ortiz (david.ortiz@bsc.es)
 */
public class ScoreCalculatorConsolidation implements SimpleScoreCalculator<ClusterState> {

    @Override
    public HardMediumSoftScore calculateScore(ClusterState solution) {
        int hardScore = calculateHardScore(solution);
		int mediumScore = solution.countOffHosts() + solution.countIdleHosts();
        int softScore = -VmPlacementConfig.initialClusterState.get().countVmMigrationsNeeded(solution);

		if("1".equals(System.getenv("CLOPLA_DEBUG"))) {
			boolean sameHosts = true;
			Long firstHost = null;
			for(Vm vm : solution.getVms()) {
				if(firstHost == null) {
					firstHost = vm.getHost().getId();
				} else {
					if(vm.getHost().getId() != firstHost) {
						sameHosts = false;
						break;
					}
				}
			}

			if(sameHosts || softScore > 0 || mediumScore > 0) {
				System.out.println("****** SOLUTION " + solution.toString());
				System.out.println("Hard score: " + hardScore);
				System.out.println("medium score: " + mediumScore);
				System.out.print("soft score: " + softScore);
				System.out.println();
			}
		}

		return HardMediumSoftScore.valueOf(hardScore,mediumScore,softScore);
    }

    private int calculateHardScore(ClusterState solution) {
        return (int) (ScoreCalculatorCommon.getClusterOverCapacityScore(solution)
                + ScoreCalculatorCommon.getClusterPenaltyScoreForFixedVms(solution));
    }
    
}
