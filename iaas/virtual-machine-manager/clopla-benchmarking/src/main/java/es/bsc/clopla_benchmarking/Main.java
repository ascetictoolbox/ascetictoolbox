package es.bsc.clopla_benchmarking;

import es.bsc.clopla.placement.config.localsearch.*;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        /*Cluster cluster = ClusterGenerator.generateCluster(
                100, new VmDimensions(1, 4, 1, 4, 10, 25),
                40, new HostDimensions(1, 16, 1, 16, 10, 100));*/

        Cluster cluster = ClusterGenerator.generateCluster(200, 50, 200, new HostDimensions(1, 16, 1, 16, 10, 100));

        List<LocalSearch> localSearchAlgs = new ArrayList<>();
        localSearchAlgs.add(new HillClimbing());
        localSearchAlgs.add(new LateAcceptance(400));
        localSearchAlgs.add(new LateSimulatedAnnealing(100, 1000));
        localSearchAlgs.add(new SimulatedAnnealing(2, 100));
        localSearchAlgs.add(new StepCountingHC(400));
        localSearchAlgs.add(new TabuSearch(7, 1000));

        Experiment experiment = ExperimentGenerator.generateExperiment(cluster, 60, localSearchAlgs);
        System.out.println(ResultsCsvConverter.experimentExecutionResultsToCsv(
                ExperimentRunner.runExperiment(experiment, 10)));
    }

}
