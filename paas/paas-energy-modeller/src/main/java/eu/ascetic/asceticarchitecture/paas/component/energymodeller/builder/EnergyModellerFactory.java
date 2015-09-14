/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.builder;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.interfaces.PaaSEnergyModeller;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.service.EnergyModellerService;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.service.EnergyModellerServiceY1;

public class EnergyModellerFactory {
	
	private static PaaSEnergyModeller theEnergyModeller;
	
	public static PaaSEnergyModeller getEnergyModeller(String propertyFile){
		if (theEnergyModeller==null) {
			theEnergyModeller = (PaaSEnergyModeller) new EnergyModellerService(propertyFile);
			return theEnergyModeller;
		} else {
			return theEnergyModeller;
		}
	}
	
	// DEPRECATED: just for backword compatibility, but going to be removed soon
	@Deprecated
	public static PaaSEnergyModeller getY1EnergyModeller(String propertyFile){
		if (theEnergyModeller==null) {
			theEnergyModeller = (PaaSEnergyModeller) new EnergyModellerServiceY1(propertyFile);
			return theEnergyModeller;
		} else {
			return theEnergyModeller;
		}
	}
	

}
