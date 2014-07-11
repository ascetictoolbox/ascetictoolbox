package es.bsc.vmmanagercore.scheduler.schedulingalgorithms;

import es.bsc.vmmanagercore.logging.VMMLogger;
import es.bsc.vmmanagercore.model.DeploymentPlan;
import es.bsc.vmmanagercore.model.Vm;
import es.bsc.vmmanagercore.model.VmAssignmentToHost;
import es.bsc.vmmanagercore.model.VmDeployed;
import es.bsc.vmmanagercore.monitoring.Host;

import java.util.ArrayList;
import java.util.List;

/**
 * Scheduling algorithm that groups VMs by the application which they belong to.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class SchedAlgGroupByApp implements SchedAlgorithm {

    private List<VmDeployed> vmsDeployed;

    public SchedAlgGroupByApp(List<VmDeployed> vmsDeployed) {
        this.vmsDeployed = vmsDeployed;
    }

    private List<VmDeployed> getVmsDeployedInHost(String hostName) {
        List<VmDeployed> result = new ArrayList<>();
        for (VmDeployed vm: vmsDeployed) {
            if (vm.getHostName().equals(hostName)) {
                result.add(vm);
            }
        }
        return result;
    }

    private int getNumberOfVmsThatBelongToAppInHost(String appId, String hostName) {
        int result = 0;
        for (VmDeployed vm: getVmsDeployedInHost(hostName)) {
            if (vm.getApplicationId().equals(appId)) {
                ++result;
            }
        }
        return result;
    }

    private int getVmsSameAppInSameHost(DeploymentPlan deploymentPlan) {
        int result = 0;
        for (int i = 0; i < deploymentPlan.getVmsAssignationsToHosts().size(); ++i) {
            Vm vm1 = deploymentPlan.getVmsAssignationsToHosts().get(i).getVm();
            Host host1 = deploymentPlan.getVmsAssignationsToHosts().get(i).getHost();
            for (int j = i + 1; j < deploymentPlan.getVmsAssignationsToHosts().size(); ++j) {
                Vm vm2 = deploymentPlan.getVmsAssignationsToHosts().get(j).getVm();
                Host host2 = deploymentPlan.getVmsAssignationsToHosts().get(j).getHost();
                boolean sameAppId = vm1.getApplicationId().equals(vm2.getApplicationId());
                boolean sameHost = host1.getHostname().equals(host2.getHostname());
                if (sameAppId && sameHost) {
                    ++result;
                }
            }
        }
        return result;
    }

    private int getVmsSameAppInSameHostForDeploymentPlan(DeploymentPlan deploymentPlan) {
        int result = 0;
        for (VmAssignmentToHost vmAssignmentToHost: deploymentPlan.getVmsAssignationsToHosts()) {
            result += getNumberOfVmsThatBelongToAppInHost(vmAssignmentToHost.getVm().getApplicationId(),
                    vmAssignmentToHost.getHost().getHostname());
        }
        return result + getVmsSameAppInSameHost(deploymentPlan);
    }

    @Override
    public boolean isBetterDeploymentPlan(DeploymentPlan deploymentPlan1, DeploymentPlan deploymentPlan2,
            List<Host> hosts) {
        int vmsSameAppInSameHostPlan1 = getVmsSameAppInSameHostForDeploymentPlan(deploymentPlan1);
        int vmsSameAppInSameHostPlan2 = getVmsSameAppInSameHostForDeploymentPlan(deploymentPlan2);
        VMMLogger.logVmsSameAppInSameHost(1, vmsSameAppInSameHostPlan1);
        VMMLogger.logVmsSameAppInSameHost(2, vmsSameAppInSameHostPlan2);
        return vmsSameAppInSameHostPlan1 >= vmsSameAppInSameHostPlan2;
    }
}
