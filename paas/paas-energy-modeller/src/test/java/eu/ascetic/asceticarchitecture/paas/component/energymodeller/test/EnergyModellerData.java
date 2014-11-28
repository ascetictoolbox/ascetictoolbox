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
//	HST 4c097560-9381-4356-bcae-687719725da7_asok12
//	HST 07027d74-dcfa-455d-b2b9-8b7a2471b1bd_asok12
//	HST 7c8a3fc0-393d-4124-856e-05aedb2d23ca_asok12
	
	private static EnergyModellerSimple serviceEM;
	//private static String HOST = "9d9864885-77db-4a92-ab6b-feec3e5eded6";
	//private static String HOST2 = "c919174d-c60b-4108-9d36-21530ae7ba8d";
	private static String HOST = "d9864885-77db-4a92-ab6b-feec3e5eded6";
	//private static String HOST2 = "c919174d-c60b-4108-9d36-21530ae7ba8d";
	private static String HOST2 = "708834af-4e90-424b-9dc2-c2fe1542918f";
//	private static String HOST4 = "a54d58a7-4772-4106-8522-6adb0d8bdbf7";

	private static String EVENT = "core0impl0";
	private static String APP = "JEPlus";

	//private static String EVENT = "TestEvent2";
	//private static String APP = "NewsAsAssets";
	
	
	@BeforeClass
	public static void setup() {
		serviceEM = (EnergyModellerSimple) EnergyModellerFactory.getEnergyModeller("c:/test/testconfig.properties");
		
	}
	
	
	@Test
	public void testEEnergyForApp() {
		System.out.println("Testing energy estimation");
		List<String> vmids = new Vector<String>();
		vmids.add(HOST);
		vmids.add(HOST2);
		//vmids.add(HOST3);s
		double energy  = serviceEM.energyEstimation(null, APP, vmids, EVENT);
		System.out.println("--------------------------------------------RESULT:"+energy);
		Assert.assertNotNull(energy);
	}
	
//	@Test
//	public void testEEnergyEstApp() {
//		System.out.println("Test Energy App Event");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST2);
////		vmids.add(HOST3);
////		vmids.add(HOST4);
//		//vmids.add(HOST5);
//		double energy  = serviceEM.energyApplicationConsumption(null, APP, vmids, null);
//		System.out.println("--------------------------------------------RESULT:"+energy);
//		Assert.assertNotNull(energy);
//	}
	
	
//	@Test
//	public void testEstimationEnergyApp() {
//		System.out.println("Testing energy estimation");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST2);
//		//vmids.add(HOST3);
//
//		double energy  = serviceEM.estimation(null, APP, vmids, null,"energy");
//		System.out.println("--------------------------------------------ENERGY :"+energy);
//		Assert.assertNotNull(energy);
//	}
//	
//	@Test
//	public void testEstimationPowerApp() {
//		System.out.println("Test Energy App Event");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST2);
//		//vmids.add(HOST3);
//		double energy  = serviceEM.estimation(null, APP, vmids, EVENT,"energy");
//		System.out.println("--------------------------------------------POWER :"+energy);
//		Assert.assertNotNull(energy);
//	}
	

	
//	@Test
//	public void testSampleForVM() {
//		System.out.println("Test Sample");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST2);
//		vmids.add(HOST3);
//		List<Sample> energy = serviceEM.applicationData(null, APP, HOST, EVENT , 1 , new Timestamp(1416412633000L),new Timestamp(1416414133000L) );
//		Assert.assertNotNull(energy);
//	}

//	@Test
//	public void testEEnergyForAppTime() {
//		System.out.println("Test ++++++++++++++++++++++LLLLLLLLLLLLLLLLLLLLLLLL");
//		double energy = serviceEM.energyApplicationConsumptionTimeInterval(null, APP, HOST, null, new Timestamp(1416412633000L), new Timestamp(1416414133000L));
//		double energy2 = serviceEM.energyApplicationConsumptionTimeInterval(null, APP, HOST2, null, new Timestamp(1416412633000L), new Timestamp(1416414133000L));
//		double energy3 = serviceEM.energyApplicationConsumptionTimeInterval(null, APP, HOST3, null, new Timestamp(1416412633000L), new Timestamp(1416414133000L));
//		System.out.println("--------------------------------------------RESULT VM "+HOST+" :"+energy);
//		System.out.println("--------------------------------------------RESULT:VM "+HOST2+" :"+energy2);
//		System.out.println("--------------------------------------------RESULT:VM "+HOST3+" :"+energy3);
//		Assert.assertNotNull(energy);
//	
//	}
//	
//	@Test
//	public void testEEnergyForEventTime() {
//		System.out.println("Test ++++++++++++++++++++++LLLLLLLLLLLLLLLLLLLLLLLL");
//		double energy = serviceEM.energyApplicationConsumptionTimeInterval(null, APP, HOST, EVENT, new Timestamp(1416412633000L), new Timestamp(1416414133000L));
//		double energy2 = serviceEM.energyApplicationConsumptionTimeInterval(null, APP, HOST2, EVENT, new Timestamp(1416412633000L), new Timestamp(1416414133000L));
//		double energy3 = serviceEM.energyApplicationConsumptionTimeInterval(null, APP, HOST3, EVENT, new Timestamp(1416412633000L), new Timestamp(1416414133000L));
//		System.out.println("--------------------------------------------RESULT VM "+HOST+" :"+energy);
//		System.out.println("--------------------------------------------RESULT:VM "+HOST2+" :"+energy2);
//		System.out.println("--------------------------------------------RESULT:VM "+HOST3+" :"+energy3);
//	
//	}
		
//	@Test
//	public void testEEnergySamplesForEventTime() {
//	System.out.println("Test 5");
//		List<EnergySample> energy = serviceEM.energyApplicationConsumptionData(null, APP,  HOST, EVENT, new Timestamp(1416412633000L),new Timestamp(1416414133000L) );
//		System.out.println("Energy "+ energy.size());
//		for (EnergySample es : energy){
//			System.out.println("Energy sample "+ es.getE_value()+ " vmid "+es.getVmid()+ " ts "+es.getTimestampBeging()+ " po "+es.getP_value()) ;
//		}
//		List<EnergySample> energy2 = serviceEM.energyApplicationConsumptionData(null, APP,  HOST2, EVENT, new Timestamp(1416412633000L),new Timestamp(1416414133000L) );
//		System.out.println("Energy "+ energy2.size());
//		for (EnergySample es : energy2){
//			System.out.println("Energy sample "+ es.getE_value()+ " vmid "+es.getVmid()+ " ts "+es.getTimestampBeging()+ " po "+es.getP_value()) ;
//		}
//		List<EnergySample> energy3 = serviceEM.energyApplicationConsumptionData(null, APP,  HOST3, EVENT, new Timestamp(1416412633000L),new Timestamp(1416414133000L) );
//		System.out.println("Energy "+ energy3.size());
//		for (EnergySample es : energy3){
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
