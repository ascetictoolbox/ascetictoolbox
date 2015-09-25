/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.test;

import java.sql.Timestamp;
import java.util.List;
import java.util.Vector;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.builder.EnergyModellerFactory;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.Unit;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.service.EnergyModellerService;


public class EnergyModellerData {

	
	private static EnergyModellerService serviceEM;
	private static String HOST = "1811";
	private static String HOST1 = "1812";
	private static String HOST2 = "1813";
	//private static String HOST3 = "b52da74d-585c-404d-8f29-4de0d93cfe5e";
	private static String PROVIDER = "provider1";
	private static String EVENT = "core0impl0";
	private static String APP = "JEPlus";
	private static String DEP = "524";
	long beginlong = 1442494327000L;
	long endlong = 1442496787000L;
	
	
	
	@BeforeClass
	public static void setup() {
		serviceEM = (EnergyModellerService) EnergyModellerFactory.getEnergyModeller("c:/dev-env/ascetic-conf/testconfig.properties");
	}
	// test for power consumption
	
	@Test
	public void testMeasureAllPoweryInterface() {
		System.out.println("Testing power measurement");
		List<String> vmids = new Vector<String>();
		vmids.add(HOST);
		vmids.add(HOST1);
		vmids.add(HOST2);
		PROVIDER=null;
		
		double result = serviceEM.measure(PROVIDER, APP, DEP, vmids, EVENT, Unit.POWER, null,null);
		System.out.println("Average Power from all samples is:  "+result);
	}

	@Test
	public void testMeasureAllEnergyInterface() {
		System.out.println("Testing energy estimationfrom all samples");
		List<String> vmids = new Vector<String>();
		vmids.add(HOST);
		vmids.add(HOST1);
		vmids.add(HOST2);
		//vmids.add(HOST1);
		PROVIDER=null;
		double result = serviceEM.measure(PROVIDER, APP, DEP, vmids, EVENT, Unit.ENERGY, null,null);
		System.out.println("Energy Wh from all samples is:  "+result);
	}
//	
//	@Test
//	public void testMeasureRangePoweryInterface() {
//		System.out.println("Testing power measurement over a time range");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST1);
//		PROVIDER=null;
//		double result = serviceEM.measure(PROVIDER, APP, DEP, vmids, null, Unit.POWER,new Timestamp(beginlong),new Timestamp(endlong));
//		System.out.println("Average Power in the time range is:  "+result);
//	}
//
//	@Test
//	public void testMeasureRangeEnergyInterface() {
//		System.out.println("Testing energy in a time period");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST1);
//		PROVIDER=null;
//		double result = serviceEM.measure(PROVIDER, APP, DEP, vmids, null, Unit.ENERGY, new Timestamp(beginlong),new Timestamp(endlong));
//		System.out.println("Energy Wh from time range:  "+result);
//	}
	
//	@Test
//	public void testMeasureEventAllPoweryInterface() {
//		System.out.println("Testing power measurement");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST1);
//		PROVIDER=null;
//		double result = serviceEM.measure(PROVIDER, APP, DEP, vmids, EVENT, Unit.POWER, null,null);
//		System.out.println("Average Power from all samples is:  "+result);
//	}
//
//	@Test
//	public void testMeasureEventAllEnergyInterface() {
//		System.out.println("Testing energy estimationfrom all samples");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST1);
//		PROVIDER=null;
//		double result = serviceEM.measure(PROVIDER, APP, DEP, vmids, EVENT, Unit.ENERGY, null,null);
//		System.out.println("Energy Wh from all samples is:  "+result);
//	}
	
//	@Test
//	public void testPredictPowerInterface() {
//		System.out.println("Predict energy estimationfrom all samples");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST1);
//		PROVIDER=null;
//		double result = serviceEM.estimate(PROVIDER, APP, DEP, vmids, null, Unit.POWER, 60);
//		System.out.println("Power estimation in "+60+" is :  "+result);
//	}
		
	
//	@Test
//	public void testMeasurePowerInterface() {
//		System.out.println("Testing power estimation between two defined timestamps");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST1);
//		
//		double result = serviceEM.measure(PROVIDER, APP, DEP, vmids, null, Unit.POWER, new Timestamp(beginlong),new Timestamp(endlong));
//		System.out.println("Average Power is:  "+result);
//	}
	
//	@Test
//	public void testMeasureAllPowerInterface() {
//		System.out.println("Testing power estimation with not time specified");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST1);
//		
//		double result = serviceEM.measure(APP, DEP, vmids, null, Unit.POWER, null,null);
//		System.out.println("Average Power is:  "+result);
//	}
//	
//	@Test
//	public void testMeasureSincePowerInterface() {
//		System.out.println("Testing power estimation with only a limit in time");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST1);
//		double result = serviceEM.measure(APP, DEP, vmids, null, Unit.POWER, null,new Timestamp(endlong));
//		System.out.println("Average Power is:  "+result);
//	}
	
//	@Test
//	public void testMeasureUpToPowerInterface() {
//		System.out.println("Testing power estimation with a start time");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST1);
//		double result = serviceEM.measure(APP, DEP, vmids, null, Unit.POWER, new Timestamp(beginlong),null);
//		System.out.println("Average Power is:  "+result);
//	}		
//	@Test
//	public void testAppSample() {
//		System.out.println("Test Sample");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST1);
//		vmids.add(HOST3);
//		List<ApplicationSample> energy = serviceEM.applicationData(null, APP, vmids, 10 , new Timestamp(beginlong),new Timestamp(endlong) );
//		for (ApplicationSample as : energy){
//			System.out.println(as.export());
//		}
//		Assert.assertNotNull(energy);
//	}
	
	// test energy estimation
	
//	@Test
//	public void testMeasurePowerInterface() {
//		System.out.println("Testing energy estimation between two defined timestamps");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST1);
//		
//		double result = serviceEM.measure(APP, DEP, vmids, null, Unit.ENERGY, new Timestamp(beginlong),new Timestamp(endlong));
//		System.out.println("Energy is:  "+result);
//	}	
//	@Test
//	public void testMeasureAllEnergyInterface() {
//		System.out.println("Testing energy estimation with not time specified");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST1);
//		
//		double result = serviceEM.measure(APP, DEP, vmids, null, Unit.ENERGY, null,null);
//		System.out.println("Energy is:  "+result);
//	}
//	
//	@Test
//	public void testMeasureSincePowerInterface() {
//		System.out.println("Testing energy estimation with only a limit in time");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST1);
//		double result = serviceEM.measure(APP, DEP, vmids, null, Unit.ENERGY, null,new Timestamp(endlong));
//		System.out.println("Energy is:  "+result);
//	}
//	@Test
//	public void testMeasureUpToPowerInterface() {
//		System.out.println("Testing energy estimation with a start time");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST1);
//		double result = serviceEM.measure(APP, DEP, vmids, null, Unit.ENERGY, new Timestamp(beginlong),null);
//		System.out.println("Energy is:  "+result);
//	}	
//	
	
	// test events consumption
	
	// other tests
	
	
//	@Test
//	public void testAppSample() {
//		System.out.println("Test Sample");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST1);
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
//		vmids.add(HOST1);
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
