/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.test;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.builder.EnergyModellerFactory;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.service.EnergyModellerSimple;

public class EnergyModellerData {

	private static EnergyModellerSimple serviceEM;
	
	@BeforeClass
	public static void setup() {
		serviceEM = (EnergyModellerSimple) EnergyModellerFactory.getEnergyModeller("c:/test/testconfig.properties");
		
	}
	
	@Test
	public void testEnergyModellerApplicationConsumption() {
		double energy = serviceEM.energyApplicationConsumption("providerid", "applicationid", "deploymentid");
		
		Assert.assertNotNull(energy);
	}
	
	@Test
	public void testEnergyModellerEnergyEstimation() {
		double energy = serviceEM.energyEstimation("providerid", "applicationid", "deploymentid",null);
		Assert.assertNotNull(energy);
	}

	@Test
	public void testEnergyModellerEnergyEstimationEvent() {
		String appid = "applicationID_deploymentID";
		double energy = serviceEM.energyEstimation("providerid", appid, "null","null");
		Assert.assertNotNull(energy);
	}
	
	@Test
	public void testEnergyModellerStartModelling() {
		boolean started = serviceEM.startModellingApplicationEnergy("providerid", "applicationid", "deploymentid");
		Assert.assertTrue(started);
	}
	
	@Test
	public void testEnergyModellerEndModelling() {
		boolean ended = serviceEM.stopModellingApplicationEnergy("providerid", "applicationid", "deploymentid");
		Assert.assertTrue(ended);
	}
	
	@Test
	public void trainApplication() {
		boolean training = serviceEM.trainApplication("providerid", "applicationid", "deploymentid",null);
		Assert.assertTrue(training);
	}
	
	@Test
	public void trainEvent() {
		boolean training = serviceEM.trainApplication("providerid", "HMMERpfam", "45","event");
		Assert.assertTrue(training);
	}
	

}
