package es.bsc.vmplacement.scorecalculators;

import es.bsc.vmplacement.domain.ClusterState;
import es.bsc.vmplacement.domain.Host;

/**
 * This class includes several score functions that are common to different score calculators.
 *
 * @author David Ortiz (david.ortiz@bsc.es)
 */
public final class ScoreCalculatorCommon {

    // Suppress default constructor for non-instantiability
    private ScoreCalculatorCommon() {
        throw new AssertionError();
    }

    private final static int PENALTY_FOR_MOVING_FIXED_VMS = 10000;

    public static int getClusterOverCapacitySCoreWithPenaltyForFixedVms(ClusterState solution) {
        double result = 0;
        for (Host host: solution.getHosts()) {
            if (host.missingFixedVMs(solution.getVms())) {
                result -= PENALTY_FOR_MOVING_FIXED_VMS;
            }
            result += host.getOverCapacityScore(solution.getVms());
        }
        return (int) result;
    }

}
