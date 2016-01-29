package es.bsc.demiurge.renewit.modellers;

import es.bsc.demiurge.core.clopla.domain.Host;
import es.bsc.demiurge.core.clopla.domain.Vm;
import es.bsc.demiurge.core.drivers.Estimator;
import es.bsc.demiurge.core.models.scheduling.DeploymentPlan;
import es.bsc.demiurge.core.models.scheduling.VmAssignmentToHost;
import es.bsc.demiurge.core.models.vms.VmDeployed;

import java.util.List;
import java.util.Map;

/**
 * Dummy energy modeller. To be implemented by Mauro's model
 *
 * @author Mario Macias (http://github.com/mariomac), Mauro Canuto (mauro.canuto@bsc.es)
 */
public class PowerModeller implements Estimator {

	private double DUMMY_POWER_PER_CPU_CORE = 30;
	private double DUMMY_POWER_PER_IDLE_HOST = 50;

	@Override
	public String getLabel() {
		return "power";
	}

	/**
	 * By the moment, calculates dummy power
	 * @param vma
	 * @param vmsDeployed
	 * @param deploymentPlan THIS IS BY THE MOMENT IGNORED. I THINK IT IS USED ONLY BY ASCETIC
	 * @return
	 */
	// TODO: check if deploymenplan is ascetic-specific, and must be removed from the core
	// TODO: mix getDeploymentEstimation and getCloplaEstimation
	@Override
	public double getDeploymentEstimation(VmAssignmentToHost vma, List<VmDeployed> vmsDeployed, DeploymentPlan deploymentPlan) {
		double pow = DUMMY_POWER_PER_IDLE_HOST + vma.getVm().getCpus() * DUMMY_POWER_PER_CPU_CORE;
		for(VmDeployed vm : vmsDeployed) {
			pow += vm.getCpus() * DUMMY_POWER_PER_CPU_CORE;
		}
		return pow;
	}

	@Override
	public double getCurrentEstimation(String vmId, Map options) {
		throw new AssertionError("this should be never called. VMM is configured for Clopla and must get rid of legacy scheduler");
	}

	@Override
	public double getCloplaEstimation(Host host, List<Vm> vmsDeployedInHost) {
		double pow = DUMMY_POWER_PER_IDLE_HOST;
		for(Vm vm : vmsDeployedInHost) {
			pow += vm.getNcpus() * DUMMY_POWER_PER_CPU_CORE;
		}
		return pow;
	}
}
