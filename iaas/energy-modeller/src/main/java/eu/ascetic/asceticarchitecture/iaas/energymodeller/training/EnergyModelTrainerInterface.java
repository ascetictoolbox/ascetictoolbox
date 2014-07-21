package eu.ascetic.asceticarchitecture.iaas.energymodeller.training;

import java.util.ArrayList;
import java.util.HashMap;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.TimePeriod;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.HostEnergyCalibrationData;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energymodel.EnergyModel;


public interface EnergyModelTrainerInterface {
	
	public boolean trainModel (Host host, double usageCPU, double usageRAM, double wattsUsed, int numberOfValues, TimePeriod duration);
	//public int trainModel (Host host, double usageCPU, double usageRAM, double totalEnergyUsed, boolean print);
	
	public EnergyModel retrieveModel (Host host);
	
	
	
	
}