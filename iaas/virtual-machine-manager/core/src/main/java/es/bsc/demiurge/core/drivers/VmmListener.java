package es.bsc.demiurge.core.drivers;

import es.bsc.demiurge.core.models.vms.Vm;
import es.bsc.demiurge.core.models.vms.VmDeployed;

/**
 *
 * The VM Manager component does not catch any exception when calling the next methods.
 * It's the responsibility of the
 * implementor to avoid the next methods trowing any exception to not interrupt the normal
 * operation of the VMM.
 *
 * Created by mmacias on 3/11/15.
 */
public interface VmmListener {
	void onVmDeployment(VmDeployed vm);
	void onVmDestruction(VmDeployed vm);
	void onVmMigration(VmDeployed vm);
	void onVmAction(VmDeployed vm, VmAction action);

	void onPreVmDeployment(Vm vm);
}
