package eu.ascetic.asceticarchitecture.iaas.energymodeller.training;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energymodel.EnergyModel;

public interface EnergyModelTrainerInterface {
	
	public int trainModel (Host host, double usageCPU, double usageRAM, double totalEnergyUsed);
	
	public EnergyModel retrieveModel (Host host);
	
	
	
	
}