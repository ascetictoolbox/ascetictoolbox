package eu.ascetic.asceticarchitecture.paas.component.energymodeller.test;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.ascetic.asceticarchitecture.paas.component.common.model.EnergyInterpolator;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.builder.EnergyModellerFactory;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.interfaces.PaaSEnergyModeller;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.service.EnergyModellerSimple;

public class EnergyModellerHandler {

	private static PaaSEnergyModeller serviceEM;
	
	@BeforeClass
	public static void setup() {
		serviceEM = EnergyModellerFactory.getEnergyModeller("c:/test/testconfig.properties");
		//serviceEM = new EnergyModellerSimple("c:/test/testconfig.properties");
	}
	
	@Test
	public void testEnergyModellerApplicationConsumption() {
		
		double energy = serviceEM.energyEstimationForVM("test", "app1", "10256", null);
		
		Assert.assertNotNull(energy);
	}
	
	
	

}
