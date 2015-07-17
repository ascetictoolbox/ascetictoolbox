/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.test;

import java.sql.Timestamp;
import java.util.List;
import java.util.Vector;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.builder.EnergyModellerFactory;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.ApplicationSample;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.EventSample;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.Unit;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.service.EnergyModellerService;

public class EnergyModellerData {

//	
//	HST 4c097560-9381-4356-bcae-687719725da7_asok12
//	HST 07027d74-dcfa-455d-b2b9-8b7a2471b1bd_asok12
//	HST 7c8a3fc0-393d-4124-856e-05aedb2d23ca_asok12
	
	private static EnergyModellerService serviceEM;
//	private static String HOST = "c0fcc44e-b29f-4fcc-b1e3-b9c017b7865f";
//	private static String HOST2 = "d9864885-77db-4a92-ab6b-feec3e5eded6";
//	private static String HOST3 = "708834af-4e90-424b-9dc2-c2fe1542918f";
//	private static String EVENT = "core0impl0";
//	private static String APP = "JEPlus";
	// for 
	private static String HOST = "c0fcc44e-b29f-4fcc-b1e3-b9c017b7865f";
	private static String HOST2 = "2d915d73-8999-420c-b2b0-2ac853052b7e";
	private static String HOST3 = "b52da74d-585c-404d-8f29-4de0d93cfe5e";

	private static String EVENT = "TestScript15-oracle";
	private static String APP = "NewsAsset";
	
	long beginlong = 1419246350742L;
	long endlong = 1419246415273L;
	
	
	@BeforeClass
	public static void setup() {
		serviceEM = (EnergyModellerService) EnergyModellerFactory.getEnergyModeller("c:/dev-env/ascetic-conf/testconfig.properties");
		
	}
	
	
	@Test
	public void testEnergyForEvent() {
		System.out.println("Testing energy estimation");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST2);
//		vmids.add(HOST3);
//		double energy  = serviceEM.energyEstimation(null, APP, vmids, EVENT);
//		System.out.println("--------------------------------------------RESULT:"+energy);
//		Assert.assertNotNull(energy);
	}
	
//	@Test
//	public void testEnergyForApp() {
//		System.out.println("Test Energy App Event");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST2);
//		vmids.add(HOST3);
//		double energy  = serviceEM.energyApplicationConsumption(null, APP, vmids, null);
//		System.out.println("--------------------------------------------RESULT:"+energy);
//		Assert.assertNotNull(energy);
//	}
	


//	@Test
//	public void testEnergyForEventTime() {
//		System.out.println("Test");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST2);
//		vmids.add(HOST3);
//		double energy = serviceEM.applicationConsumptionInInterval(null, APP, vmids, null, Unit.ENERGY, new Timestamp(beginlong), new Timestamp(endlong));
//		System.out.println("-- Res " +energy );
//		Assert.assertNotNull(energy);
//	
//	}

//	@Test
//	public void testEnergyForAppTime() {
//		System.out.println("Test");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST2);
//		vmids.add(HOST3);
//		double energy = serviceEM.applicationConsumptionInInterval(null, APP, vmids, null, "energy", new Timestamp(beginlong), new Timestamp(endlong));
//		System.out.println("-- Res " +energy );
//		Assert.assertNotNull(energy);
//	
//	}
	
//	@Test
//	public void testAppSample() {
//		System.out.println("Test Sample");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST2);
//		vmids.add(HOST3);
//		List<ApplicationSample> energy = serviceEM.applicationData(null, APP, vmids, 10 , new Timestamp(beginlong),new Timestamp(endlong) );
//		for (ApplicationSample as : energy){
//			System.out.println(as.export());
//		}
//		Assert.assertNotNull(energy);
//	}
	
	
//	@Test
//	public void testEnergyWithinInterval() {
//		System.out.println("Testing energy estimation");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST2);
//		vmids.add(HOST3);
//
//		List<EventSample> results = serviceEM.eventsData( null, APP,vmids, EVENT,new Timestamp(beginlong),new Timestamp(endlong));	
//		
//		for (EventSample es : results){
//			System.out.println(es.export());
//		}
//	}

//	@Test
//	public void testMeasureEventEnergyInterface() {
//		System.out.println("Testing energy estimation");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST2);
//		vmids.add(HOST3);	
//		
//		double result = serviceEM.measure(null, APP, vmids, EVENT, Unit.ENERGY, new Timestamp(beginlong),new Timestamp(endlong));
//		System.out.println("Testing energy estimation gave "+result);
//	}
	
//	@Test
//	public void testMeasureEventPowerInterface() {
//		System.out.println("Testing energy estimation");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST2);
//		vmids.add(HOST3);	
//		
//		double result = serviceEM.measure(null, APP, vmids, EVENT, Unit.ENERGY, new Timestamp(beginlong),new Timestamp(endlong));
//		System.out.println("Testing energy estimation gave "+result);
//	}
		
//	@Test
//	public void testMeasureAppEnergyInterface() {
//		System.out.println("Testing energy estimation");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST2);
//		vmids.add(HOST3);	
//		
//		double result = serviceEM.measure(null, APP, vmids, EVENT, Unit.ENERGY, new Timestamp(beginlong),new Timestamp(endlong));
//		System.out.println("Testing energy estimation gave "+result);
//	}	

//	@Test
//	public void testMeasureAppPowerInterface() {
//		System.out.println("Testing energy estimation");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST2);
//		vmids.add(HOST3);	
//		
//		double result = serviceEM.measure(null, APP, vmids, null, Unit.POWER, new Timestamp(beginlong),new Timestamp(endlong));
//		System.out.println("Testing energy estimation gave "+result);
//	}
	
//	@Test
//	public void testMeasureAppEnergyAllInterface() {
//		System.out.println("Testing energy estimation");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST2);
//		vmids.add(HOST3);	
//		
//		double result = serviceEM.measure(null, APP, vmids, null, Unit.ENERGY, null , null);
//		System.out.println("Testing energy estimation gave "+result);
//	}	

}
