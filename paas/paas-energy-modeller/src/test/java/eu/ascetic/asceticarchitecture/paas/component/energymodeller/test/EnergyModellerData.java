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
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.EnergySample;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.Sample;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.service.EnergyModellerSimple;

public class EnergyModellerData {

	private static EnergyModellerSimple serviceEM;
	private static String HOST = "dcf8e3eb-9cfd-4f34-a48b-2421fc9c423d";
	private static String EVENT = "core0impl0";
	private static String APP = "JEPlus";
	
	
	@BeforeClass
	public static void setup() {
		serviceEM = (EnergyModellerSimple) EnergyModellerFactory.getEnergyModeller("c:/test/testconfig.properties");
		
	}
	
	
//	@Test
//	public void testEEnergyForApp() {
//		System.out.println("Testing energy estimation");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		double energy  = serviceEM.energyEstimation(null, APP, vmids, null);
//		System.out.println("--------------------------------------------RESULT:"+energy);
//		Assert.assertNotNull(energy);
//	}
//	
//	@Test
//	public void testEEnergyEstApp() {
//		System.out.println("Test Energy App Event");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		double energy  = serviceEM.energyApplicationConsumption(null, APP, vmids, EVENT);
//		System.out.println("--------------------------------------------RESULT:"+energy);
//		Assert.assertNotNull(energy);
//	}
//
//	@Test
//	public void testEEnergyForEve() {
//		System.out.println("Testing energy estimation event");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		double energy  = serviceEM.energyEstimation(null, APP, vmids, EVENT);
//		System.out.println("--------------------------------------------RESULT:"+energy);
//		Assert.assertNotNull(energy);
//	}
	
	@Test
	public void testSampleForVM() {
		System.out.println("Test Sample");
		List<String> vmids = new Vector<String>();
		vmids.add(HOST);
		List<Sample> energy = serviceEM.applicationData(null, APP, HOST, null , 1 , new Timestamp(1413798058000L),new Timestamp(1413819659000L) );
		Assert.assertNotNull(energy);
	}

//	@Test
//	public void testEEnergyForAppTime() {
//		System.out.println("Test ++++++++++++++++++++++LLLLLLLLLLLLLLLLLLLLLLLL");
//		double energy = serviceEM.energyApplicationConsumptionTimeInterval(null, APP, HOST, null, new Timestamp(1413549656000L), new Timestamp(1413555016000L));
//		System.out.println("--------------------------------------------RESULT:"+energy);
//		Assert.assertNotNull(energy);
//	
//	}
//	
//	@Test
//	public void testEEnergyForEventTime() {
//		System.out.println("Test ++++++++++++++++++++++APP");
//		double energy = serviceEM.energyApplicationConsumptionTimeInterval(null, APP, HOST, EVENT, new Timestamp(1413549656000L), new Timestamp(1413555016000L));
//		System.out.println("--------------------------------------------RESULT:"+energy);
//		Assert.assertNotNull(energy);
//	
//	}
		
//	@Test
//	public void testEEnergySamplesForEventTime() {
//	System.out.println("Test 5");
//		List<EnergySample> energy = serviceEM.energyApplicationConsumptionData(null, APP,  HOST, EVENT, new Timestamp(1413549656000L), new Timestamp(1413555016000L));
//		System.out.println("Energy "+ energy.size());
//		for (EnergySample es : energy){
//			System.out.println("Energy sample "+ es.getValue()+ " vmid "+es.getVmid()+ " ts "+es.getTimestampBeging());
//		}
//		Assert.assertNotNull(energy);
//	
//	}
	
//	@Test
//	public void testEEnergySamplesForTime() {
//	System.out.println("Test NEWW");
//		List<EnergySample> energy = serviceEM.energyApplicationConsumptionData(null, APP,  HOST, null, new Timestamp(1413549656000L), new Timestamp(1413555016000L));
//		System.out.println("Energy from samplse: "+ energy.size());
//	
//		Assert.assertNotNull(energy);
//	
//	}
	
}
