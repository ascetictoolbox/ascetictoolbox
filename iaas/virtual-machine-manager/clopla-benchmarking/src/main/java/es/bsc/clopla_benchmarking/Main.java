package es.bsc.clopla_benchmarking;

import es.bsc.clopla.domain.ClusterState;
import es.bsc.clopla.domain.ConstructionHeuristic;
import es.bsc.clopla.domain.Vm;
import es.bsc.clopla.lib.Clopla;
import es.bsc.clopla.placement.config.Policy;
import es.bsc.clopla.placement.config.VmPlacementConfig;
import es.bsc.clopla.placement.config.localsearch.*;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        /*Cluster cluster = ClusterGenerator.generateCluster(
                100, new VmDimensions(1, 4, 1, 4, 10, 25),
                40, new HostDimensions(1, 16, 1, 16, 10, 100));*/

        Cluster cluster = ClusterGenerator.generateCluster(100, 50, 100, new HostDimensions(1, 16, 1, 16, 10, 100));

        // Assign the hosts with any construction heuristic, because we are only interested in comparing the
        // local search heuristics (at least for now)
        // The timeout specified should be higher enough to finish placing all the VMs in the 'hardest' case.
        ClusterState initialSolution = new Clopla().getBestSolution(
                cluster.getHosts(),
                cluster.getVms(),
                new VmPlacementConfig.Builder(
                        Policy.CONSOLIDATION,
                        300,
                        ConstructionHeuristic.FIRST_FIT,
                        null,
                        false).build());
        cluster = new Cluster(initialSolution.getVms(), initialSolution.getHosts());

        // Check that the construction heuristic had time to place all the VMs
        for (Vm vm : cluster.getVms()) {
            if (vm.getHost() == null) {
                throw new RuntimeException("The construction heuristic did not have time to place all the VMs." +
                        "Specify a higher timeout or reduce the cluster size.");
            }
        }

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
