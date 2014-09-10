package eu.ascetic.asceticarchitecture.paas.component.energymodeller.interfaces;

import java.util.List;

public interface DataCollectorTaskInterface {
	
	public void setup();
	
	public void handleEventData(String applicationid,String deploymentid,String eventid);
	
	public void handleEventData(String applicationid,String deploymentid, List<String> vm,String eventid);
	
	public void handleConsumptionData(String applicationid,String deploymentid);
	
	public void handleConsumptionData(String applicationid, List<String> vm,String deploymentid);
	
}
