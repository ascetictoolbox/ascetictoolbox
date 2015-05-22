import es.bsc.clopla.domain.ClusterState;
import es.bsc.clopla.domain.Host;
import es.bsc.clopla.domain.Vm;
import es.bsc.clopla.lib.Clopla;
import es.bsc.clopla.lib.IClopla;
import es.bsc.clopla.placement.config.VmPlacementConfig;

import java.util.ArrayList;
import java.util.List;

public class ExperimentExecutionRunner {

    private ExperimentExecutionRunner() {
        throw new AssertionError();
    }

    private static final IClopla clopla = new Clopla();

    // Returns a list of cluster states. This list contains one cluster state for each minute of execution.
    // This way, we can study how much better an algorithm gets given more execution time.
    public static List<ClusterState> runExperimentExecution(ExperimentExecution experimentExecution,
                                                            int intervalSeconds) {
        List<ClusterState> result = new ArrayList<>();

        // Good enough for now. Later, be careful with latest execution because of rounding.
        int executionIntervals = experimentExecution.getVmPlacementConfig().getTimeLimitSeconds()/intervalSeconds;

        List<Vm> vms = experimentExecution.getVms();
        List<Host> hosts = experimentExecution.getHosts();

        for (int i = 0; i < executionIntervals; ++i) {
            VmPlacementConfig currentConfig = new VmPlacementConfig.Builder(
                    experimentExecution.getVmPlacementConfig().getPolicy(),
                    intervalSeconds,
                    experimentExecution.getVmPlacementConfig().getConstructionHeuristic(),
                    experimentExecution.getVmPlacementConfig().getLocalSearch(),
                    false).build();

            ClusterState currentClusterState = clopla.getBestSolution(hosts, vms, currentConfig);
            vms = currentClusterState.getVms();
            hosts = currentClusterState.getHosts();
            result.add(currentClusterState);
        }

        return result;
    }

}
