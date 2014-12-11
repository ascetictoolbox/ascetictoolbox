/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.service.dconnector;

import java.sql.Timestamp;
import java.util.List;

import org.apache.log4j.Logger;

import eu.ascetic.asceticarchitecture.paas.component.common.dao.impl.DataEventDAOImpl;
import eu.ascetic.asceticarchitecture.paas.component.common.database.table.DataEvent;

public class EventDataAggregatorService {

	private DataEventDAOImpl daoEvent;
	private static final Logger logger = Logger.getLogger(EventDataAggregatorService.class);

	public double getTotal(String app, String depl, String vmid, String event) {
		double total = daoEvent.getEventCountVM(app, depl, vmid, event);
		logger.info("Total is "+total);
		return total;
	}
	
	public List<DataEvent> getEvents(String app, String depl, String vmid, String event) {

		List<DataEvent> events = daoEvent.getByApplicationId(app,vmid,event);
		if (events==null)return null;
		logger.info("##################### Total events "+events.size()+"from " + vmid + " event" + event);
		return events;
	}
	
	public List<DataEvent> getEventsInTime(String app, String depl, String vmid, String event,Timestamp start,Timestamp end) {

		List<DataEvent> events = daoEvent.getByApplicationIdTime(app,vmid,event,start,end);
		if (events==null)return null;
		logger.info("##################### Total is "+events.size());
		return events;
	}

	
	public void setDaoEvent(DataEventDAOImpl daoEvent) {
		this.daoEvent = daoEvent;
	}



}
