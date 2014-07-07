package es.bsc.vmmanagercore.scheduler;

import es.bsc.vmmanagercore.model.*;
import es.bsc.vmmanagercore.monitoring.Host;

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
            List<Host> hosts) {
        Map<String, ServerLoad> serversLoad = new HashMap<>();

        // Initialize the Map with the current server load of each host
        for (Host host: hosts) {
            serversLoad.put(host.getHostname(), new ServerLoad(host.getServerLoad().getCpuLoad(),
                    host.getServerLoad().getRamLoad(), host.getServerLoad().getDiskLoad()));
        }

        // Update the map according to the deployment plan
        for (VmAssignmentToHost vmAssignmentToHost: deploymentPlan.getVmsAssignationsToHosts()) {
            Vm vmAssigned = vmAssignmentToHost.getVm();
            Host deploymentHost = vmAssignmentToHost.getHost();
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
     * Returns the best deployment plan from a list of deployment plans. The deployment plan chosen depends
     * on the algorithm used (distribution, consolidation, energy-aware, etc.).
     *
     * @param deploymentPlans the deployment plans
     * @param hosts the hosts of the infrastructure
     * @return the best deployment plan according to the algorithm applied
     */
    public DeploymentPlan chooseBestDeploymentPlan(List<DeploymentPlan> deploymentPlans, List<Host> hosts) {
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