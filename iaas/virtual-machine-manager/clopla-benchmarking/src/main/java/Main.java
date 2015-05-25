import es.bsc.clopla.domain.ConstructionHeuristic;
import es.bsc.clopla.placement.config.Policy;
import es.bsc.clopla.placement.config.VmPlacementConfig;
import es.bsc.clopla.placement.config.localsearch.HillClimbing;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        Cluster cluster = ClusterGenerator.generateCluster(
                100, new VmDimensions(1, 4, 1, 4, 10, 25),
                40, new HostDimensions(1, 16, 1, 16, 10, 100));

        VmPlacementConfig vmPlacementConfig = new VmPlacementConfig.Builder(
                Policy.CONSOLIDATION,
                60,
                ConstructionHeuristic.FIRST_FIT_DECREASING,
                new HillClimbing(),
                false)
                .build();

        List<ExperimentExecution> experimentExecutions = new ArrayList<>();
        experimentExecutions.add(new ExperimentExecution(cluster, vmPlacementConfig));
        Experiment experiment = new Experiment(experimentExecutions);
        System.out.println(ExperimentRunner.runExperiment(experiment, 10));
    }

}
