/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package es.bsc.demiurge.core.clopla.placement;

import es.bsc.demiurge.core.clopla.domain.ClusterState;
import es.bsc.demiurge.core.clopla.domain.Host;
import es.bsc.demiurge.core.clopla.domain.Vm;
import es.bsc.demiurge.core.clopla.placement.config.VmPlacementConfig;
import es.bsc.demiurge.core.clopla.placement.solver.VmPlacementSolver;
import org.optaplanner.core.api.solver.Solver;

import java.util.ArrayList;
import java.util.List;

/**
 * This class describes the problem of placing n VMs in m hosts. This class defines a list of virtual machines,
 * a list of hosts, and a configuration attribute that specifies how the problem should be solved (construction
 * heuristic algorithm, local search algorithm, time limit to solve the problem, etc.).
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz (david.ortiz@bsc.es)
 */
public class VmPlacementProblem {

    private final List<Vm> vms;
    private final List<Host> hosts;
    private final VmPlacementConfig config;
    private final VmPlacementSolver vmPlacementSolver;

    /**
     * Class constructor.
     *
     * @param hosts the hosts
     * @param vms the virtual machines
     * @param config the configuration object for the VM placement problem
     */
    public VmPlacementProblem(List<Host> hosts, List<Vm> vms, VmPlacementConfig config) {
        this.hosts = new ArrayList<>(hosts);
        this.vms = new ArrayList<>(vms);
        this.config = config;
        this.vmPlacementSolver = new VmPlacementSolver(config);
        VmPlacementConfig.initialClusterState.set(getInitialState());
        addFixedVmsToHosts();
    }

    /**
     * This function returns the best solution to the problem.
     *
     * @return the state of a cluster after solving the placement problem
     */
    public ClusterState getBestSolution() {
        Solver solver = vmPlacementSolver.buildSolver();
        solver.setPlanningProblem(getInitialState());
        solver.solve();
        ClusterState result = (ClusterState) solver.getBestSolution();
        cleanThreadLocals(); // to avoid memory leaks
        return result;
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
        result.setHosts(putOffHostsAtTheEndOfTheList());
        return result;
    }

    /**
     * Returns a modified list of the hosts, where the ones that are off are placed at the end of the list.
     * This method does not modify the order of the rest of the elements.
     * @return the list of hosts with the ones that are off at the end
     */
    private List<Host> putOffHostsAtTheEndOfTheList() {
        List<Host> result = new ArrayList<>();
        result.addAll(getOnHosts());
        result.addAll(getInitiallyOffHosts());
        return result;
    }

    /**
     * Returns a list that contains the hosts that are on.
     *
     * @return the list of hosts
     */
    private List<Host> getOnHosts() {
        List<Host> result = new ArrayList<>();
        for (Host host: hosts) {
            if (!host.wasOffInitiallly()) {
                result.add(host);
            }
        }
        return result;
    }

    /**
     * Returns a list that contains the hosts that were off at the beginning of the planning.
     *
     * @return the list of hosts
     */
    private List<Host> getInitiallyOffHosts() {
        List<Host> result = new ArrayList<>();
        for (Host host: hosts) {
            if (host.wasOffInitiallly()) {
                result.add(host);
            }
        }
        return result;
    }
    
    private void cleanThreadLocals() {
        VmPlacementConfig.initialClusterState.set(null);
    }

}
