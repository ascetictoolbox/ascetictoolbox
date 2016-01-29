package es.bsc.demiurge.monitoring.zabbix;

import es.bsc.demiurge.core.drivers.VmAction;
import es.bsc.demiurge.core.drivers.VmmListener;
import es.bsc.demiurge.core.models.vms.Vm;
import es.bsc.demiurge.core.models.vms.VmDeployed;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Created by mmacias on 19/11/15.
 */
public class ZabbixListener implements VmmListener {
	private Logger log = LogManager.getLogger(ZabbixListener.class);
	@Override
	public void onVmDeployment(VmDeployed vm) {
		log.debug("Registering VM " + vm.getId() + " in zabbix");
		ZabbixConnector.registerVmInZabbix(vm.getId(), vm.getHostName(), vm.getIpAddress());

	}
	@Override
	public void onVmDestruction(VmDeployed vm) {
		try {
			log.debug("Deleting VM " + vm.getId() + " from zabbix");

			ZabbixConnector.deleteVmFromZabbix(vm.getId(), vm.getHostName());
		} catch(Exception e) {
			log.error(e.getMessage(),e);
		}
	}
	@Override
	public void onVmMigration(VmDeployed vm) {
		ZabbixConnector.migrateVmInZabbix(vm.getId(), vm.getIpAddress());
	}
	@Override
	public void onVmAction(VmDeployed vm, VmAction action) {}

	@Override
	public void onPreVmDeployment(Vm vm) {}
}
