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
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.EnergySamples;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.service.EnergyModellerSimple;

public class EnergyModellerData {

	private static EnergyModellerSimple serviceEM;
	private static String HOST = "cdf624c1-1bc9-49dd-adc4-627fa25c1969";
	
	@BeforeClass
	public static void setup() {
		serviceEM = (EnergyModellerSimple) EnergyModellerFactory.getEnergyModeller("c:/test/testconfig.properties");
		
	}
	
	
	@Test
	public void testEEnergyForApp() {
		System.out.println("Test 1");
		List<String> vmids = new Vector<String>();
		vmids.add(HOST);
		double energy  = serviceEM.energyApplicationConsumption("providerid", "HMMERpfam", vmids, null);
		Assert.assertNotNull(energy);
	}
	
	@Test
	public void testEEnergyForEvent() {
		System.out.println("Test 2");
		List<String> vmids = new Vector<String>();
		vmids.add(HOST);
		double energy = serviceEM.energyApplicationConsumption("providerid", "HMMERpfam", vmids, "testevent");
		Assert.assertNotNull(energy);
	}
	
	@Test
	public void testEEnergyForAppTime() {
		System.out.println("Test 3");
		double energy = serviceEM.energyApplicationConsumptionTimeInterval("providerid", "HMMERpfam", HOST, null, new Timestamp(1413117929000L), new Timestamp(1413118709000L));
		Assert.assertNotNull(energy);
	
	}
	
	@Test
	public void testEEnergyForEventTime() {
		System.out.println("Test 4");
		double energy = serviceEM.energyApplicationConsumptionTimeInterval("providerid", "HMMERpfam", HOST, "eventtype", new Timestamp(1413117929000L), new Timestamp(1413118709000L));
		Assert.assertNotNull(energy);
	
	}
	
	@Test
	public void testEEnergySamplesForEventTime() {
	System.out.println("Test 5");
		List<EnergySamples> energy = serviceEM.energyApplicationConsumptionData("providerid", "HMMERpfam",  HOST, "eventtype", new Timestamp(1413117929000L), new Timestamp(1413118709000L));
		System.out.println("Energy "+ energy.size());
		for (EnergySamples es : energy){
			System.out.println("Energy sample "+ es.getValue()+ " vmid "+es.getVmid()+ " ts "+es.getTimestampBeging());
		}
		Assert.assertNotNull(energy);
	
	}
	

}
