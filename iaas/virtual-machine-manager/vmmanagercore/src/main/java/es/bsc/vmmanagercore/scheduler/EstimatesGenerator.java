package es.bsc.vmmanagercore.scheduler;

import es.bsc.vmmanagercore.model.DeploymentPlan;
import es.bsc.vmmanagercore.model.VmAssignmentToHost;
import es.bsc.vmmanagercore.model.VmDeployed;
import es.bsc.vmmanagercore.model.VmEstimate;

import java.util.ArrayList;
import java.util.List;

/**
 *
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class EstimatesGenerator {

    /**
     * Returns price and energy estimates for each VM in a deployment plan.
     *
     * @param deploymentPlan the deployment plan
     * @param vmsDeployed VMs deployed in the infrastructure
     * @return the price and energy estimates for each VM
     */
    public List<VmEstimate> getVmEstimates(DeploymentPlan deploymentPlan, List<VmDeployed> vmsDeployed) {
        List<VmEstimate> result = new ArrayList<>();
        for (VmAssignmentToHost vmAssignmentToHost: deploymentPlan.getVmsAssignationsToHosts()) {
            result.add(vmAssignmentToHost.getVmEstimate(vmsDeployed));
        }
        return result;
    }

}
