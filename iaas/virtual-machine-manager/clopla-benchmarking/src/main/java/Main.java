import es.bsc.clopla.domain.ConstructionHeuristic;
import es.bsc.clopla.domain.Host;
import es.bsc.clopla.domain.Vm;
import es.bsc.clopla.placement.config.Policy;
import es.bsc.clopla.placement.config.VmPlacementConfig;
import es.bsc.clopla.placement.config.localsearch.HillClimbing;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        List<Vm> vms = VmCollectionGenerator.generateVmCollection(
                50, new VmDimensions(1, 4, 1, 4, 10, 25));

        List<Host> hosts = ClusterGenerator.generateCluster(
                new ClusterDimensions(40, new HostDimensions(1, 16, 1, 16, 10, 100)));

        VmPlacementConfig vmPlacementConfig = new VmPlacementConfig.Builder(
                Policy.CONSOLIDATION, // Scheduling policy
                60, // Timeout
                ConstructionHeuristic.FIRST_FIT_DECREASING, // Construction heuristic
                new HillClimbing(), // Local Search heuristic
                false) // Deploy VMs in specific hosts?
                .build();

        ExperimentExecution experimentExecution = new ExperimentExecution(vms, hosts, vmPlacementConfig);
        System.out.println(ExperimentExecutionRunner.runExperimentExecution(experimentExecution, 5));
    }

}
