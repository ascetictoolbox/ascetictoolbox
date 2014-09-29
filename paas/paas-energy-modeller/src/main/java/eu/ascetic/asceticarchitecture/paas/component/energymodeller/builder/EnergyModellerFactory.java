/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.builder;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.interfaces.PaaSEnergyModeller;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.service.EnergyModellerSimple;

public class EnergyModellerFactory {
	
	public static PaaSEnergyModeller getEnergyModeller(String propertyFile){
			return (PaaSEnergyModeller) new EnergyModellerSimple(propertyFile);
	}

}
