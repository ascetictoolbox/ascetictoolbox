/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.service.task;

import java.sql.Timestamp;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import eu.ascetic.asceticarchitecture.paas.component.common.dao.impl.DataEventDAOImpl;
import eu.ascetic.asceticarchitecture.paas.component.common.model.DataEvent;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.EnergySamples;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.interfaces.DataAggregatorTaskInterface;

public class EventDataAggregatorService implements DataAggregatorTaskInterface {

	private DataEventDAOImpl daoEvent;
	private static final Logger logger = Logger.getLogger(EventDataAggregatorService.class);
	//private long CONVERSIONTIME_MILLISEC = 3600000;
	
	

	@Override
	public double getTotal(String app, String depl, String vmid, String event) {
		double total = daoEvent.getEventCountVM(app, depl, vmid, event);
		logger.info("Total is "+total);
		return total;
	}
	
	public List<DataEvent> getEvents(String app, String depl, String vmid, String event) {

		List<DataEvent> events = daoEvent.getByApplicationId(app);
		logger.info("##################### Total events "+events.size());
		return events;
	}
	
	public List<DataEvent> getEventsInTime(String app, String depl, String vmid, String event,Timestamp start,Timestamp end) {

		List<DataEvent> events = daoEvent.getByApplicationIdTime(app,start,end);
		logger.info("##################### Total is "+events.size());
		return events;
	}

	
	public void setDaoEvent(DataEventDAOImpl daoEvent) {
		this.daoEvent = daoEvent;
	}

	@Override
	public double getAverage(String app, String depl, String vmid, String event) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getAverageInInterval(String app,  String vmid, String event, long start, long end) {
		// TODO Auto-generated method stub
		return 0;
	}

//	@Override
//	public List<EnergySamples> getSamplesInInterval(String app, String depl,String vmid, String event, Timestamp start, Timestamp end, long freq) {
//		// TODO Auto-generated method stub
//		return null;
//	}

}
