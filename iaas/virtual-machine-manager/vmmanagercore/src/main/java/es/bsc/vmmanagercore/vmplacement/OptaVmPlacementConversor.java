/**
 Copyright (C) 2013-2014  Barcelona Supercomputing Center

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package es.bsc.vmmanagercore.vmplacement;

import es.bsc.vmmanagercore.model.scheduling.RecommendedPlan;
import es.bsc.vmmanagercore.model.scheduling.RecommendedPlanRequest;
import es.bsc.vmmanagercore.model.scheduling.SchedulingAlgorithm;
import es.bsc.vmmanagercore.model.vms.Vm;
import es.bsc.vmmanagercore.model.vms.VmDeployed;
import es.bsc.vmmanagercore.monitoring.Host;
import es.bsc.vmmanagercore.pricingmodeller.PricingModeller;
import es.bsc.vmplacement.domain.ClusterState;
import es.bsc.vmplacement.domain.ConstructionHeuristic;
import es.bsc.vmplacement.placement.config.Policy;
import es.bsc.vmplacement.placement.config.VmPlacementConfig;
import es.bsc.vmplacement.placement.config.localsearch.*;

import java.util.ArrayList;
import java.util.List;

/**
 * The function of this class is to convert the data types used in then VM Manager Core, to the equivalent data
 * types used in the Opta VM Placement library.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class OptaVmPlacementConversor {

    /**
     * Converts a list of VMs as defined in the VMM core to a list of VMs as defined in the OptaVMPlacement library.
     * It is possible to initialize in each one of the VMs, the host where they are deployed in.
     *
     * @param vms the list of VMs used by the VMM core
     * @param hosts the list of hosts as defined by the OptaVmPlacement library
     * @param assignVmsToHosts indicates whether it is needed to set the hosts in the VMs
     * @return the list of VMs used by the OptaVmPlacement library
     */
    public List<es.bsc.vmplacement.domain.Vm> getOptaVms(List<VmDeployed> vms,
                                                         List<Vm> vmsToDeploy,
                                                         List<es.bsc.vmplacement.domain.Host> hosts,
                                                         boolean assignVmsToHosts) {
        List<es.bsc.vmplacement.domain.Vm> result = new ArrayList<>();

        // Add the VMs already deployed
        for (int i = 0; i < vms.size(); ++i) {
            result.add(getOptaVm((long) i, vms.get(i), hosts, assignVmsToHosts));
        }

        // Add the VMs that need to be deployed
        for (int i = vms.size(); i < vms.size() + vmsToDeploy.size(); ++i) {
            result.add(getOptaVmToDeploy((long) i, vmsToDeploy.get(i - vms.size())));
        }

        return result;
    }

    /**
     * Converts a list of hosts as defined in the VMM core to a list of hosts as defined in the OptaVMPlacement library.
     *
     * @param hosts the list of host used by the VMM core
     * @return the list of host used by the OptaVmPlacement library
     */
    public List<es.bsc.vmplacement.domain.Host> getOptaHosts(List<Host> hosts) {
        List<es.bsc.vmplacement.domain.Host> result = new ArrayList<>();
        for (Host host: hosts) {
            result.add(OptaHostFactory.getOptaHost(host));
        }
        return result;
    }

    /**
     * Returns a configuration object for the VM Placement performed by the OptaVmPlacement library.
     *
     * @param schedulingAlgorithm the scheduling algorithm
     * @param recommendedPlanRequest the recommended plan request
     * @return the placement configuration for the OptaVmPlacement library
     */
    public VmPlacementConfig getOptaPlacementConfig(SchedulingAlgorithm schedulingAlgorithm,
            RecommendedPlanRequest recommendedPlanRequest, PricingModeller pricingModeller) {
        int timeLimitSec = recommendedPlanRequest.getTimeLimitSeconds();
        if (getLocalSearch(recommendedPlanRequest) == null) {
            timeLimitSec = 1; // It does not matter because the local search alg will not be run, but the
                              // VM placement library complains if we send 0
        }

        return new VmPlacementConfig.Builder(
                getPolicy(schedulingAlgorithm),
                timeLimitSec,
                getConstructionHeuristic(recommendedPlanRequest.getConstructionHeuristicName()),
                getLocalSearch(recommendedPlanRequest),
                false)
                .energyModel(new OptaEnergyModeller())
                .priceModel(new OptaPriceModeller(pricingModeller))
                .build();
    }

    /**
     * Returns a recommended plan from a cluster state defined by the optaVmPlacement library.
     *
     * @param clusterState the cluster state
     * @return the recommended plan
     */
    public RecommendedPlan getRecommendedPlan(ClusterState clusterState) {
        RecommendedPlan result = new RecommendedPlan();
        for (es.bsc.vmplacement.domain.Vm vm: clusterState.getVms()) {
            result.addVmToHostAssignment(vm.getAlphaNumericId(), vm.getHost().getHostname());
        }
        return result;
    }

    /**
     * Converts a deployed VM as defined in the VMM core to a VM as defined in the OptaVMPlacement library.
     *
     * @param id the id of the VM used by the OptaVMPlacement library
     * @param vm the VM used by the VMM core
     * @param optaHosts list of hosts of the cluster as defined by the OptaVMPlacement library
     * @param assignVmsToHosts indicates whether it is needed to set the hosts in the VMs
     * @return the VM used by the OptaVMPlacement library
     */
    private es.bsc.vmplacement.domain.Vm getOptaVm(Long id, VmDeployed vm,
                                                   List<es.bsc.vmplacement.domain.Host> optaHosts,
                                                   boolean assignVmsToHosts) {
        es.bsc.vmplacement.domain.Vm result = new es.bsc.vmplacement.domain.Vm(
                id, vm.getCpus(), vm.getRamMb(), vm.getDiskGb(), vm.getApplicationId(), vm.getId());

        // If we do not need to assign the VMs to their current hosts, then return the result
        if (!assignVmsToHosts) {
            return result;
        }

        // Else, find the host by its hostname and assign it to the VM
        result.setHost(findOptaHost(optaHosts, vm.getHostName()));
        return result;
    }

    // Note: This function should probably be merged with getOptaVm
    private es.bsc.vmplacement.domain.Vm getOptaVmToDeploy(Long id, Vm vm) {
        return new es.bsc.vmplacement.domain.Vm(id, vm.getCpus(), vm.getRamMb(), vm.getDiskGb(),
                vm.getApplicationId(), vm.getName());
    }

    /**
     * Gets a Policy as defined in the OptaVmPlacement library from a Scheduling Algorithm as defined in the
     * VMM core.
     *
     * @param schedulingAlgorithm the scheduling algorithm
     * @return the policy
     */
    private Policy getPolicy(SchedulingAlgorithm schedulingAlgorithm) {
        switch (schedulingAlgorithm) {
            case CONSOLIDATION:
                return Policy.CONSOLIDATION;
            case COST_AWARE:
                return Policy.PRICE;
            case DISTRIBUTION:
                return Policy.DISTRIBUTION;
            case ENERGY_AWARE:
                return Policy.ENERGY;
            case GROUP_BY_APP:
                return Policy.GROUP_BY_APP;
            case RANDOM:
                return Policy.RANDOM;
            default:
                throw new IllegalArgumentException("Invalid policy");
        }
    }

    /**
     * Gets a Construction Heuristic as defined in the OptaVmPlacement library from an heuristic name.
     *
     * @param name the name
     * @return the construction heuristic
     */
    private ConstructionHeuristic getConstructionHeuristic(String name) {
        if (name == null) {
            return null;
        }

        switch (name) {
            case "FIRST_FIT":
                return ConstructionHeuristic.FIRST_FIT;
            case "FIRST_FIT_DECREASING":
                return ConstructionHeuristic.FIRST_FIT_DECREASING;
            case "BEST_FIT":
                return ConstructionHeuristic.BEST_FIT;
            case "BEST_FIT_DECREASING":
                return ConstructionHeuristic.BEST_FIT_DECREASING;
            default:
                throw new IllegalArgumentException("Invalid construction heuristic");
        }
    }

    /**
     * Gets a Local Search as defined in the OptaVmPlacement library from a Recommended Plan Request.
     *
     * @param recommendedPlanRequest the recommended plan request
     * @return the local search
     */
    private LocalSearch getLocalSearch(RecommendedPlanRequest recommendedPlanRequest) {
        if (recommendedPlanRequest.getLocalSearchAlgorithm() == null) {
            return null;
        }

        switch (recommendedPlanRequest.getLocalSearchAlgorithm().getName()) {
            case "Hill Climbing":
                return new HillClimbing();
            case "Late Acceptance":
                return new LateAcceptance(recommendedPlanRequest.getLocalSearchAlgorithm().getOptions().get("size"));
            case "Late Simulated Annealing":
                return new LateSimulatedAnnealing(
                        recommendedPlanRequest.getLocalSearchAlgorithm().getOptions().get("size"),
                        recommendedPlanRequest.getLocalSearchAlgorithm().getOptions().get("acceptedCountLimit"));
            case "Simulated Annealing":
                return new SimulatedAnnealing(
                        recommendedPlanRequest.getLocalSearchAlgorithm().getOptions().get("initialHardTemp"),
                        recommendedPlanRequest.getLocalSearchAlgorithm().getOptions().get("initialSoftTemp"));
            case "Step Counting Hill Climbing":
                return new StepCountingHC(recommendedPlanRequest.getLocalSearchAlgorithm().getOptions().get("size"));
            case "Tabu Search":
                return new TabuSearch(
                        recommendedPlanRequest.getLocalSearchAlgorithm().getOptions().get("size"),
                        recommendedPlanRequest.getLocalSearchAlgorithm().getOptions().get("acceptedCountLimit"));
            default:
                throw new IllegalArgumentException("Invalid local search algorithm");
        }
    }

    /**
     * Finds an optaHost by hostname in a list of optaHosts
     *
     * @param optaHosts the list of optaHosts
     * @param hostname the hostname
     * @return the optaHost found
     */
    private es.bsc.vmplacement.domain.Host findOptaHost(List<es.bsc.vmplacement.domain.Host> optaHosts,
                                                        String hostname) {
        for (es.bsc.vmplacement.domain.Host optaHost: optaHosts) {
            if (hostname.equals(optaHost.getHostname())) {
                return optaHost;
            }
        }
        return null;
    }

}
