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

//	
//	HST cb65d79c-8f68-428e-9166-ece36ac27949_asok09
//	HST 73570473-e6f4-4f3a-9c65-4668c485a58d_asok09
//	HST 0e12ea48-7b26-4133-924c-6bf08c3ea067_asok09
//	HST 2d9f5a8f-da75-4ef0-aa9a-3c0747250a89_asok09
//	HST 87db8f11-ec86-4617-9dcf-16192a3b0668_asok09
//	HST 23025351-6bf2-47d6-b5c4-ddd2de804f86_asok09
//	HST 6ee74f86-1c39-412c-9866-94a49b3aed0d_asok09
	
	private static EnergyModellerSimple serviceEM;
	private static String HOST = "e33bf9f7-f500-4776-8016-3eca8d4817a5";
	private static String HOST2 = "16070ce5-9f09-4c6d-a7fc-fe5e814e46ba";
//	private static String HOST3 = "96a8e5ea-9335-4cec-96da-b5bb34349adb";
//	private static String HOST4 = "a54d58a7-4772-4106-8522-6adb0d8bdbf7";



	private static String EVENT = "TestSession1";
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
	@Test
	public void testEEnergyEstApp() {
		System.out.println("Test Energy App Event");
		List<String> vmids = new Vector<String>();
		vmids.add(HOST);
		vmids.add(HOST2);
//		vmids.add(HOST3);
//		vmids.add(HOST4);
		//vmids.add(HOST5);
		double energy  = serviceEM.energyApplicationConsumption(null, APP, vmids, null);
		System.out.println("--------------------------------------------RESULT:"+energy);
		Assert.assertNotNull(energy);
	}
	
	
//	@Test
//	public void testEstimationEnergyApp() {
//		System.out.println("Testing energy estimation");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		double energy  = serviceEM.estimation(null, APP, vmids, null,"energy");
//		System.out.println("--------------------------------------------ENERGY :"+energy);
//		Assert.assertNotNull(energy);
//	}
	
//	@Test
//	public void testEstimationPowerApp() {
//		System.out.println("Test Energy App Event");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		double energy  = serviceEM.estimation(null, APP, vmids, null,"power");
//		System.out.println("--------------------------------------------POWER :"+energy);
//		Assert.assertNotNull(energy);
//	}
	

	
//	@Test
//	public void testSampleForVM() {
//		System.out.println("Test Sample");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		List<Sample> energy = serviceEM.applicationData(null, APP, HOST, null , 1 , new Timestamp(1414104343000L),new Timestamp(1414108801000L) );
//		Assert.assertNotNull(energy);
//	}

//	@Test
//	public void testEEnergyForAppTime() {
//		System.out.println("Test ++++++++++++++++++++++LLLLLLLLLLLLLLLLLLLLLLLL");
//		double energy = serviceEM.energyApplicationConsumptionTimeInterval(null, APP, HOST, null, new Timestamp(1413549656000L), new Timestamp(1413555016000L));
//		System.out.println("--------------------------------------------RESULT:"+energy);
//		Assert.assertNotNull(energy);
//	
//	}
	
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
//		List<EnergySample> energy = serviceEM.energyApplicationConsumptionData(null, APP,  HOST, EVENT, new Timestamp(1414104343000L),new Timestamp(1414108801000L) );
//		System.out.println("Energy "+ energy.size());
//		for (EnergySample es : energy){
//			System.out.println("Energy sample "+ es.getE_value()+ " vmid "+es.getVmid()+ " ts "+es.getTimestampBeging()+ " po "+es.getP_value()) ;
//		}
//		Assert.assertNotNull(energy);
//	
//	}
	
//	@Test
//	public void testEEnergySamplesForTime() {
//	System.out.println("Test NEWW");
//		List<EnergySample> energy = serviceEM.energyApplicationConsumptionData(null, APP,  HOST, null, new Timestamp(1414104343000L),new Timestamp(1414108801000L) );
//		System.out.println("Energy from samplse: "+ energy.size());
//		for (EnergySample es : energy){
//			System.out.println("Energy sample "+ es.getE_value()+ " vmid "+es.getVmid()+ " ts "+es.getTimestampBeging()+ " po "+es.getP_value()) ;
//		}
//		Assert.assertNotNull(energy);
//	
//	}
	
}
