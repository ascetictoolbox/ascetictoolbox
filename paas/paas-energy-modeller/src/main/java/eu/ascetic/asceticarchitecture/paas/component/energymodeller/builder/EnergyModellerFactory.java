/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.builder;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.interfaces.PaaSEnergyModeller;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.service.EnergyModellerService;

public class EnergyModellerFactory {
	
	public static PaaSEnergyModeller getEnergyModeller(String propertyFile){
			return (PaaSEnergyModeller) new EnergyModellerService(propertyFile);
	}

}
