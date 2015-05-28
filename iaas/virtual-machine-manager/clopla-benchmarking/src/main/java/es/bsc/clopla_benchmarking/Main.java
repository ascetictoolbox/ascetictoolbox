package es.bsc.clopla_benchmarking;

import es.bsc.clopla.domain.Host;
import es.bsc.clopla.domain.Vm;
import es.bsc.clopla.placement.config.localsearch.*;
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

        int[] times = {60, 180, 300};

        List<Cluster> clusters = new ArrayList<>();

        clusters.add(ClusterGenerator.generateCluster(100, 25, 100, new HostDimensions(1, 16, 1, 16, 10, 100)));
        clusters.add(ClusterGenerator.generateCluster(100, 50, 100, new HostDimensions(1, 16, 1, 16, 10, 100)));
        clusters.add(ClusterGenerator.generateCluster(100, 75, 100, new HostDimensions(1, 16, 1, 16, 10, 100)));
        clusters.add(ClusterGenerator.generateCluster(100, 100, 100, new HostDimensions(1, 16, 1, 16, 10, 100)));

        clusters.add(ClusterGenerator.generateCluster(1000, 25, 1000, new HostDimensions(1, 16, 1, 16, 10, 100)));
        clusters.add(ClusterGenerator.generateCluster(1000, 50, 1000, new HostDimensions(1, 16, 1, 16, 10, 100)));
        clusters.add(ClusterGenerator.generateCluster(1000, 75, 1000, new HostDimensions(1, 16, 1, 16, 10, 100)));
        clusters.add(ClusterGenerator.generateCluster(1000, 100, 1000, new HostDimensions(1, 16, 1, 16, 10, 100)));

        clusters.add(ClusterGenerator.generateCluster(10000, 25, 10000, new HostDimensions(1, 16, 1, 16, 10, 100)));
        clusters.add(ClusterGenerator.generateCluster(10000, 50, 10000, new HostDimensions(1, 16, 1, 16, 10, 100)));
        clusters.add(ClusterGenerator.generateCluster(10000, 75, 10000, new HostDimensions(1, 16, 1, 16, 10, 100)));
        clusters.add(ClusterGenerator.generateCluster(10000, 100, 10000, new HostDimensions(1, 16, 1, 16, 10, 100)));

        List<LocalSearch> localSearchAlgs = new ArrayList<>();
        localSearchAlgs.add(new HillClimbing());
        localSearchAlgs.add(new LateAcceptance(400));
        localSearchAlgs.add(new LateSimulatedAnnealing(100, 1000));
        localSearchAlgs.add(new SimulatedAnnealing(2, 100));
        localSearchAlgs.add(new StepCountingHC(400));
        localSearchAlgs.add(new TabuSearch(7, 1000));


        for (Cluster cluster : clusters) {
            assignVmsToHostRandomly(cluster.getVms(), cluster.getHosts());
            List<Vm> originalVmList = new ArrayList<>(cluster.getVms());

            for (int time : times) {
                Experiment experiment = ExperimentGenerator.generateExperiment(
                        new Cluster(originalVmList, cluster.getHosts()), time, localSearchAlgs);
                System.out.println(ResultsCsvConverter.experimentExecutionResultsToCsv(
                        ExperimentRunner.runExperiment(experiment)));
            }
        }
    }

    private static void assignVmsToHostRandomly(List<Vm> vms, List<Host> hosts) {
        for (Vm vm : vms) {
            vm.setHost(hosts.get(Randomizer.generate(0, hosts.size() - 1)));
        }
    }

}
