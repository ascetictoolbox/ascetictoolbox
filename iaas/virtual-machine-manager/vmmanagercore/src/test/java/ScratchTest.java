import es.bsc.vmmanagercore.configuration.VmManagerConfiguration;
import es.bsc.vmmanagercore.modellers.energy.dummy.DummyEnergyModeller;
import es.bsc.vmmanagercore.modellers.price.dummy.DummyPricingModeller;
import es.bsc.vmmanagercore.models.scheduling.SchedAlgorithmNameEnum;
import es.bsc.vmmanagercore.models.vms.Vm;
import es.bsc.vmmanagercore.models.vms.VmDeployed;
import es.bsc.vmmanagercore.monitoring.hosts.*;
import es.bsc.vmmanagercore.monitoring.zabbix.ZabbixConnector;
import es.bsc.vmmanagercore.scheduler.Scheduler;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.*;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.Host;
import org.apache.commons.configuration.ConfigurationException;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ScratchTest {
	@BeforeClass
	public static void setupVMConfig() {
		System.setProperty("config", "configTest.properties");
		VmManagerConfiguration.getInstance();
	}

	@Ignore
	@Test
	public void testZabbixScratch() {
		List<es.bsc.vmmanagercore.monitoring.hosts.Host> vmmHosts = new ArrayList<>();

		System.out.println("****** HOSTS *****");
		int hosts = 0;
		for(Host h : ZabbixConnector.getZabbixClient().getAllHosts()) {
			if("1".equals(h.getAvailable())) {
				try {
					HostZabbix hz = new HostZabbix(h.getHost());
					vmmHosts.add(hz);
					System.out.println(hz.toString());
				} catch(IllegalArgumentException e) {
					// not registered
				}
			}
			if(++hosts > 5) break;
		}


		Scheduler scheduler = new Scheduler(
				SchedAlgorithmNameEnum.DISTRIBUTION,
				new ArrayList<VmDeployed>(),
				new DummyEnergyModeller(),
				new DummyPricingModeller());

		List<Vm> vms = new ArrayList<>();
		for(int i = 0 ; i < 3 ; i++) {
			vms.add(new Vm("testId_"+i,"img",1+i/2,128,1,null,"testApp"));
		}
		scheduler.chooseBestDeploymentPlan(vms, vmmHosts);



	}
}
