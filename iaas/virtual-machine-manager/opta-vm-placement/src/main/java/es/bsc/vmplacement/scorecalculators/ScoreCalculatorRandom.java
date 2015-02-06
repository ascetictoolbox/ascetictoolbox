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
import es.bsc.vmplacement.domain.Host;
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.impl.score.director.simple.SimpleScoreCalculator;

import java.util.Random;

/**
 * @author David Ortiz (david.ortiz@bsc.es)
 */
public final class ScoreCalculatorRandom implements SimpleScoreCalculator<ClusterState> {

    private final Random rand = new Random();
    private final static int POSSIBLE_SCORES = 1000000; // Range of values for the random scores

    @Override
    public HardMediumSoftScore calculateScore(ClusterState solution) {
        return HardMediumSoftScore.valueOf(
                calculateHardScore(solution),
                calculateMediumScore(solution),
                calculateSoftScore());
    }

    private int calculateHardScore(ClusterState solution) {
        return ScoreCalculatorCommon.getClusterOverCapacitySCoreWithPenaltyForFixedVms(solution);
    }

    private int calculateMediumScore(ClusterState solution) {
        return solution.countOffHosts();
    }
    
    private int calculateSoftScore() {
        return rand.nextInt(POSSIBLE_SCORES);
    }

}
