package es.bsc.clopla_benchmarking;

import es.bsc.clopla.domain.Host;
import es.bsc.clopla.domain.Vm;
import es.bsc.clopla.placement.config.localsearch.HillClimbing;
import es.bsc.clopla.placement.config.localsearch.LocalSearch;
import es.bsc.clopla.placement.config.localsearch.StepCountingHC;
import es.bsc.clopla_benchmarking.cluster_generation.ClusterGenerator;
import es.bsc.clopla_benchmarking.cluster_generation.HostDimensions;
import es.bsc.clopla_benchmarking.experiments.Experiment;
import es.bsc.clopla_benchmarking.experiments.ExperimentGenerator;
import es.bsc.clopla_benchmarking.experiments.ExperimentRunner;
import es.bsc.clopla_benchmarking.experiments.ResultsCsvConverter;
import es.bsc.clopla_benchmarking.models.Cluster;
import es.bsc.clopla_benchmarking.utils.Randomizer;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        Cluster cluster = ClusterGenerator.generateCluster(1000, 50, 1000, new HostDimensions(1, 16, 1, 16, 10, 100));

        // Assign the hosts randomly, because we are only interested in comparing
        // the local search heuristics, not the construction heuristics (at least for now)
        assignVmsToHostRandomly(cluster.getVms(), cluster.getHosts());

        List<LocalSearch> localSearchAlgs = new ArrayList<>();
        localSearchAlgs.add(new HillClimbing());
        //localSearchAlgs.add(new LateAcceptance(400));
        //localSearchAlgs.add(new LateSimulatedAnnealing(100, 1000));
        //localSearchAlgs.add(new SimulatedAnnealing(2, 100));
        localSearchAlgs.add(new StepCountingHC(400));
        //localSearchAlgs.add(new TabuSearch(7, 1000));

        Experiment experiment = ExperimentGenerator.generateExperiment(cluster, 120, localSearchAlgs);
        System.out.println(ResultsCsvConverter.experimentExecutionResultsToCsv(
                ExperimentRunner.runExperiment(experiment)));
    }

    private static void assignVmsToHostRandomly(List<Vm> vms, List<Host> hosts) {
        for (Vm vm : vms) {
            vm.setHost(hosts.get(Randomizer.generate(0, hosts.size() - 1)));
        }
    }

}
