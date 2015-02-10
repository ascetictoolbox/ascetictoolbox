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
import org.optaplanner.core.impl.score.director.simple.SimpleScoreCalculator;

import java.util.List;

/**
 * @author David Ortiz (david.ortiz@bsc.es)
 */
public final class ScoreCalculatorGroupByApp implements SimpleScoreCalculator<ClusterState> {

    @Override
    public HardMediumSoftScore calculateScore(ClusterState solution) {
        return HardMediumSoftScore.valueOf(
                calculateHardScore(solution),
                calculateMediumScore(solution),
                calculateSoftScore(solution));
    }
    
    private int calculateHardScore(ClusterState solution) {
        return ScoreCalculatorCommon.getClusterOverCapacitySCoreWithPenaltyForFixedVms(solution);
    }

    private int calculateMediumScore(ClusterState solution) {
        return solution.countOffHosts();
    }

    private int calculateSoftScore(ClusterState solution) {
        int result = 0;
        for (Host host: solution.getHosts()) {
            double hostScore = 0;
            List<String> idsOfVmsOfHost = solution.getIdsOfAppsDeployedInHost(host);
            for (int i = 0; i < idsOfVmsOfHost.size(); ++i) {
                for (int j = 0; j < idsOfVmsOfHost.size(); ++j) {
                    if (idsOfVmsOfHost.get(i).equals(idsOfVmsOfHost.get(j)) && i != j) {
                        ++hostScore;
                    }
                }
            }
            result += hostScore;
        }
        return result;
    }

}
