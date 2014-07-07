package eu.ascetic.asceticarchitecture.iaas.energymodeller.training;

public interface EnergyModelTrainer {
	
	public void TrainModelForHost (Host host, Collection<VM> virtualMachines);
	
}