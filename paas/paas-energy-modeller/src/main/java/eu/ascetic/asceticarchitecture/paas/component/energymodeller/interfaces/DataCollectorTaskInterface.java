package eu.ascetic.asceticarchitecture.paas.component.energymodeller.interfaces;

import java.sql.Timestamp;
import java.util.List;

public interface DataCollectorTaskInterface {
	
	public void setup();
	
	public void handleEventData(String applicationid,String deploymentid,String eventid);
	
	public void handleEventData(String applicationid,String deploymentid, List<String> vm,String eventid);
	
	public void handleEventDataInterval(String applicationid,String deploymentid, List<String> vm,String eventid,Timestamp start, Timestamp end);
	
	public void handleConsumptionData(String applicationid,String deploymentid);
	
	public void handleConsumptionData(String applicationid, List<String> vm,String deploymentid);
	
	public void handleConsumptionData(String applicationid, String vm,String deploymentid);
	
	public void handleConsumptionDataInterval(String applicationid, List<String> vm,String deploymentid, Timestamp start, Timestamp end);
	
}
