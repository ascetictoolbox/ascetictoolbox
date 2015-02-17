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

package es.bsc.vmplacement.scorecalculators;

import es.bsc.vmplacement.domain.ClusterState;
import org.optaplanner.core.api.score.buildin.bendable.BendableScore;
import org.optaplanner.core.impl.score.director.simple.SimpleScoreCalculator;


/**
 * This class defines the score used in the consolidation policy.
 * The score in this case contains 1 hard score and 3 levels of soft scores.
 * Hard score: overcapacity of the servers of the cluster 
 *             plus number of fixed VMs that were moved. (minimize)
 * Soft scores: 1) Number of hosts that are off. (maximize)
 *              2) Number of hosts that are idle. (maximize)
 *              3) Total unused CPU %. (minimize)
 *  
 * @author David Ortiz (david.ortiz@bsc.es)
 */
public class ScoreCalculatorConsolidation implements SimpleScoreCalculator<ClusterState> {

    @Override
    public BendableScore calculateScore(ClusterState solution) {
        int[] hardScores = { calculateHardScore(solution) };
        int[] softScores = {
                calculateSoftScore1(solution), 
                calculateSoftScore2(solution), 
                calculateSoftScore3(solution)};
        return BendableScore.valueOf(hardScores, softScores);
    }

    private int calculateHardScore(ClusterState solution) {
        return (int) (ScoreCalculatorCommon.getClusterOverCapacityScore(solution)
                + ScoreCalculatorCommon.getClusterPenaltyScoreForFixedVms(solution));
    }

    private int calculateSoftScore1(ClusterState solution) {
        return solution.countOffHosts();
    }

    private int calculateSoftScore2(ClusterState solution) {
        return solution.countIdleHosts();
    }

    private int calculateSoftScore3(ClusterState solution) {
        return -solution.calculateCumulativeUnusedCpuPerc();
    }
}
