package es.bsc.clopla_benchmarking.experiments;

import es.bsc.clopla.domain.ClusterState;
import es.bsc.clopla.lib.Clopla;
import es.bsc.clopla.lib.IClopla;
import es.bsc.clopla.placement.config.VmPlacementConfig;
import es.bsc.clopla_benchmarking.models.Cluster;

public class ExperimentExecutionRunner {

    // Suppress default constructor for non-instantiability
    private ExperimentExecutionRunner() {
        throw new AssertionError();
    }

    private static final IClopla clopla = new Clopla();

    // Returns a list of cluster states. This list contains one cluster state for each minute of execution.
    // This way, we can study how much better an algorithm gets given more execution time.
    public static ExperimentExecutionResults runExperimentExecution(ExperimentExecution experimentExecution) {
        VmPlacementConfig currentConfig = new VmPlacementConfig.Builder(
                experimentExecution.getVmPlacementConfig().getPolicy(),
                experimentExecution.getVmPlacementConfig().getTimeLimitSeconds(),
                experimentExecution.getVmPlacementConfig().getConstructionHeuristic(),
                experimentExecution.getVmPlacementConfig().getLocalSearch(),
                false).build();

        ClusterState solutionClusterState = clopla.getBestSolution(
                experimentExecution.getCluster().getHosts(),
                experimentExecution.getCluster().getVms(),
                currentConfig);

        return new ExperimentExecutionResults(
                solutionClusterState.getHosts().size(),
                solutionClusterState.getVms().size(),
                new Cluster(solutionClusterState.getVms(), solutionClusterState.getHosts())
                        .getClusterLoad().getAvgLoad(),
                experimentExecution.getVmPlacementConfig().getLocalSearch(),
                experimentExecution.getVmPlacementConfig().getTimeLimitSeconds(),
                getScore(solutionClusterState));
    }

    private static int getScore(ClusterState clusterState) {
        // If there is overbooking return a negative number (according to Clopla cluster overcapacity score),
        // so we can distinguish the algorithms that are good enough to place all the VMs without overbooking.
        // If there is no overbooking, simply return the number of idle hosts.
        if (clusterState.getScore().toLevelNumbers()[0].intValue() < 0) {
            return -1;
        }
        return clusterState.getScore().toLevelNumbers()[2].intValue();
    }

}
