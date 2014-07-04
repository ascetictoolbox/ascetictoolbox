package es.bsc.vmmanagercore.scheduler;

import es.bsc.vmmanagercore.model.DeploymentPlan;
import es.bsc.vmmanagercore.model.Vm;
import es.bsc.vmmanagercore.model.VmAssignmentToHost;
import es.bsc.vmmanagercore.monitoring.Host;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Host filter. Selects hosts that meet a certain criteria about free CPUs, RAM, and disk.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class HostFilter {

    private class PossibleHostsForVm {
        public final Vm vm;
        public final List<Host> hosts;

        public PossibleHostsForVm(Vm vm, List<Host> hosts) {
            this.vm = vm;
            this.hosts = hosts;
        }
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
    public static List<Host> filter(List<Host> hosts, int minCpus, int minRamMb, int minDiskGb) {
        List<Host> filteredHosts = new ArrayList<>();
        for (Host host: hosts) {
            if (host.hasEnoughResources(minCpus, minRamMb, minDiskGb)) {
                filteredHosts.add(host);
            }
        }
        return filteredHosts;
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

    /**
     * Checks whether the hosts of the deployment plan have enough resources to host the VMs that they have
     * been assigned.
     *
     * @param deploymentPlan the deployment plan
     * @return true if the deployment plan can be applied, false otherwise
     */
    private boolean deploymentPlanCanBeApplied(DeploymentPlan deploymentPlan) {
        // Build a map that contains for each host, the VMs that are going to be deployed in it
        Map<Host, List<Vm>> vmsOfEachHost = new HashMap<>();
        for (VmAssignmentToHost vmAssignmentToHost: deploymentPlan.getVmsAssignationsToHosts()) {
            Host host = vmAssignmentToHost.getHost();
            if (!vmsOfEachHost.containsKey(host)) {
                vmsOfEachHost.put(host, new ArrayList<Vm>());
            }
            vmsOfEachHost.get(host).add(vmAssignmentToHost.getVm());
        }

        // For each of the host, check whether it has enough resources to host all the VMs that it
        // has been assigned
        for (Map.Entry<Host, List<Vm>> entry : vmsOfEachHost.entrySet()) {
            if (!entry.getKey().hasEnoughResourcesToDeployVms(entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    /**
     * From a list of deployment plans, returns only the ones that can be applied.
     * A deployment plan contains for each VM the host where it should be deployed.
     * In this context, we consider that a deployment plan can be applied if its hosts
     * contain enough resources to host the VMs that they have been assigned.
     *
     * @param deploymentPlans the deployment plans to filter
     * @return the filtered list of deployment plans
     */
    private List<DeploymentPlan> filterDeploymentPlans(List<DeploymentPlan> deploymentPlans) {
        List<DeploymentPlan> result = new ArrayList<>();
        for (DeploymentPlan deploymentPlan: deploymentPlans) {
            if (deploymentPlanCanBeApplied(deploymentPlan)) {
                result.add(deploymentPlan);
            }
        }
        return result;
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
    public List<DeploymentPlan> getAllPossibleDeploymentPlans(List<Vm> vms, List<Host> hosts) {
        List<DeploymentPlan> result = new ArrayList<>();
        generateDeploymentPlans(getPossibleHostsForVms(vms, hosts), result, 0,
                new DeploymentPlan(new ArrayList<VmAssignmentToHost>()));
        return filterDeploymentPlans(result);
    }

}