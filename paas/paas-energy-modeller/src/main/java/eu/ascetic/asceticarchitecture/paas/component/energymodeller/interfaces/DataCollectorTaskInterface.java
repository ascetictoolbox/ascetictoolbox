package eu.ascetic.asceticarchitecture.paas.component.energymodeller.interfaces;

public interface DataCollectorTaskInterface {
	
	public void setup();
	
	public void handleEventData();
	
	public void handleConsumptionData();
	
}
