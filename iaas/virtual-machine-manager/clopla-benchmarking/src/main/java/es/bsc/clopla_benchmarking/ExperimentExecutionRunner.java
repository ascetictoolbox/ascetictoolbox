package es.bsc.clopla_benchmarking;

import es.bsc.clopla.domain.ClusterState;
import es.bsc.clopla.domain.Host;
import es.bsc.clopla.domain.Vm;
import es.bsc.clopla.lib.Clopla;
import es.bsc.clopla.lib.IClopla;
import es.bsc.clopla.placement.config.VmPlacementConfig;

import java.util.ArrayList;
import java.util.List;

public class ExperimentExecutionRunner {

    // Suppress default constructor for non-instantiability
    private ExperimentExecutionRunner() {
        throw new AssertionError();
    }

    private static final IClopla clopla = new Clopla();

    // Returns a list of cluster states. This list contains one cluster state for each minute of execution.
    // This way, we can study how much better an algorithm gets given more execution time.
    public static ExperimentExecutionResults runExperimentExecution(ExperimentExecution experimentExecution,
                                                                    int intervalSeconds) {
        List<ClusterState> intermediateClusterStates = new ArrayList<>();

        // Good enough for now. Later, be careful with latest execution because of rounding.
        int executionIntervals = experimentExecution.getVmPlacementConfig().getTimeLimitSeconds()/intervalSeconds;

        List<Vm> vms = experimentExecution.getCluster().getVms();
        List<Host> hosts = experimentExecution.getCluster().getHosts();

        for (int i = 0; i < executionIntervals; ++i) {
            VmPlacementConfig currentConfig = new VmPlacementConfig.Builder(
                    experimentExecution.getVmPlacementConfig().getPolicy(),
                    intervalSeconds,
                    experimentExecution.getVmPlacementConfig().getConstructionHeuristic(),
                    experimentExecution.getVmPlacementConfig().getLocalSearch(),
                    false).build();

            ClusterState currentClusterState = clopla.getBestSolution(hosts, vms, currentConfig);

            // Make sure that in the first iteration all the VMs have been place
            for (Vm vm : currentClusterState.getVms()) {
                if (vm.getHost() == null) {
                    throw new RuntimeException(
                            "There are some VMs that have not been assigned to a host in the first iteration."
                            + "Try setting longer iteration steps or reducing the size of the VM placement problem.");
                }
            }

            vms = currentClusterState.getVms();
            hosts = currentClusterState.getHosts();
            intermediateClusterStates.add(currentClusterState);
        }

        return new ExperimentExecutionResults(
                intermediateClusterStates.get(0).getHosts().size(),
                intermediateClusterStates.get(0).getVms().size(),
                new Cluster(intermediateClusterStates.get(intermediateClusterStates.size() - 1).getVms(),
                        intermediateClusterStates.get(intermediateClusterStates.size() - 1).getHosts())
                        .getClusterLoad().getAvgLoad(),
                experimentExecution.getVmPlacementConfig().getLocalSearch(),
                getScores(intermediateClusterStates));
    }

    // This is temporal. It returns the third score of the list, which is idle hosts for the consolidation
    // policy, but later we might be interested in other metrics.
    private static List<Integer> getScores(List<ClusterState> clusterStates) {
        List<Integer> result = new ArrayList<>();
        for (ClusterState clusterState : clusterStates) {
            result.add(clusterState.getScore().toLevelNumbers()[2].intValue());
        }
        return result;
    }

}
