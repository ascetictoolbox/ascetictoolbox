package es.bsc.vmplacement.scorecalculators;

import es.bsc.vmplacement.domain.ClusterState;
import es.bsc.vmplacement.domain.Host;

/**
 * This class includes several score functions that are common to different score calculators.
 *
 * @author David Ortiz (david.ortiz@bsc.es)
 */
public abstract class ScoreCalculatorCommon {

    private final static int PENALTY_FOR_MOVING_FIXED_VMS = 10000;

    public static double getClusterOverCapacityScore(ClusterState clusterState) {
        double result = 0;
        for (Host host: clusterState.getHosts()) {
            result += host.getOverCapacityScore(clusterState.getVms());
        }
        return result;
    }
    
    public static double getClusterPenaltyScoreForFixedVms(ClusterState clusterState) {
        double result = 0;
        for (Host host: clusterState.getHosts()) {
            if (host.missingFixedVMs(clusterState.getVms())) {
                result -= PENALTY_FOR_MOVING_FIXED_VMS;
            }
        }
        return result;
    }
    
}
