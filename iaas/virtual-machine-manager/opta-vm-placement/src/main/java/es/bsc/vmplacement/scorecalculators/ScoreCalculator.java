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
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.impl.score.director.simple.SimpleScoreCalculator;

import java.util.List;

/**
 * @author David Ortiz (david.ortiz@bsc.es)
 */
public abstract class ScoreCalculator implements SimpleScoreCalculator<ClusterState> {

    protected final static int PENALTY_FOR_MOVING_FIXED_VMS = 10000;

    @Override
    public HardSoftScore calculateScore(ClusterState solution) {
        double hardScore = 0;
        double softScore = 0;
        for (Host host: solution.getHosts()) {
            hardScore += calculateHardScoreForHost(host, solution.getVms());
            softScore += calculateSoftScoreForHost(host, solution);
        }
        return HardSoftScore.valueOf((int) hardScore, (int) softScore);
    }

    abstract protected double calculateHardScoreForHost(Host host, List<Vm> vms);

    abstract protected double calculateSoftScoreForHost(Host host, ClusterState clusterState);

}
