package es.bsc.vmplacement.placement;

import es.bsc.vmplacement.domain.ClusterState;
import es.bsc.vmplacement.domain.Host;
import es.bsc.vmplacement.domain.Vm;
import es.bsc.vmplacement.placement.config.VmPlacementConfig;
import org.optaplanner.core.api.solver.Solver;

import java.util.List;

/**
 * This class describes the problem of placing n VMs in m hosts. This class defines a list of virtual machines,
 * a list of hosts, and a configuration attribute that specifies how the problem should be solved (construction
 * heuristic algorithm, local search algorithm, time limit to solve the problem, etc.).
 *
 * @author David Ortiz (david.ortiz@bsc.es)
 */
public class VmPlacementProblem {

    private final List<Vm> vms;
    private final List<Host> hosts;
    private final VmPlacementConfig config;

    /**
     * Class constructor.
     *
     * @param hosts the hosts
     * @param vms the virtual machines
     * @param config the configuration object for the VM placement problem
     */
    public VmPlacementProblem(List<Host> hosts, List<Vm> vms, VmPlacementConfig config) {
        this.hosts = hosts;
        this.vms = vms;
        this.config = config;
        addFixedVmsToHosts();
    }

    /**
     * This function returns the best solution to the problem.
     *
     * @return the state of a cluster after solving the placement problem
     */
    public ClusterState getBestSolution() {
        Solver solver = VmPlacementSolver.buildSolver(config);
        solver.setPlanningProblem(getInitialState());
        solver.solve();
        return (ClusterState) solver.getBestSolution();
    }

    /**
     * This function adds to the hosts the VMs that the user specified that need to be deployed in that host.
     * We refer to those VMs as 'fixed'. This function only adds the VMs as fixed if the option of fixed VMs is
     * active in the configuration.
     */
    private void addFixedVmsToHosts() {
        if (config.vmsAreFixed()) {
            for (Vm vm: vms) {
                if (vm.getHost() != null) {
                    getHost(vm.getHost().getId()).addFixedVm(vm.getId());
                }
            }
        }
    }

    /**
     * Returns a host by ID from the list of hosts.
     *
     * @param id the ID of the host
     * @return the host
     */
    private Host getHost(long id) {
        for (Host host: hosts) {
            if (host.getId().equals(id)) {
                return host;
            }
        }
        return null;
    }

    /**
     * Returns the initial state of the cluster.
     * This function just creates a new ClusterState instance from the data received. It does not perform any VM to host
     * assignment that was not specified.
     *
     * @return the initial state of the cluster
     */
    private ClusterState getInitialState() {
        ClusterState result = new ClusterState();
        result.setVms(vms);
        result.setHosts(hosts);
        return result;
    }

}
