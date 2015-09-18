/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.dataservice;

import java.sql.Timestamp;
import java.util.List;

import org.apache.log4j.Logger;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.dao.impl.DataEventDAOImpl;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.table.DataEvent;

public class EventDataAggregatorService {

	private DataEventDAOImpl daoEvent;
	private ApplicationMonitoringDataService eventCollectorService;
	private static final Logger logger = Logger.getLogger(EventDataAggregatorService.class);


	public List<DataEvent> getEvents(String app, String depl, String vmid, String event, Timestamp start,Timestamp end) {
		eventCollectorService.handleEventData(app, depl, vmid, event);
		if((start==null)&&(end==null)){
			List<DataEvent> events = daoEvent.getByDeployId(app,depl,vmid,event);
			if (events==null)return null;
			logger.info("##################### Total events "+events.size()+"from " + vmid + " event" + event);
			return events;
		} else {
			List<DataEvent> events = daoEvent.getByDeployIdTime(app,depl,vmid,event,start,end);
			if (events==null)return null;
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
		// set dao
		eventCollectorService.setDataevent(daoEvent);
		logger.info("#set the event dao");
		// setup
		eventCollectorService.setup();
		logger.info("#setup of event collector completed!");
	}
	
	public void setDaoEvent(DataEventDAOImpl daoEvent) {
		this.daoEvent = daoEvent;
	}

}
