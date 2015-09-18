/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.test;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.util.Assert;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.builder.EnergyModellerFactory;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.PaaSEMDatabaseManager;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.dataservice.legacy.ZabbixDataCollectorService;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.service.EnergyModellerServiceY1;

public class EnergyModellerDataZabbix {

	
	/**
	 * 
	 * 
	 * This test class is used as a test client for the EM
	 * 
	 */
	
	private static String HOST = "47d3b87d-536f-4769-9b02-c6c6b528dd08";
	private static String HOST2 = "eb60a3b8-7904-4ef1-bcc4-2a25fca8d8ff";
	private static String HOST3 = "cbd14f4b-09dc-4d03-a6b2-f5d4e3ecc502";
	private static String hstr ="wally152";
	private static ZabbixDataCollectorService dcollector;
	private static PaaSEMDatabaseManager dbmanager;
	
	long beginlong = 1418580703000L;
	long endlong = 1418587903000L;
	
	@BeforeClass
	public static void setup() {
		System.out.println("Preparing test");
		dbmanager = new PaaSEMDatabaseManager();
		dbmanager.setup("springtest.xml");
		dcollector = new ZabbixDataCollectorService();
		dcollector.setDataconumption(dbmanager.getDataConsumptionDAOImpl());
		dcollector.setup();
		System.out.println("Prepared test");
	}
	
	@Test
	public void searchHost() {
		System.out.println("Running test");
		List<String> hosts = dcollector.getHostsnames();
		for (String hst : hosts)System.out.println("HST "+hst);
		String result = dcollector.searchFullHostsname(HOST);
		System.out.println("HST "+result);
//		Assert.notNull(result);
		
	}
	
	@Test
	public void testVMDataAll() {
		System.out.println("################################## ");
		System.out.println("### ALL DATA FOR "+dcollector.searchFullHostsname(HOST));
		System.out.println("################################## ");
		dcollector.getHistoryForItemFrom("apptest","Power", dcollector.searchFullHostsname(HOST),0);
	}
	
//	@Test
//	public void testVMDataSamples() {
//		System.out.println("################################## ");
//		System.out.println("### SAMPLES DATA FOR "+dcollector.searchFullHostsname(HOST));
//		System.out.println("################################## ");
//		dcollector.getHistoryForItemSamples("apptest","deptest","Power", dcollector.searchFullHostsname(HOST), 100);
//	}
//	
//	@Test
//	public void testVMInterval() {
//		System.out.println("################################## ");
//		System.out.println("### INTERVAL DATA FOR "+dcollector.searchFullHostsname(HOST));
//		System.out.println("################################## ");
//		dcollector.getHistoryForItemInterval("apptest","deptest","Power", dcollector.searchFullHostsname(HOST), beginlong, endlong);
//		 
//	}
//	
//	@Test
//	public void testVMCPU() {
//		System.out.println("################################## ");
//		System.out.println("### INTERVAL DATeA FOR "+dcollector.searchFullHostsname(HOST));
//		System.out.println("################################## ");
//		dcollector.getHistoryForItemSamples("apptest","deptest","vm.memory.size[total]", dcollector.searchFullHostsname(HOST), 100);
//	}
//	@Test
//	public void testVMMemory() {
//		System.out.println("################################## ");
//		System.out.println("### INTERVAL DATA FOR "+dcollector.searchFullHostsname(HOST));
//		System.out.println("################################## ");
//		//dcollector.getSeriesHistoryForItemInterval("apptest","deptest","CPU user time", dcollector.searchFullHostsname(HOST), 1412179349000L, 1412870549000L);
//		dcollector.getHistoryForItemSamples("apptest","deptest","system.cpu.load[percpu,avg15]", dcollector.searchFullHostsname(HOST), 100);
//	}
//	
//	@Test
//	public void testVMfrom() {
//		System.out.println("################################## ");
//		System.out.println("### FROM DATA FOR "+dcollector.searchFullHostsname(HOST));
//		System.out.println("################################## ");
//		dcollector.getHistoryForItemFrom("apptest","deptest","Power", dcollector.searchFullHostsname(HOST), 1412870549000L);
	
//	}

}
