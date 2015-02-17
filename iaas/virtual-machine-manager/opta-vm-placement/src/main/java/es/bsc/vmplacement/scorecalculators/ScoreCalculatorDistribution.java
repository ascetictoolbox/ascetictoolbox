package es.bsc.vmplacement.scorecalculators;

import es.bsc.vmplacement.domain.ClusterState;
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import org.optaplanner.core.impl.score.director.simple.SimpleScoreCalculator;

/**
 * This class defines the score used in the distribution policy.
 * The score in this case contains a hard, a medium, and a soft score.
 * Hard score: overcapacity of the servers of the cluster 
 *             plus number of fixed VMs that were moved. (minimize)
 * Medium score: Number of hosts that are not idle. (maximize)
 * Soft score: std dev of the CPUs assigned to the hosts of the cluster. (minimize)
 *
 * @author David Ortiz (david.ortiz@bsc.es)
 */
public class ScoreCalculatorDistribution implements SimpleScoreCalculator<ClusterState> {

    @Override
    public HardMediumSoftScore calculateScore(ClusterState solution) {
        return HardMediumSoftScore.valueOf(
                calculateHardScore(solution),
                calculateMediumScore(solution),
                calculateSoftScore(solution));
    }

    private int calculateHardScore(ClusterState solution) {
        return (int) (ScoreCalculatorCommon.getClusterOverCapacityScore(solution)
                + ScoreCalculatorCommon.getClusterPenaltyScoreForFixedVms(solution));
    }

    private int calculateMediumScore(ClusterState solution) {
        return solution.countNonIdleHosts();
    }

    private int calculateSoftScore(ClusterState solution) {
        return -((int) solution.calculateStdDevCpusAssignedPerHost());
    }

}
