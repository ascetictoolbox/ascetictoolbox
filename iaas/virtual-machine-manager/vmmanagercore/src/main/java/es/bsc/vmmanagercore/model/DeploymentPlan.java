package es.bsc.vmmanagercore.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Deployment plan. A deployment plan is a list where each item contains a pair {vm, host}. The pair indicates on
 * which host a specific VM should be deployed.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class DeploymentPlan {

    private List<VmAssignmentToHost> vmsAssignationsToHosts = new ArrayList<>();

    public DeploymentPlan(List<VmAssignmentToHost> vmsAssignationsToHosts) {
        this.vmsAssignationsToHosts = vmsAssignationsToHosts;
    }

    public void addVmAssignmentToPlan(VmAssignmentToHost vmAssignmentToHost) {
        vmsAssignationsToHosts.add(vmAssignmentToHost);
    }

    public List<VmAssignmentToHost> getVmsAssignationsToHosts() {
        return new ArrayList<>(vmsAssignationsToHosts);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (VmAssignmentToHost vmAssignmentToHost: vmsAssignationsToHosts) {
            stringBuilder.append(vmAssignmentToHost.toString());
            stringBuilder.append(" ");
        }
        return stringBuilder.toString();
    }

}


