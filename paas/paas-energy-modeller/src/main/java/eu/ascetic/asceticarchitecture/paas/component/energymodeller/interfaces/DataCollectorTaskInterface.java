package eu.ascetic.asceticarchitecture.paas.component.energymodeller.interfaces;

public interface DataCollectorTaskInterface {
	
	public void setup();
	
	public void handleEventData(String applicationid,String deploymentid,String eventid);
	
	public void handleConsumptionData(String applicationid,String deploymentid);
	
}
