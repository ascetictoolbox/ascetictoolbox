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
import es.bsc.vmplacement.domain.Vm;
import es.bsc.vmplacement.modellers.energy.EnergyModel;
import es.bsc.vmplacement.modellers.price.PriceModel;
import es.bsc.vmplacement.placement.config.VmPlacementConfig;

import java.util.List;

/**
 * @author David Ortiz (david.ortiz@bsc.es)
 */
public class ScoreCalculatorPrice extends ScoreCalculator {

    private final PriceModel priceModel = VmPlacementConfig.priceModel;
    private final EnergyModel energyModel = VmPlacementConfig.energyModel;

    @Override
    protected double calculateHardScoreForHost(Host host, List<Vm> vms) {
        if (host.missingFixedVMs(vms)) {
            return -PENALTY_FOR_MOVING_FIXED_VMS;
        }
        return host.getOverCapacityScore(vms);
    }

    @Override
    protected double calculateSoftScoreForHost(Host host, ClusterState clusterState) {
        return -priceModel.getCost(host, clusterState.getVmsDeployedInHost(host), energyModel);
    }

}
