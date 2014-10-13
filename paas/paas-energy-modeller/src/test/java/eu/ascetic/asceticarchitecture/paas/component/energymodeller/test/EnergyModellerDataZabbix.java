/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.test;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.util.Assert;

import eu.ascetic.asceticarchitecture.paas.component.common.database.PaaSEMDatabaseManager;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.builder.EnergyModellerFactory;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.service.EnergyModellerSimple;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.service.task.DataCollector;

public class EnergyModellerDataZabbix {

	private static String HOST = "cdf624c1-1bc9-49dd-adc4-627fa25c1969";
	private static DataCollector dcollector;
	private static PaaSEMDatabaseManager dbmanager;
	
	@BeforeClass
	public static void setup() {
		dbmanager = new PaaSEMDatabaseManager();
		dbmanager.setup("springtest.xml");
		dcollector = new DataCollector();
		dcollector.setDataconumption(dbmanager.getDataConsumptionDAOImpl());
		dcollector.setup();
	}
	
	@Test
	public void searchHost() {
		
		List<String> hosts = dcollector.getHostsnames();
		//for (String hst : hosts)System.out.println("HST "+hst);
		String result = dcollector.searchFullHostsname(HOST);
		System.out.println("HST "+result);
		Assert.notNull(result);
		
		
	}
	
	@Test
	public void testVMDataAll() {
		System.out.println("################################## ");
		System.out.println("### ALL DATA FOR "+dcollector.searchFullHostsname(HOST));
		System.out.println("################################## ");
		dcollector.getHistoryForItem("apptest","deptest","Power", dcollector.searchFullHostsname(HOST),5);
	}
	
	@Test
	public void testVMDataSamples() {
		System.out.println("################################## ");
		System.out.println("### SAMPLES DATA FOR "+dcollector.searchFullHostsname(HOST));
		System.out.println("################################## ");
		dcollector.getHistoryForItemSamples("apptest","deptest","Power", dcollector.searchFullHostsname(HOST), 100);
	}
	
	@Test
	public void testVMInterval() {
		System.out.println("################################## ");
		System.out.println("### INTERVAL DATA FOR "+dcollector.searchFullHostsname(HOST));
		System.out.println("################################## ");
		dcollector.getHistoryForItemInterval("apptest","deptest","Power", dcollector.searchFullHostsname(HOST), 1412179349000L, 1412870549000L);
	}
	
	@Test
	public void testVMfrom() {
		System.out.println("################################## ");
		System.out.println("### FROM DATA FOR "+dcollector.searchFullHostsname(HOST));
		System.out.println("################################## ");
		dcollector.getHistoryForItemFrom("apptest","deptest","Power", dcollector.searchFullHostsname(HOST), 1412870549000L);
	}

}
