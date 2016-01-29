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

package es.bsc.demiurge.core.scheduler;

import es.bsc.demiurge.core.models.vms.Vm;
import es.bsc.demiurge.core.models.scheduling.VmAssignmentToHost;
import es.bsc.demiurge.core.models.scheduling.DeploymentPlan;
import es.bsc.demiurge.core.monitoring.hosts.Host;

import java.util.ArrayList;
import java.util.List;

/**
 * Deployment plan generator. Generates deployment plans that can be applied.
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class DeploymentPlanGenerator {

    private static class PossibleHostsForVm {

        public final Vm vm;
        public final List<Host> hosts;

        public PossibleHostsForVm(Vm vm, List<Host> hosts) {
            this.vm = vm;
            this.hosts = hosts;
        }

    }

    /**
     * Returns all the possible deployment plans for a list of VMs and a list of hosts.
     * A deployment plan is a list where each element indicates the host where a VM should be deployed.
     * This function returns only deployment plans that can be applied. This means that it will not return
     * deployment plans where a host is assigned VMs that require more resources than the resources that
     * are available in the host.
     *
     * @param vms the list of VMs
     * @param hosts the list of hosts
     * @return the list of deployment plans that can be applied
     */
    public List<DeploymentPlan> getPossibleDeploymentPlans(List<Vm> vms, List<Host> hosts) {
        return DeploymentPlanFilterer.filterDeploymentPlans(getAllDeploymentPlans(vms, hosts));
    }

    /**
     * Returns all the deployment plans for a list of VMs and a list of hosts.
     * Note: the result of this method contains deployment plans that use overbooking.
     *
     * @param vms the list of VMs
     * @param hosts the hosts of the infrastructure
     * @return the list of deployment plans
     */
    public List<DeploymentPlan> getAllDeploymentPlans(List<Vm> vms, List<Host> hosts) {
        List<DeploymentPlan> deploymentPlans = new ArrayList<>();
        generateDeploymentPlans(getPossibleHostsForVms(vms, hosts), deploymentPlans, 0,
                new DeploymentPlan(new ArrayList<VmAssignmentToHost>()));
        return deploymentPlans;
    }

    //TODO rename this method and the previous one to make this clearer
    public List<DeploymentPlan> getDeploymentPlansWithoutRestrictions(List<Vm> vms, List<Host> hosts) {
        List<DeploymentPlan> deploymentPlans = new ArrayList<>();
        generateDeploymentPlans(getHostsForVmsWithoutRestrictions(vms, hosts), deploymentPlans, 0,
                new DeploymentPlan(new ArrayList<VmAssignmentToHost>()));
        return deploymentPlans;
    }

    /**
     * Generates all the possible deployment plans.
     *
     * @param possibleHostsForEachVm list where each element contains a VM and all the hosts that have enough
     *        resources to host that VM
     * @param result deployment plans built
     * @param currentVmIndex index of a VM in the "possibleHostsForEachVM" list
     * @param currentDeploymentPlan deployment plan in construction
     */
    // Note: This function is implemented using a typical recursive algorithm to generate combinations.
    private void generateDeploymentPlans(List<PossibleHostsForVm> possibleHostsForEachVm, List<DeploymentPlan> result,
            int currentVmIndex, DeploymentPlan currentDeploymentPlan) {
        if (currentVmIndex == possibleHostsForEachVm.size()) {
            result.add(currentDeploymentPlan);
        }
        else {
            PossibleHostsForVm currentPossibleHostsForVm = possibleHostsForEachVm.get(currentVmIndex);
            for (Host possibleHost: currentPossibleHostsForVm.hosts) {
                DeploymentPlan deploymentPlan = new DeploymentPlan(currentDeploymentPlan.getVmsAssignationsToHosts());
                deploymentPlan.addVmAssignmentToPlan(new VmAssignmentToHost(currentPossibleHostsForVm.vm,
                        possibleHost));
                generateDeploymentPlans(possibleHostsForEachVm, result, currentVmIndex + 1, deploymentPlan);
            }
        }
    }

    /**
     * Returns a list where each position contains a VM and the hosts that have enough resources available
     * to deploy that VM.
     *
     * @param vms the VMs that need to be deployed
     * @param hosts the available hosts
     * @return the list where each position contains a VM and a list of hosts where it can be deployed
     */
    private List<PossibleHostsForVm> getPossibleHostsForVms(List<Vm> vms, List<Host> hosts) {
        List<PossibleHostsForVm> result = new ArrayList<>();
        for (Vm vm: vms) {
            result.add(new PossibleHostsForVm(vm, filter(hosts, vm.getCpus(), vm.getRamMb(), vm.getDiskGb())));
        }
        return result;
    }

    private List<PossibleHostsForVm> getHostsForVmsWithoutRestrictions(List<Vm> vms, List<Host> hosts) {
        List<PossibleHostsForVm> result = new ArrayList<>();
        for (Vm vm: vms) {
            result.add(new PossibleHostsForVm(vm, hosts));
        }
        return result;
    }

    /**
     * From a list of hosts, returns the ones that have enough CPUs, RAM, and disk available.
     *
     * @param hosts the lists of hosts
     * @param minCpus the minimum number of free CPUs
     * @param minRamMb the minimum amount of free RAM (MB)
     * @param minDiskGb the minimum amount of free disk (GB)
     * @return hosts from the input that meet the CPU, RAM, and disk requirements
     */
    private List<Host> filter(List<Host> hosts, int minCpus, int minRamMb, int minDiskGb) {
        List<Host> result = new ArrayList<>();
        for (Host host: hosts) {
            if (host.hasEnoughResources(minCpus, minRamMb, minDiskGb)) {
                result.add(host);
            }
        }
        return result;
    }

}