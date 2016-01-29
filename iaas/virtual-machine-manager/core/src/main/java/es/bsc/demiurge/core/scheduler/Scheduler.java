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

import es.bsc.demiurge.core.manager.components.EstimatesManager;
import es.bsc.demiurge.core.models.scheduling.DeploymentPlan;
import es.bsc.demiurge.core.models.vms.Vm;
import es.bsc.demiurge.core.models.vms.VmDeployed;
import es.bsc.demiurge.core.scheduler.schedulingalgorithms.SchedAlgorithm;
import es.bsc.demiurge.core.models.hosts.ServerLoad;
import es.bsc.demiurge.core.models.scheduling.VmAssignmentToHost;
import es.bsc.demiurge.core.monitoring.hosts.Host;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 *  Scheduler that decides where to place the VMs that need to be deployed.
 *  This scheduler can be configured to use different scheduling algorithms (consolidation, distribution, etc.)
 * 
 *  @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 * @deprecated Scheduler and SchedAlgorithm classes have been replaced by Clopla
 */
@Deprecated()
public class Scheduler {

    private SchedAlgorithm schedAlgorithm;
    private List<VmDeployed> vmsDeployed;
    private EstimatesManager estimatorsManager;
	private SchedulingAlgorithmsRepository schedulingAlgorithmsRepository;
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss.SSS"); // Useful for logs

    private Logger logger = LogManager.getLogger(Scheduler.class);
    /**
     * Class constructor.
     *
     * @param schedAlg scheduling algorithm used
     * @param vmsDeployed list of VMs deployed in the infrastructure
     */
    public Scheduler(String schedAlg, List<VmDeployed> vmsDeployed, EstimatesManager estimatorsManager,
					 SchedulingAlgorithmsRepository schedulingAlgorithmsRepository) {
        this.vmsDeployed = vmsDeployed;
        this.estimatorsManager = estimatorsManager;
		this.schedulingAlgorithmsRepository = schedulingAlgorithmsRepository;
        setSchedAlgorithm(schedAlg);
    }

    /**
     * Instantiates the scheduling algorithm class.
     *
     * @param name scheduling algorithm to be used
     */
    public synchronized void setSchedAlgorithm(String name) {

		schedAlgorithm = schedulingAlgorithmsRepository.newInstance(name);
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

    public static double getTotalUnusedCpuPerc(Collection<ServerLoad> serverLoads) {
        double result = 0;
        for (ServerLoad serverLoad: serverLoads) {
            result += serverLoad.getUnusedCpuPerc();
        }
        return result;
    }

    public static double getTotalUnusedMemPerc(Collection<ServerLoad> serverLoads) {
        double result = 0;
        for (ServerLoad serverLoad: serverLoads) {
            result += serverLoad.getUnusedRamPerc();
        }
        return result;
    }

    public static double getTotalUnusedDiskPerc(Collection<ServerLoad> serverLoads) {
        double result = 0;
        for (ServerLoad serverLoad: serverLoads) {
            result += serverLoad.getUnusedDiskPerc();
        }
        return result;
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
            serversLoad.put(deploymentHost.getHostname(), new ServerLoad(newCpuLoad, newRamLoad, newDiskLoad));
        }

        return serversLoad;
    }

    /**
     * Returns the total overload of a set of servers loads
     *
     * @param serverLoads the servers loads
     * @return the total overload
     */
    private double getServersOverload(Map<String, ServerLoad> serverLoads) {
        double result = 0;
        for (Map.Entry<String, ServerLoad> entry: serverLoads.entrySet()) {
            result += entry.getValue().getTotalOverload();
        }
        return result;
    }

    /**
     * Checks whether the hosts of a deployment plan are less overloaded than the servers of another deployment plan.
     *
     * @param deploymentPlan1 first deployment plan
     * @param deploymentPlan2 second deployment plan
     * @param hosts the hosts of the infrastructure
     * @return true if the hosts of the first deployment plan are less overloaded than the hosts of the second
     * deployment plan
     */
    private boolean serversAreLessOverloaded(DeploymentPlan deploymentPlan1, DeploymentPlan deploymentPlan2,
                List<Host> hosts) {
        Map<String, ServerLoad> serverLoadsPlan1 = getServersLoadsAfterDeploymentPlanExecuted(deploymentPlan1, hosts);
        Map<String, ServerLoad> serverLoadsPlan2 = getServersLoadsAfterDeploymentPlanExecuted(deploymentPlan2, hosts);
        return getServersOverload(serverLoadsPlan1) < getServersOverload(serverLoadsPlan2);
    }

    /**
     * Receives a list of deployment plans where all of them use overbooking and selects the best one.
     * Overbooking in our context means that there is at least a server that has been assigned VMs that
     * demand more resources than the resources available at the server.
     * We consider that the best deployment plan is the one where less VMs are assigned to hosts with overbooking.
     *
     * @param deploymentPlans possible deployment plans
     * @param hosts the hosts of the infrastructure
     * @return the best deployment plan
     */
    private DeploymentPlan findBestEffortDeploymentPlan(List<DeploymentPlan> deploymentPlans, List<Host> hosts) {
        DeploymentPlan bestPlan = null;
        for (DeploymentPlan deploymentPlan: deploymentPlans) {
            if (bestPlan == null || serversAreLessOverloaded(deploymentPlan, bestPlan, hosts)) {
                bestPlan = deploymentPlan;
            }
        }
        return bestPlan;
    }

    /**
     * Returns a unique ID for the deployment. This ID is used to identify the log messages that belong to a
     * particular deployment.
     *
     */
    private String getDeploymentIdForLogMessages() {
        return dateFormat.format(new Date());
    }

    /**
     * Returns the best deployment plan from a list of vms. The deployment plan chosen depends
     * on the algorithm used (distribution, consolidation, energy-aware, etc.).
     *
     * @param vms the VMs that need to be deployed
     * @param hosts the hosts of the infrastructure
     * @return the best deployment plan according to the algorithm applied
     */
    public DeploymentPlan chooseBestDeploymentPlan(List<Vm> vms, List<Host> hosts) {
        String deploymentId = getDeploymentIdForLogMessages();
        LogManager.getLogger(Scheduler.class).debug("[VMM] ***EVALUATION OF DEPLOYMENT PLANS STARTS: " + schedAlgorithm.getName() + " ***"
                + " --id:" + deploymentId);

        // Get all the possible plans that do not use overbooking
        List<DeploymentPlan> possibleDeploymentPlans =
                new DeploymentPlanGenerator().getPossibleDeploymentPlans(vms, hosts);

        // TODO. This is an ugly quick fix for Ascetic so asok10 is never used.
        possibleDeploymentPlans = DeploymentPlanFilterer.filterDeploymentPlansThatUseHost(
                possibleDeploymentPlans, "asok10");

        // Find the best deployment plan
        DeploymentPlan bestDeploymentPlan = schedAlgorithm.chooseBestDeploymentPlan(vmsDeployed,
                estimatorsManager,
                possibleDeploymentPlans, hosts, deploymentId);

        if (bestDeploymentPlan != null) {
            logger.debug("[VMM] chosen deployment plan: [ " + bestDeploymentPlan.toString() + "] --id:" + deploymentId);
        }
        else { // No plans could be chosen, so apply overbooking

            // TODO. Ugly fix included again to avoid deploying VMs to asok10
            bestDeploymentPlan = findBestEffortDeploymentPlan(
                    DeploymentPlanFilterer.filterDeploymentPlansThatUseHost(
                            new DeploymentPlanGenerator().getDeploymentPlansWithoutRestrictions(vms, hosts),
                            "asok10"),
                    hosts);
            logger.debug("[VMM] no plans could be applied without overbooking. --id:" + deploymentId);
        }

        logger.debug("[VMM] ***EVALUATION OF DEPLOYMENT PLANS ENDS: " + schedAlgorithm.getName() + " ***"
                + " --id:" + deploymentId);

        return bestDeploymentPlan;
    }

}