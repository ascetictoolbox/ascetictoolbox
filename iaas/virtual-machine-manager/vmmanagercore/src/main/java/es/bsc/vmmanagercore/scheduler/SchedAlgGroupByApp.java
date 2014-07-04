package es.bsc.vmmanagercore.scheduler;

import es.bsc.vmmanagercore.model.DeploymentPlan;
import es.bsc.vmmanagercore.model.Vm;
import es.bsc.vmmanagercore.model.VmDeployed;
import es.bsc.vmmanagercore.monitoring.HostInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Scheduling algorithm that groups VMs by the application which they belong to.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class SchedAlgGroupByApp implements SchedAlgorithm {

    private List<VmDeployed> vmsDeployed;
    private List<HostInfo> hostsInfo;

    public SchedAlgGroupByApp(List<VmDeployed> vmsDeployed) {
        this.vmsDeployed = vmsDeployed;
    }

    private boolean hostHasEnoughResources(String hostname) {
        for (HostInfo hostInfo: hostsInfo) {
            if (hostname.equals(hostInfo.getHostname())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a hashmap that contains, for each host, the number of VMs that belong to the same
     * application as the one that we need to deploy.
     */
    private Map<String, Integer> getNumberOfVmsThatBelongToTheAppForEachHost(String app) {
        Map<String, Integer> vmsOfAppPerHost = new HashMap<>();
        for (VmDeployed vmDeployed: vmsDeployed) {
            // Increase the counter if it is a VM that belongs to the same application
            if (vmDeployed.getApplicationId().equals(app)) {
                if (vmsOfAppPerHost.get(vmDeployed.getHostName()) == null) {
                    vmsOfAppPerHost.put(vmDeployed.getHostName(), 1);
                }
                else {
                    vmsOfAppPerHost.put(vmDeployed.getHostName(), vmsOfAppPerHost.get(vmDeployed.getHostName()) + 1);
                }
            }
        }
        return vmsOfAppPerHost;
    }

    /**
     * Get the host with more VMs of the same app. The hosts needs to have enough resources available.
     */
    private String getHostWithMoreVmsOfTheApp(Map<String, Integer> vmsOfAppPerHost) {
        String host = null;
        int maxNumberOfVMsOfApp = 0;
        for (Map.Entry<String, Integer> entry : vmsOfAppPerHost.entrySet()) {
            if (hostHasEnoughResources(entry.getKey()) && (entry.getValue() >= maxNumberOfVMsOfApp || host == null)) {
                host = entry.getKey();
                maxNumberOfVMsOfApp = entry.getValue();
            }
        }
        return host;
    }

    @Override
    public String chooseHost(List<HostInfo> hostsInfo, Vm vm) {
        this.hostsInfo = hostsInfo;
        Map<String, Integer> vmsOfAppPerHost = getNumberOfVmsThatBelongToTheAppForEachHost(vm.getApplicationId());
        return getHostWithMoreVmsOfTheApp(vmsOfAppPerHost);
    }

    @Override
    public boolean isBetterDeploymentPlan(DeploymentPlan deploymentPlan1, DeploymentPlan deploymentPlan2,
            List<HostInfo> hosts) {
        return false;
    }
}
