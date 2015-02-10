package es.bsc.vmplacement.scorecalculators;

import es.bsc.vmplacement.domain.ClusterState;
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import org.optaplanner.core.impl.score.director.simple.SimpleScoreCalculator;

public final class ScoreCalculatorDistribution implements SimpleScoreCalculator<ClusterState> {

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
        return solution.countNonIdleHosts();
    }

    private int calculateSoftScore(ClusterState solution) {
        return -((int) solution.calculateStdDevCpusAssignedPerHost());
    }

}
