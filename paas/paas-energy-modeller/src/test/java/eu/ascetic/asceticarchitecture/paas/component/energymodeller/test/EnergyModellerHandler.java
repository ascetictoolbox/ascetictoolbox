/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.test;

import java.util.List;
import java.util.Vector;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.builder.EnergyModellerFactory;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.interfaces.PaaSEnergyModeller;

public class EnergyModellerHandler {

	private static PaaSEnergyModeller serviceEM;
	
	@BeforeClass
	public static void setup() {
		serviceEM = EnergyModellerFactory.getEnergyModeller("c:/test/testconfig.properties");
		//serviceEM = new EnergyModellerSimple("c:/test/testconfig.properties");
	}
	
	
	@Test
	public void testEventConsumption(){
		
		//"HMMERpfam", "45"
		List<String> vms = new Vector<String>();
		vms.add("cdf624c1-1bc9-49dd-adc4-627fa25c1969");
		
		double est = serviceEM.energyApplicationConsumption(null, "HMMERpfam", vms, "allevents");
		System.out.println("Energy estim for event "+est);
	}
	
	
	

}
