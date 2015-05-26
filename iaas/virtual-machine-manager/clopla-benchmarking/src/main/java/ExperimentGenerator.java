import es.bsc.clopla.domain.ConstructionHeuristic;
import es.bsc.clopla.placement.config.Policy;
import es.bsc.clopla.placement.config.VmPlacementConfig;
import es.bsc.clopla.placement.config.localsearch.LocalSearch;

import java.util.ArrayList;
import java.util.List;

public class ExperimentGenerator {

    private static final Policy POLICY = Policy.CONSOLIDATION;
    private static final ConstructionHeuristic CONSTRUCTION_HEURISTIC = ConstructionHeuristic.FIRST_FIT_DECREASING;

    private ExperimentGenerator() {
        throw new AssertionError();
    }

    public static Experiment generateExperiment(Cluster cluster, int experimentTimeoutSeconds,
                                                List<LocalSearch> localSearchAlgs) {
        List<ExperimentExecution> experimentExecutions = new ArrayList<>();
        List<VmPlacementConfig> vmPlacementConfigs =
                generateVmPlacementConfigs(experimentTimeoutSeconds, localSearchAlgs);
        for (VmPlacementConfig vmPlacementConfig : vmPlacementConfigs) {
            experimentExecutions.add(new ExperimentExecution(cluster, vmPlacementConfig));
        }
        return new Experiment(experimentExecutions);
    }

    private static List<VmPlacementConfig> generateVmPlacementConfigs(int experimentTimeoutSeconds,
                                                                      List<LocalSearch> localSearchAlgs) {
        List<VmPlacementConfig> result = new ArrayList<>();
        for (LocalSearch localSearchAlg : localSearchAlgs) {
            result.add(new VmPlacementConfig.Builder(
                    POLICY,
                    experimentTimeoutSeconds,
                    CONSTRUCTION_HEURISTIC,
                    localSearchAlg,
                    false)
                    .build());
        }
        return result;
    }


}
