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
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.interfaces.DataAggregatorTaskInterface;

public class EventDataAggregatorService implements DataAggregatorTaskInterface {

	private DataEventDAOImpl daoEvent;
	private static final Logger logger = Logger.getLogger(EventDataAggregatorService.class);
	//private long CONVERSIONTIME_MILLISEC = 3600000;
	
	
	@Override
	public double getTotal(String app, String depl, String event) {
		
		
		//Timestamp min = daoEvent.getFirstEventTime(app, depl, event);
		//Timestamp max = daoEvent.getLastEventTime(app, depl, event);
		double total = daoEvent.getEventCount(app, depl, event);
		logger.info("Total is "+total);
		return total;
	}

	@Override
	public double getTotal(String app, String depl, String vmid, String event) {
		//Timestamp min = daoEvent.getFirstEventTimeVM(app, depl, vmid,event);
		//Timestamp max = daoEvent.getLastEventTimeVM(app, depl, vmid, event);
		double total = daoEvent.getEventCountVM(app, depl, vmid, event);
		logger.info("Total is "+total);
		return total;
	}

	@Override
	public double getTotalAtTime(String app, String depl, String vmid,String event, Timestamp time) {
		// TODO Auto-generated method stub
		return 1;
	}	
	
	
	@Override
	public double getTotalAtTime(String app, String depl, String event, Timestamp time) {
		// TODO Auto-generated method stub
		return 1;
	}
	
	
	public List<DataEvent> getEvents(String app, String depl, String vmid, String event) {

		List<DataEvent> events = daoEvent.getByApplicationId(app);
		logger.info("##################### Total is "+events.size());
		return events;
	}
	


//	private void buildTable(Timestamp min, Timestamp max){
//		// TODO for future implementation
//		// hour difference and data for hour
//		long delta = max.getTime()-min.getTime();
//		double hoursbetween = delta/CONVERSIONTIME_MILLISEC;
//		
//		
//		
//		
//	}
	
	public void setDaoEvent(DataEventDAOImpl daoEvent) {
		this.daoEvent = daoEvent;
	}

	@Override
	public double getAverage(String app, String depl, String vmid, String event) {
		// TODO Auto-generated method stub
		return 0;
	}

}
