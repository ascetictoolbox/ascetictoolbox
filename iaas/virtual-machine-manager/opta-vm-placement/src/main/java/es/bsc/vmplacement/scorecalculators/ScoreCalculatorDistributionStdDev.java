package es.bsc.vmplacement.scorecalculators;

import es.bsc.vmplacement.domain.ClusterState;
import es.bsc.vmplacement.domain.Host;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import org.optaplanner.core.impl.score.director.simple.SimpleScoreCalculator;

public final class ScoreCalculatorDistributionStdDev implements SimpleScoreCalculator<ClusterState> {

    protected final static int PENALTY_FOR_MOVING_FIXED_VMS = 10000;

    @Override
    public Score calculateScore(ClusterState solution) {
        return HardMediumSoftScore.valueOf(
                calculateHardScore(solution),
                calculateMediumScore(solution),
                calculateSoftScore(solution));
    }

    private int calculateHardScore(ClusterState solution) {
        int result = 0;
        for (Host host: solution.getHosts()) {
            if (host.missingFixedVMs(solution.getVms())) {
                result -= PENALTY_FOR_MOVING_FIXED_VMS;
            }
            result += host.getOverCapacityScore(solution.getVms());
        }
        return result;
    }

    private int calculateMediumScore(ClusterState solution) {
        int result = 0;
        for (Host host: solution.getHosts()) {
            if (!solution.hostIsIdle(host)) {
                ++result;
            }
        }
        return result;
    }

    private int calculateSoftScore(ClusterState solution) {
        double averageCpus = solution.avgCpusAssignedPerHost();
        double temp = 0;
        for(Host host: solution.getHosts()) {
            temp += (averageCpus - solution.getVmsDeployedInHost(host).size()) *
                    (averageCpus - solution.getVmsDeployedInHost(host).size());
        }
        double variance = temp/solution.getHosts().size();
        return -(int) Math.sqrt(variance);
    }



}
