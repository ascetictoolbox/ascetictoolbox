/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.dataservice;

import java.sql.Timestamp;
import java.util.List;

import org.apache.log4j.Logger;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.table.DataEvent;

public class EventDataAggregatorService {

	//private DataEventDAOImpl daoEvent;
	private ApplicationMonitoringDataService eventCollectorService;
	private EventDataService eventDataManager;
	
	
	private static final Logger logger = Logger.getLogger(EventDataAggregatorService.class);


	public List<DataEvent> getEvents(String app, String depl, String vmid, String event, Timestamp start,Timestamp end) {
		eventDataManager = new EventDataService(eventCollectorService.generateEventData(app, depl, vmid, event));
		if((start==null)&&(end==null)){
			List<DataEvent> events = eventDataManager.getByDeployId(app,depl,vmid,event);
			logger.info("##################### Total events "+events.size()+" from " + vmid + " event" + event);
			return events;
		} else {
			List<DataEvent> events = eventDataManager.getByDeployIdTime(app,depl,vmid,event,start,end);
			logger.info("##################### Total is "+events.size());
			return events;
		}
	}
	
	public void setupApplicationMonitor(String url){
		// crea
		logger.info("# initializing the event collector component");
		this.eventCollectorService = new ApplicationMonitoringDataService();
		// set path url
		eventCollectorService.setAMPath(url);
		logger.info("# setting path of AMonitor " +url); 
		// setup
		eventCollectorService.setup();
		logger.info("#setup of event collector completed!");
	}
	

}
