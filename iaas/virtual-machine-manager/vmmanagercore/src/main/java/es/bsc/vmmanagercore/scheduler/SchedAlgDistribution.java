package es.bsc.vmmanagercore.scheduler;

import es.bsc.vmmanagercore.model.DeploymentPlan;
import es.bsc.vmmanagercore.model.ServerLoad;
import es.bsc.vmmanagercore.model.Vm;
import es.bsc.vmmanagercore.model.VmAssignmentToHost;
import es.bsc.vmmanagercore.monitoring.HostInfo;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Distribution scheduling algorithm.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class SchedAlgDistribution implements SchedAlgorithm {

    Logger logger = LogManager.getLogger(SchedAlgDistribution.class);

    public SchedAlgDistribution() {}

    @Override
    public String chooseHost(List<HostInfo> hostsInfo, Vm vm) {
        logger.debug("\n [VMM] ---DISTRIBUTION ALG. START--- \n " +
                "Applying distribution algorithm to schedule VM " + vm.toString());

        ServerLoad minFutureLoad = new ServerLoad(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
        String selectedHost = null;

        //for each host
        for (HostInfo hostInfo: hostsInfo) {

            //calculate the future load (%) of the host if the VM is deployed in that host
            ServerLoad futureServerLoad = hostInfo.getFutureLoadIfVMDeployed(vm);
            logger.debug("[VMM] The load of host " + hostInfo.getHostname() + " would be "
                    + futureServerLoad.toString());

            //check if the host will have the lowest load after deploying the VM
            boolean lessCpu = futureServerLoad.getCpuLoad() < minFutureLoad.getCpuLoad();
            boolean sameCpuLessMemory = (futureServerLoad.getCpuLoad() == minFutureLoad.getCpuLoad())
                    && (futureServerLoad.getRamLoad() < minFutureLoad.getRamLoad());
            boolean sameCpuSameMemoryLessDisk = (futureServerLoad.getCpuLoad() == minFutureLoad.getCpuLoad())
                    && (futureServerLoad.getRamLoad() == minFutureLoad.getRamLoad())
                    && (futureServerLoad.getDiskLoad() < minFutureLoad.getDiskLoad());

            //if the host will be the least loaded according to the specified criteria (CPU more
            //important than memory, and memory more important than disk), save it
            if (lessCpu || sameCpuLessMemory || sameCpuSameMemoryLessDisk) {
                selectedHost = hostInfo.getHostname();
                minFutureLoad = futureServerLoad;
            }

        }

        logger.debug("[VMM] VM " + vm.toString() + " is going to be deployed in " + selectedHost +
            "\n [VMM] ---DISTRIBUTION ALG. END---");

        return selectedHost;
    }


    private Collection<ServerLoad> getServersLoadAfterDeploymentPlanExecuted(DeploymentPlan deploymentPlan,
            List<HostInfo> hosts) {
        Map<HostInfo, ServerLoad> serversLoad = new HashMap<>();

        // Initialize the Map with the current server load of each host
        for (HostInfo host: hosts) {
            serversLoad.put(host, host.getServerLoad());
        }

        // Update the map according to the deployment plan
        for (VmAssignmentToHost vmAssignmentToHost: deploymentPlan.getVmsAssignationsToHosts()) {
            Vm vmAssigned = vmAssignmentToHost.getVm();
            HostInfo deploymentHost = vmAssignmentToHost.getHost();
            double newCpuLoad = serversLoad.get(deploymentHost).getCpuLoad()
                    + vmAssigned.getCpus()/deploymentHost.getTotalCpus();
            double newRamLoad = serversLoad.get(deploymentHost).getRamLoad()
                    + vmAssigned.getCpus()/deploymentHost.getTotalMemoryMb();
            double newDiskLoad = serversLoad.get(deploymentHost).getDiskLoad()
                    + vmAssigned.getCpus()/deploymentHost.getTotalDiskGb();
            serversLoad.get(deploymentHost).setCpuLoad(newCpuLoad);
            serversLoad.get(deploymentHost).setRamLoad(newRamLoad);
            serversLoad.get(deploymentHost).setDiskLoad(newDiskLoad);
        }

        // Return just the loads
        return serversLoad.values();
    }

    public DeploymentPlan chooseBestDeploymentPlan(List<DeploymentPlan> deploymentPlans, List<HostInfo> hosts) {
        //TODO
        return null;
    }

}
