package es.bsc.demiurge.core.drivers;

import es.bsc.demiurge.core.clopla.domain.Host;
import es.bsc.demiurge.core.clopla.domain.Vm;
import es.bsc.demiurge.core.cloudmiddleware.CloudMiddlewareException;
import es.bsc.demiurge.core.models.scheduling.VmAssignmentToHost;
import es.bsc.demiurge.core.models.scheduling.DeploymentPlan;
import es.bsc.demiurge.core.models.vms.VmDeployed;

import java.util.List;
import java.util.Map;

/**
 * @author Mario Macías (http://github.com/mariomac)
 */
public interface Estimator {
    String getLabel();

	/**
	 * for deployment estimations
	 * @param vma
	 * @param vmsDeployed
	 * @param deploymentPlan
	 * @return
	 */
    double getDeploymentEstimation(VmAssignmentToHost vma, List<VmDeployed> vmsDeployed, DeploymentPlan deploymentPlan);

	/**
	 * for current estimations
	 * @param vmId
	 * @param options
	 * @return
	 */
	double getCurrentEstimation(String vmId, Map options) throws CloudMiddlewareException; // for pricing modeler, options wil include the "false" value for undeployed

	/**
	 * For clopla estimations. *CAUTION* the parameters are not VMM-specific host and vm classes but Clopla ones.
	 * @param host
	 * @param vmsDeployedInHost
	 * @return
	 */
	double getCloplaEstimation(Host host, List<Vm> vmsDeployedInHost);
}
