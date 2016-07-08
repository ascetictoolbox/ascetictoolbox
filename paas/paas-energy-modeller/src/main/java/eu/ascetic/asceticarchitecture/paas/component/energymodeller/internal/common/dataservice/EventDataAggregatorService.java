/**
   Copyright 2014-2015 Hewlett-Packard Development Company, L.P.  
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

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

	public List<DataEvent> getEvents(String provider, String app, String depl, String vmid, String event, Timestamp start,Timestamp end) {

		eventDataManager = new EventDataService(eventCollectorService.generateEventData(provider, app, depl, vmid, event));
		
		if((start==null)&&(end==null)){
			List<DataEvent> events = eventDataManager.getByDeployId(provider,app,depl,vmid,event);
			logger.info("##################### Total events "+events.size()+" from " + vmid + " event" + event);
			return events;
		} else {
			List<DataEvent> events = eventDataManager.getByDeployIdTime(provider,app,depl,vmid,event,start,end);
			logger.info("##################### Total is "+events.size());
			return events;
		}
	}

	public int getAllEventsNumber(String provider, String app, String vmid, String event, long start,long end) {
				
		return eventDataManager.getAllEventsInTimeFrame(provider, app, vmid, event, start, end);		
		
	}
	
	public List<Long> getAllDeltas(String provider, String app, String vmid, String event, long start,long end) {
				
		return eventDataManager.getAllDeltas(provider, app, vmid, event, start, end);

	}
	
	public List<Double> getAllProductsDurationWeight(String provider, String app, String vmid, String event, long start,long end) {
		
		return eventDataManager.getAllProductsDurationWeight(provider, app, vmid, event, start, end);
			
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
