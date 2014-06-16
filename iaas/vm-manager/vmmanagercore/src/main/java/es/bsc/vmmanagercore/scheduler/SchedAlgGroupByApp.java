package es.bsc.vmmanagercore.scheduler;

import es.bsc.vmmanagercore.model.Vm;
import es.bsc.vmmanagercore.model.VmDeployed;
import es.bsc.vmmanagercore.monitoring.HostInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class SchedAlgGroupByApp implements SchedAlgorithm {

    private ArrayList<VmDeployed> vmsDeployed;
    private ArrayList<HostInfo> hostsInfo;

    public SchedAlgGroupByApp(ArrayList<VmDeployed> vmsDeployed) {
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

    @Override
    public String chooseHost(ArrayList<HostInfo> hostsInfo, Vm vm) {
        this.hostsInfo = hostsInfo;
        String appOfVmToDeploy = vm.getApplicationId();
        // Host -> number of VMs that belong to the same application as the one that we need to deploy
        HashMap<String, Integer> vmsOfAppPerHost = new HashMap<>();

        // Calculate the number of VMs that belong to the app for each host
        for (VmDeployed vmDeployed: vmsDeployed) {
            // If it is a VM that belongs to the same application, increase the counter
            if (vmDeployed.getApplicationId().equals(appOfVmToDeploy)) {
                if (vmsOfAppPerHost.get(vmDeployed.getHostName()) == null) {
                    vmsOfAppPerHost.put(vmDeployed.getHostName(), 1);
                }
                else {
                    vmsOfAppPerHost.put(vmDeployed.getHostName(), vmsOfAppPerHost.get(vmDeployed.getHostName()) + 1);
                }
            }
        }

        // Get the host with more VMs of the same app. The hosts needs to have enough resources available.
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
}
