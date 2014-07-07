package es.bsc.vmmanagercore.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class DeploymentPlan {
    private List<VmAssignmentToHost> vmsAssignationsToHosts = new ArrayList<>();

    public DeploymentPlan(List<VmAssignmentToHost> vmsAssignationsToHosts) {
        this.vmsAssignationsToHosts = vmsAssignationsToHosts;
    }

    public void addVmAssignmentToPlan(VmAssignmentToHost vmAssigmentToHost) {
        vmsAssignationsToHosts.add(vmAssigmentToHost);
    }

    public List<VmAssignmentToHost> getVmsAssignationsToHosts() {
        return new ArrayList<>(vmsAssignationsToHosts);
    }

    @Override
    public String toString() {
        String result = "";
        for (VmAssignmentToHost vmAssignmentToHost: vmsAssignationsToHosts) {
            result += vmAssignmentToHost.getVm().getName() + "-->" + vmAssignmentToHost.getHost().getHostname() + ", ";
        }
        return result;
    }

}


