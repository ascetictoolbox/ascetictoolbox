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

import es.bsc.clopla.domain.ClusterState;
import es.bsc.clopla.domain.ConstructionHeuristic;
import es.bsc.clopla.placement.config.Policy;
import es.bsc.clopla.placement.config.VmPlacementConfig;
import es.bsc.clopla.placement.config.localsearch.*;
import es.bsc.vmmanagercore.energymodeller.EnergyModeller;
import es.bsc.vmmanagercore.model.scheduling.RecommendedPlan;
import es.bsc.vmmanagercore.model.scheduling.RecommendedPlanRequest;
import es.bsc.vmmanagercore.model.scheduling.SchedulingAlgorithm;
import es.bsc.vmmanagercore.model.vms.Vm;
import es.bsc.vmmanagercore.model.vms.VmDeployed;
import es.bsc.vmmanagercore.monitoring.hosts.Host;
import es.bsc.vmmanagercore.pricingmodeller.PricingModeller;

import java.util.ArrayList;
import java.util.List;

/**
 * The function of this class is to convert the data types used in then VM Manager Core, to the equivalent data
 * types used in the VM Placement library.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class CloplaConversor {

    // Suppress default constructor for non-instantiability
    private CloplaConversor() {
        throw new AssertionError();
    }

    /**
     * Converts a list of VMs as defined in the VMM core to a list of VMs as defined in the VM placement library.
     * It is possible to initialize in each one of the VMs, the host where they are deployed in.
     *
     * @param vms the list of VMs used by the VMM core
     * @param hosts the list of hosts as defined by the VM placement library
     * @param assignVmsToHosts indicates whether it is needed to set the hosts in the VMs
     * @return the list of VMs used by the VM placement library
     */
    public static List<es.bsc.clopla.domain.Vm> getCloplaVms(List<VmDeployed> vms,
                                                             List<Vm> vmsToDeploy,
                                                             List<es.bsc.clopla.domain.Host> hosts,
                                                             boolean assignVmsToHosts) {
        List<es.bsc.clopla.domain.Vm> result = new ArrayList<>();

        // Add the VMs already deployed
        for (int i = 0; i < vms.size(); ++i) {
            result.add(getCloplaVm((long) i, vms.get(i), hosts, assignVmsToHosts));
        }

        // Add the VMs that need to be deployed
        for (int i = vms.size(); i < vms.size() + vmsToDeploy.size(); ++i) {
            result.add(getCloplaVmToDeploy((long) i, vmsToDeploy.get(i - vms.size())));
        }

        return result;
    }

    /**
     * Converts a list of hosts as defined in the VMM core to a list of hosts as defined in the VM placement library.
     *
     * @param hosts the list of host used by the VMM core
     * @return the list of host used by the VM placement library
     */
    public static List<es.bsc.clopla.domain.Host> getCloplaHosts(List<Host> hosts) {
        List<es.bsc.clopla.domain.Host> result = new ArrayList<>();
        for (Host host: hosts) {
            result.add(CloplaHostFactory.getCloplaHost(host));
        }
        return result;
    }

    /**
     * Returns a configuration object for the VM Placement performed by the VM placement library.
     *
     * @param schedulingAlgorithm the scheduling algorithm
     * @param recommendedPlanRequest the recommended plan request
     * @return the placement configuration for the VM placement library
     */
    public static VmPlacementConfig getCloplaConfig(SchedulingAlgorithm schedulingAlgorithm,
                                                    RecommendedPlanRequest recommendedPlanRequest,
                                                    EnergyModeller energyModeller, PricingModeller pricingModeller) {
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
                .energyModeller(new CloplaEnergyModeller(energyModeller))
                .priceModeller(new CloplaPriceModeller(pricingModeller, energyModeller))
                .build();
    }

    /**
     * Returns a recommended plan from a cluster state defined by the VM placement library.
     *
     * @param clusterState the cluster state
     * @return the recommended plan
     */
    public static RecommendedPlan getRecommendedPlan(ClusterState clusterState) {
        RecommendedPlan result = new RecommendedPlan();
        for (es.bsc.clopla.domain.Vm vm: clusterState.getVms()) {
            result.addVmToHostAssignment(vm.getAlphaNumericId(), vm.getHost().getHostname());
        }
        return result;
    }

    /**
     * Converts a list of VMs from the format used in the Vm Placement library to the format used by the VMM
     *
     * @param vms the list of VMs for the placement library
     * @return the list of VMs for the Energy Modeller
     */
    public static List<es.bsc.vmmanagercore.model.vms.Vm> cloplaVmsToVmmType(List<es.bsc.clopla.domain.Vm> vms) {
        List<es.bsc.vmmanagercore.model.vms.Vm> result = new ArrayList<>();
        for (es.bsc.clopla.domain.Vm vm: vms) {
            result.add(new es.bsc.vmmanagercore.model.vms.Vm(
                    vm.getAlphaNumericId(),
                    null,
                    vm.getNcpus(),
                    vm.getRamMb(),
                    vm.getDiskGb(),
                    0, // Is this a problem? Clopla does not deal with swap
                    null,
                    null));
        }
        return result;
    }

    /**
     * Converts a deployed VM as defined in the VMM core to a VM as defined in the VM placement library.
     *
     * @param id the id of the VM used by the VM placement library
     * @param vm the VM used by the VMM core
     * @param cloplaHosts list of hosts of the cluster as defined by the VM placement library
     * @param assignVmsToHosts indicates whether it is needed to set the hosts in the VMs
     * @return the VM used by the VM placement library
     */
    private static es.bsc.clopla.domain.Vm getCloplaVm(Long id, VmDeployed vm,
                                                       List<es.bsc.clopla.domain.Host> cloplaHosts,
                                                       boolean assignVmsToHosts) {
        es.bsc.clopla.domain.Vm result = new es.bsc.clopla.domain.Vm.Builder(
                id, vm.getCpus(), vm.getRamMb(), vm.getDiskGb())
                .appId(vm.getApplicationId())
                .alphaNumericId(vm.getId())
                .build();

        // If we do not need to assign the VMs to their current hosts, then return the result
        if (!assignVmsToHosts) {
            return result;
        }

        // Else, find the host by its hostname and assign it to the VM
        result.setHost(findCloplaHost(cloplaHosts, vm.getHostName()));
        return result;
    }

    // Note: This function should probably be merged with getCloplaVm
    private static es.bsc.clopla.domain.Vm getCloplaVmToDeploy(Long id, Vm vm) {
        return new es.bsc.clopla.domain.Vm.Builder(
                id, vm.getCpus(), vm.getRamMb(), vm.getDiskGb())
                .appId(vm.getApplicationId())
                .alphaNumericId(vm.getName())
                .build();
    }

    /**
     * Gets a Policy as defined in the VM placement library from a Scheduling Algorithm as defined in the VMM core.
     *
     * @param schedulingAlgorithm the scheduling algorithm
     * @return the policy
     */
    private static Policy getPolicy(SchedulingAlgorithm schedulingAlgorithm) {
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
     * Gets a Construction Heuristic as defined in the VM placement library from an heuristic name.
     *
     * @param name the name
     * @return the construction heuristic
     */
    private static ConstructionHeuristic getConstructionHeuristic(String name) {
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
     * Gets a Local Search as defined in the VM placement library from a Recommended Plan Request.
     *
     * @param recommendedPlanRequest the recommended plan request
     * @return the local search
     */
    private static LocalSearch getLocalSearch(RecommendedPlanRequest recommendedPlanRequest) {
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
     * Finds a cloplaHost by hostname in a list of cloplaHosts
     *
     * @param cloplaHosts the list of cloplaHosts
     * @param hostname the hostname
     * @return the cloplaHost found
     */
    private static es.bsc.clopla.domain.Host findCloplaHost(List<es.bsc.clopla.domain.Host> cloplaHosts,
                                                            String hostname) {
        for (es.bsc.clopla.domain.Host cloplaHost: cloplaHosts) {
            if (hostname.equals(cloplaHost.getHostname())) {
                return cloplaHost;
            }
        }
        return null;
    }

}
