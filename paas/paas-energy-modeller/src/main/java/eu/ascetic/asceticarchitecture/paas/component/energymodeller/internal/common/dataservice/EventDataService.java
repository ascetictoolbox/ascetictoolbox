package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.dataservice;

import java.sql.Timestamp;
import java.util.List;
import java.util.Vector;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.dao.DataeEventDAO;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.table.DataEvent;

public class EventDataService implements DataeEventDAO {

	private List<DataEvent> eventData;

	public EventDataService() {
		eventData = new Vector<DataEvent>();
	}
	
	public EventDataService(List<DataEvent> eventData) {
		this.eventData = eventData;
	}
	
	
	@Override
	public void save(DataEvent data) {
		eventData.add(data);
	}
	
	@Override
	public List<DataEvent> getByApplicationIdTime(String applicationid,String vmid, String eventid, Timestamp start, Timestamp end) {
		List<DataEvent> resultSet = new Vector<DataEvent>();
		
		for (DataEvent de : eventData){
			
			if ( (de.getApplicationid().equals(applicationid))&& (de.getVmid().equals(vmid))&& (de.getEventid().equals(eventid))){
				// ok events referred to the right app/vm/dep
				if ( (de.getBegintime()>=start.getTime()) && (de.getBegintime()<=end.getTime()) ){
					resultSet.add(de);
				}
				
			}
		}
		
		return resultSet;
	}

	@Override
	public List<DataEvent> getByApplicationId(String applicationid, String vmid, String eventid) {
		List<DataEvent> resultSet = new Vector<DataEvent>();
		
		for (DataEvent de : eventData){
			
			if ( (de.getApplicationid().equals(applicationid))&& (de.getVmid().equals(vmid))&& (de.getEventid().equals(eventid))){
				// ok events referred to the right app/vm/dep
					resultSet.add(de);
				
			}
		}
		
		return resultSet;
		
	}

	@Override
	public List<DataEvent> getByDeployIdTime(String applicationid, String deploymentid, String vmid, String eventid, Timestamp start, Timestamp end) {
		List<DataEvent> resultSet = new Vector<DataEvent>();
		
		for (DataEvent de : eventData){
			
			if ( (de.getApplicationid().equals(applicationid))&& (de.getVmid().equals(vmid)) && (de.getEventid().equals(eventid)) && (de.getDeploymentid().equals(deploymentid)) && (de.getEventid().equals(eventid))){
				// ok events referred to the right app/vm/dep
				if ( (de.getBegintime()>=start.getTime()) && (de.getBegintime()<=end.getTime()) ){
					resultSet.add(de);
				}
				
			}
		}
		
		return resultSet;
	}
	
	@Override
	public List<DataEvent> getByDeployId(String applicationid, String deploymentid, String vmid, String eventid) {
		List<DataEvent> resultSet = new Vector<DataEvent>();
		
		for (DataEvent de : eventData){
			
			if ( (de.getApplicationid().equals(applicationid))&& (de.getVmid().equals(vmid)) && (de.getEventid().equals(eventid)) && (de.getDeploymentid().equals(deploymentid)) && (de.getEventid().equals(eventid))){
					resultSet.add(de);
			}
		}
		
		return resultSet;
	}
	

}
