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
import es.bsc.vmplacement.modellers.EnergyModeller;
import es.bsc.vmplacement.modellers.PriceModeller;
import es.bsc.vmplacement.placement.config.VmPlacementConfig;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.impl.score.director.simple.SimpleScoreCalculator;

/**
 * This class defines the score used in the price policy.
 * The score in this case contains a hard score and a soft score.
 * Hard score: overcapacity of the servers of the cluster
 *             plus number of fixed VMs that were moved. (minimize)
 * Soft score: price of running the VMs in the hosts indicated. (minimize)
 *
 * @author David Ortiz (david.ortiz@bsc.es)
 */
public final class ScoreCalculatorPrice implements SimpleScoreCalculator<ClusterState> {

    private final PriceModeller priceModeller = VmPlacementConfig.priceModeller;
    private final EnergyModeller energyModeller = VmPlacementConfig.energyModeller;

    @Override
    public HardSoftScore calculateScore(ClusterState solution) {
        return HardSoftScore.valueOf(
                calculateHardScore(solution),
                calculateSoftScore(solution));
    }

    private int calculateHardScore(ClusterState solution) {
        return (int) (ScoreCalculatorCommon.getClusterOverCapacityScore(solution)
                + ScoreCalculatorCommon.getClusterPenaltyScoreForFixedVms(solution));
    }

    private int calculateSoftScore(ClusterState solution) {
        double result = 0;
        for (Host host: solution.getHosts()) {
            result -= priceModeller.getCost(host, solution.getVmsDeployedInHost(host), energyModeller);
        }
        return (int) result;
    }

}
