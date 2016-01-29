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

package es.bsc.demiurge.core.manager.components;

import es.bsc.demiurge.core.clopla.domain.ClusterState;
import es.bsc.demiurge.core.clopla.domain.LocalSearchHeuristic;
import es.bsc.demiurge.core.clopla.domain.LocalSearchHeuristicOption;
import es.bsc.demiurge.core.clopla.lib.Clopla;
import es.bsc.demiurge.core.clopla.lib.IClopla;
import es.bsc.demiurge.core.cloudmiddleware.CloudMiddlewareException;
import es.bsc.demiurge.core.models.scheduling.*;
import es.bsc.demiurge.core.models.vms.Vm;
import es.bsc.demiurge.core.models.vms.VmDeployed;
import es.bsc.demiurge.core.monitoring.hosts.Host;
import es.bsc.demiurge.core.vmplacement.CloplaConversor;
import es.bsc.demiurge.core.configuration.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class VmPlacementManager {

    private final IClopla clopla = new Clopla(); // Library used for the VM Placement
    private final VmsManager vmsManager;
    private final HostsManager hostsManager;
    private final EstimatesManager estimatesManager;

    public VmPlacementManager(VmsManager vmsManager, HostsManager hostsManager, 
                              EstimatesManager estimatesManager) {
        this.vmsManager = vmsManager;
        this.hostsManager = hostsManager;
        this.estimatesManager = estimatesManager;
    }
    
    /**
     * Returns a list of the construction heuristics supported by the VM Manager.
     *
     * @return the list of construction heuristics
     */
    public List<ConstructionHeuristic> getConstructionHeuristics() {
        List<ConstructionHeuristic> result = new ArrayList<>();
        for (es.bsc.demiurge.core.clopla.domain.ConstructionHeuristic heuristic: clopla.getConstructionHeuristics()) {
            result.add(new ConstructionHeuristic(heuristic.name()));
        }
        return result;
    }

    /**
     * Returns a list of the local search algorithms supported by the VM Manager.
     *
     * @return the list of local search algorithms
     */
    public List<LocalSearchAlgorithmOptionsUnset> getLocalSearchAlgorithms() {
        // This function could be simplified changing the LocalSearchAlgorithmOptionsUnset
        // It would be a good idea to use the same approach as in the vm placement library
        List<LocalSearchAlgorithmOptionsUnset> result = new ArrayList<>();
        for (Map.Entry<LocalSearchHeuristic, List<LocalSearchHeuristicOption>> entry :
                clopla.getLocalSearchAlgorithms().entrySet()) {
            String heuristicName = entry.getKey().toString();
            List<String> heuristicOptions = new ArrayList<>();
            for (LocalSearchHeuristicOption option: entry.getValue()) {
                heuristicOptions.add(option.toString());
            }
            result.add(new LocalSearchAlgorithmOptionsUnset(heuristicName, heuristicOptions));

        }
        return result;
    }

    /**
     * This function calculates a deployment plan based on a request. It uses the VM placement library.
     *
     * @param recommendedPlanRequest the request
     * @param assignVmsToCurrentHosts indicates whether the hosts should be set in the VM instances
     * @param vmsToDeploy list of VMs that need to be deployed
     * @return the recommended plan
     */
    public RecommendedPlan getRecommendedPlan(String schedulingAlgorithm,
                                              RecommendedPlanRequest recommendedPlanRequest,
                                              boolean assignVmsToCurrentHosts,
                                              List<Vm> vmsToDeploy) throws CloudMiddlewareException {
        CloplaConversor cc = Config.INSTANCE.getCloplaConversor();
        List<Host> hosts = hostsManager.getHosts();
        ClusterState clusterStateRecommendedPlan = clopla.getBestSolution(
                cc.getCloplaHosts(hosts),
                cc.getCloplaVms(
                        getVmsDeployedAndScheduledNonDeployed(),
                        vmsToDeploy,
                        cc.getCloplaHosts(hosts),
                        assignVmsToCurrentHosts),
                cc.getCloplaConfig(
						schedulingAlgorithm,
                        recommendedPlanRequest,
                        estimatesManager));
        return cc.getRecommendedPlan(clusterStateRecommendedPlan);
    }

    /**
     * This function executes a deployment plan. This means that each of the VMs of the deployment plan are migrated
     * to the host specified if they were not already deployed there.
     *
     * @param deploymentPlan the deployment plan
     */
    public void executeDeploymentPlan(VmPlacement[] deploymentPlan) throws CloudMiddlewareException {
        for (VmPlacement vmPlacement: deploymentPlan) {

            // We need to check that the VM is still deployed.
            // It might be the case that a VM was deleted in the time interval between a recommended plan is
            // calculated and the execution order for that deployment plan is received
            if (vmsManager.getVm(vmPlacement.getVmId()) != null) {
                boolean vmAlreadyDeployedInHost = vmPlacement.getHostname()
                        .equals(vmsManager.getVm(vmPlacement.getVmId()).getHostName());
                if (!vmAlreadyDeployedInHost) {
                    vmsManager.migrateVm(vmPlacement.getVmId(), vmPlacement.getHostname());
                }
            }

        }
    }

    private List<VmDeployed> getVmsDeployedAndScheduledNonDeployed() throws CloudMiddlewareException {
        List<VmDeployed> result = new ArrayList<>();

        // I think that the VMs that are scheduled but not deployed should be gotten before the scheduled ones.
        // The reason is that if we obtain first the ones that are deployed and then the ones that are scheduled but
        // not deployed, we might not take into account those VMs that were not deployed at the moment of the first
        // call but that were deployed before the making the second call.

        List<VmDeployed> vmsScheduledNonDeployed = vmsManager.getScheduledNonDeployedVms();
        List<VmDeployed> vmsDeployed = vmsManager.getAllVms();
        result.addAll(vmsDeployed);

        // Add only the ones that have not been deployed yet.
        // (They could have been deployed during the two calls)
        for (VmDeployed vmWasScheduledNonDeployed: vmsScheduledNonDeployed) {
            boolean deployed = false;

            for (int i = 0; i < vmsDeployed.size() && !deployed; ++i) {
                if (vmWasScheduledNonDeployed.getId().equals(vmsDeployed.get(i).getId())) {
                    deployed = true;
                }
            }

            if (!deployed) {
                result.add(vmWasScheduledNonDeployed);
            }
        }

        return result;
    }

}
