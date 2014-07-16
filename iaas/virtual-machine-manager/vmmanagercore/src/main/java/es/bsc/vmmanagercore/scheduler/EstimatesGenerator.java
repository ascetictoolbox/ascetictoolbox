package es.bsc.vmmanagercore.scheduler;

import es.bsc.vmmanagercore.model.*;

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
    public ListVmEstimates getVmEstimates(DeploymentPlan deploymentPlan, List<VmDeployed> vmsDeployed) {
        List<VmEstimate> vmEstimates = new ArrayList<>();
        for (VmAssignmentToHost vmAssignmentToHost: deploymentPlan.getVmsAssignationsToHosts()) {
            vmEstimates.add(vmAssignmentToHost.getVmEstimate(vmsDeployed));
        }
        return new ListVmEstimates(vmEstimates);
    }

}
