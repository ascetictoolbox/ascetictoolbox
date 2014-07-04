package es.bsc.vmmanagercore.scheduler;

import es.bsc.vmmanagercore.model.*;
import es.bsc.vmmanagercore.monitoring.HostInfo;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *  Scheduler that decides where to place the VMs that need to be deployed.
 *  This scheduler can be configured to use different scheduling algorithms (consolidation, distribution, etc.)
 * 
 *  @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class Scheduler {

    private SchedAlgorithm schedAlgorithm;
    private List<VmDeployed> vmsDeployed;

    public Scheduler(SchedulingAlgorithm schedAlg, List<VmDeployed> vmsDeployed) {
        this.vmsDeployed = vmsDeployed;
        setSchedAlgorithm(schedAlg);
    }

    private void setSchedAlgorithm(SchedulingAlgorithm schedAlg) {
        switch (schedAlg) {
            case CONSOLIDATION:
                schedAlgorithm = new SchedAlgConsolidation();
                break;
            case DISTRIBUTION:
                schedAlgorithm = new SchedAlgDistribution();
                break;
            case ENERGY_AWARE:
                schedAlgorithm = new SchedAlgEnergyAware(vmsDeployed);
                break;
            case GROUP_BY_APP:
                schedAlgorithm = new SchedAlgGroupByApp(vmsDeployed);
                break;
            case RANDOM:
                schedAlgorithm = new SchedAlgRandom();
                break;
        }
    }

    private void reserveResourcesForVmInHost(Vm vm, String hostForDeployment, List<HostInfo> hosts) {
        for (HostInfo host: hosts) {
            if (host.getHostname().equals(hostForDeployment)) {
                host.setReservedCpus(vm.getCpus());
                host.setReservedMemoryMb(vm.getRamMb());
                host.setReservedDiskGb(vm.getDiskGb());
            }
        }
    }

    /**
     * Chooses the host where a VM should be deployed. If none of the hosts has enough resources available,
     * then chooses one randomly. Otherwise, selects a host according to the scheduling algorithm used.
     *
     * @param allHosts all the hosts of the cluster
     * @param hostsWithEnoughResources the hosts of the clusted with enough resources available to deploy the VM
     * @param vm the VM
     * @return the name of the host where the VM should be deployed
     */
    private String chooseHost(List<HostInfo> allHosts, List<HostInfo> hostsWithEnoughResources, Vm vm) {
        String selectedHost;
        if (hostsWithEnoughResources.isEmpty()) {
            selectedHost = new SchedAlgRandom().chooseHost(allHosts, vm);
        }
        else {
            selectedHost = schedAlgorithm.chooseHost(hostsWithEnoughResources, vm);
        }
        return selectedHost;
    }

    /**
     * Returns the average CPU load in a collection of server loads.
     *
     * @param serversLoad the load of the servers
     * @return the average CPU load
     */
    private static double calculateAvgCpuLoad(Collection<ServerLoad> serversLoad) {
        double sum = 0;
        for (ServerLoad serverLoad: serversLoad) {
            sum += serverLoad.getCpuLoad();
        }
        return sum/serversLoad.size();
    }

    /**
     * Returns the standard deviation of the CPU load in a collection of server loads.
     *
     * @param serversLoad the load of the servers
     * @return the standard deviation of the CPU load
     */
    public static double calculateStDevCpuLoad(Collection<ServerLoad> serversLoad) {
        double sumOfDifferences = 0;
        double avgCpuLoad = calculateAvgCpuLoad(serversLoad);
        for (ServerLoad serverLoad: serversLoad) {
            sumOfDifferences += Math.pow((serverLoad.getCpuLoad() - avgCpuLoad), 2);
        }
        return Math.sqrt(sumOfDifferences/serversLoad.size());
    }

    /**
     * Returns the average memory load in a collection of server loads.
     *
     * @param serversLoad the load of the servers
     * @return the average memory load
     */
    private static double calculateAvgMemLoad(Collection<ServerLoad> serversLoad) {
        double sum = 0;
        for (ServerLoad serverLoad: serversLoad) {
            sum += serverLoad.getRamLoad();
        }
        return sum/serversLoad.size();
    }

    /**
     * Returns the standard deviation of the memory load in a collection of server loads.
     *
     * @param serversLoad the load of the servers
     * @return the standard deviation of the memory load
     */
    public static double calculateStDevMemLoad(Collection<ServerLoad> serversLoad) {
        double sumOfDifferences = 0;
        double avgMemLoad = calculateAvgMemLoad(serversLoad);
        for (ServerLoad serverLoad: serversLoad) {
            sumOfDifferences += Math.pow((serverLoad.getRamLoad() - avgMemLoad), 2);
        }
        return Math.sqrt(sumOfDifferences/serversLoad.size());
    }

    /**
     * Returns the average disk load in a collection of server loads.
     *
     * @param serversLoad the load of the servers
     * @return the average disk load
     */
    private static double calculateAvgDiskLoad(Collection<ServerLoad> serversLoad) {
        double sum = 0;
        for (ServerLoad serverLoad: serversLoad) {
            sum += serverLoad.getDiskLoad();
        }
        return sum/serversLoad.size();
    }

    /**
     * Returns the standard deviation of the disk load in a collection of server loads.
     *
     * @param serversLoad the load of the servers
     * @return the standard deviation of the disk load
     */
    public static double calculateStDevDiskLoad(Collection<ServerLoad> serversLoad) {
        double sumOfDifferences = 0;
        double avgDiskLoad = calculateAvgDiskLoad(serversLoad);
        for (ServerLoad serverLoad: serversLoad) {
            sumOfDifferences += Math.pow((serverLoad.getDiskLoad() - avgDiskLoad), 2);
        }
        return Math.sqrt(sumOfDifferences/serversLoad.size());
    }

    /**
     * Returns the load for each host after a deployment plan is executed.
     *
     * @param deploymentPlan the deployment plan
     * @param hosts the hosts
     * @return the load for each host
     */
    public static Map<String, ServerLoad> getServersLoadsAfterDeploymentPlanExecuted(DeploymentPlan deploymentPlan,
            List<HostInfo> hosts) {
        Map<String, ServerLoad> serversLoad = new HashMap<>();

        // Initialize the Map with the current server load of each host
        for (HostInfo host: hosts) {
            serversLoad.put(host.getHostname(), new ServerLoad(host.getServerLoad().getCpuLoad(),
                    host.getServerLoad().getRamLoad(), host.getServerLoad().getDiskLoad()));
        }

        // Update the map according to the deployment plan
        for (VmAssignmentToHost vmAssignmentToHost: deploymentPlan.getVmsAssignationsToHosts()) {
            Vm vmAssigned = vmAssignmentToHost.getVm();
            HostInfo deploymentHost = vmAssignmentToHost.getHost();
            double newCpuLoad = serversLoad.get(deploymentHost.getHostname()).getCpuLoad()
                    + (double) vmAssigned.getCpus()/deploymentHost.getTotalCpus();
            double newRamLoad = serversLoad.get(deploymentHost.getHostname()).getRamLoad()
                    + vmAssigned.getRamMb()/deploymentHost.getTotalMemoryMb();
            double newDiskLoad = serversLoad.get(deploymentHost.getHostname()).getDiskLoad()
                    + vmAssigned.getDiskGb()/deploymentHost.getTotalDiskGb();
            serversLoad.get(deploymentHost.getHostname()).setCpuLoad(newCpuLoad);
            serversLoad.get(deploymentHost.getHostname()).setRamLoad(newRamLoad);
            serversLoad.get(deploymentHost.getHostname()).setDiskLoad(newDiskLoad);
        }

        return serversLoad;
    }

    /**
     * Decides on which host deploy each of the VMs that need to be deployed. When there are no hosts that
     * satisfy the requirements for a specific VM, that VM is deployed in a host chosen randomly.
     *
     * @param vms the description of the VMs that need to be scheduled
     * @param hosts information of the hosts of the infrastructure where the VMs need to be deployed
     * @return HashMap that contains for each VM description, the name of the host where
     * the VM should be deployed according to the scheduling algorithm
     */
    public Map<Vm, String> schedule(List<Vm> vms, List<HostInfo> hosts) {
        Map<Vm, String> scheduling = new HashMap<>(); // HashMap VM -> host where it is going to be deployed

        // For each of the VMs to be scheduled
        for (Vm vm: vms) {
            // Get hosts with enough resources
            List<HostInfo> hostsWithEnoughResources = HostFilter.filter(hosts, vm.getCpus(),
                    vm.getRamMb(), vm.getDiskGb());

            // Choose the host to deploy the VM
            String selectedHost = chooseHost(hosts, hostsWithEnoughResources, vm);

            // Add the host to the result
            scheduling.put(vm, selectedHost);

            // Reserve the resources that the VM needs
            reserveResourcesForVmInHost(vm, selectedHost, hostsWithEnoughResources);
        }

        return scheduling;
    }

    /**
     * Returns the best deployment plan from a list of deployment plans. The deployment plan chosen depends
     * on the algorithm used (distribution, consolidation, energy-aware, etc.).
     *
     * @param deploymentPlans the deployment plans
     * @param hosts the hosts of the infrastructure
     * @return the best deployment plan according to the algorithm applied
     */
    public DeploymentPlan chooseBestDeploymentPlan(List<DeploymentPlan> deploymentPlans, List<HostInfo> hosts) {
        DeploymentPlan bestDeploymentPlan = null;
        for (DeploymentPlan deploymentPlan: deploymentPlans) {
            if (bestDeploymentPlan == null ||
                    schedAlgorithm.isBetterDeploymentPlan(deploymentPlan, bestDeploymentPlan, hosts)) {
                bestDeploymentPlan = deploymentPlan;
            }
        }
        return bestDeploymentPlan;
    }




}