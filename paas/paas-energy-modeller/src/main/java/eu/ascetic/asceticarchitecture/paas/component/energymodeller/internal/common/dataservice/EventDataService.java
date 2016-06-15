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
import java.util.Vector;

import org.apache.log4j.Logger;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.table.DataEvent;

public class EventDataService {
	private static final Logger logger = Logger.getLogger(EventDataService.class);
	private List<DataEvent> eventData;

	public EventDataService() {
		eventData = new Vector<DataEvent>();
	}
	
	public EventDataService(List<DataEvent> eventData) {
		this.eventData = eventData;
	}
	
	

	public void save(DataEvent data) {
		eventData.add(data);
	}
	

	// M. Fontanella - 11 Jan 2016 - begin
	public List<DataEvent> getByApplicationIdTime(String providerid, String applicationid,String vmid, String eventid, Timestamp start, Timestamp end) {
		// M. Fontanella - 11 Jan 2016 - end
		List<DataEvent> resultSet = new Vector<DataEvent>();
		
		for (DataEvent de : eventData){
			
			// M. Fontanella - 11 Jan 2016 - begin
			if ( (de.getProviderid().equals(providerid)) && (de.getApplicationid().equals(applicationid))&& (de.getVmid().equals(vmid))&& (de.getEventid().equals(eventid))){
				// ok events referred to the right prov/app/vm/dep
				// M. Fontanella - 11 Jan 2016 - end
				if ( (de.getBegintime()>=start.getTime()) && (de.getBegintime()<=end.getTime()) ){
					resultSet.add(de);
				}
				
			}
		}
		
		return resultSet;
	}


	// M. Fontanella - 11 Jan 2016 - begin
	public List<DataEvent> getByApplicationId(String providerid, String applicationid, String vmid, String eventid) {
		// M. Fontanella - 11 Jan 2016 - end
		List<DataEvent> resultSet = new Vector<DataEvent>();
		
		for (DataEvent de : eventData){
			
			// M. Fontanella - 11 Jan 2016 - begin
			if ( (de.getProviderid().equals(providerid)) && (de.getApplicationid().equals(applicationid))&& (de.getVmid().equals(vmid))&& (de.getEventid().equals(eventid))){
				// ok events referred to the right prov/app/vm/dep
				// M. Fontanella - 11 Jan 2016 - end
					resultSet.add(de);
				
			}
		}
		
		return resultSet;
		
	}


	// M. Fontanella - 11 Jan 2016 - begin
	public List<DataEvent> getByDeployIdTime(String providerid, String applicationid, String deploymentid, String vmid, String eventid, Timestamp start, Timestamp end) {
		// M. Fontanella - 11 Jan 2016 - end
		List<DataEvent> resultSet = new Vector<DataEvent>();
		
		for (DataEvent de : eventData){
			
			// M. Fontanella - 11 Jan 2016 - begin
			if ( (de.getProviderid().equals(providerid)) && (de.getApplicationid().equals(applicationid))&& (de.getVmid().equals(vmid)) && (de.getEventid().equals(eventid)) && (de.getDeploymentid().equals(deploymentid)) && (de.getEventid().equals(eventid))){
				// ok events referred to the right prov/app/vm/dep
				// M. Fontanella - 11 Jan 2016 - end
				if ( (de.getBegintime()>=start.getTime()) && (de.getBegintime()<=end.getTime()) ){
					resultSet.add(de);
				}
				
			}
		}
		
		return resultSet;
	}
	

	// M. Fontanella - 11 Jan 2016 - begin
	public List<DataEvent> getByDeployId(String providerid, String applicationid, String deploymentid, String vmid, String eventid) {
		// M. Fontanella - 11 Jan 2016 - end
		List<DataEvent> resultSet = new Vector<DataEvent>();
		if (vmid==null) return resultSet;
		for (DataEvent de : eventData){
			
			// logger.info("PROV="+de.getProviderid()+", APP="+de.getApplicationid()+", VM="+de.getVmid()+", EVENT="+de.getEventid()+", DEP="+de.getDeploymentid());//MAXIM
			// M. Fontanella - 11 Jan 2016 - begin
			if ( (de.getProviderid().equals(providerid)) && (de.getApplicationid().equals(applicationid))&& (de.getVmid().equals(vmid)) && (de.getEventid().equals(eventid)) && (de.getDeploymentid().equals(deploymentid))){
				
				// logger.info("***** ADD ***** PROV="+de.getProviderid()+", APP="+de.getApplicationid()+", VM="+de.getVmid()+", EVENT="+de.getEventid()+", DEP="+de.getDeploymentid());//MAXIM
					// M. Fontanella - 11 Jan 2016 - end
					resultSet.add(de);
			}
		}
		
		return resultSet;
	}


	// M. Fontanella - 11 Jan 2016 - begin
	public int getEventsInTimeFrame(String providerid, String applicationid, String vmid, String eventid, long tstart, long tend) {
		// M. Fontanella - 11 Jan 2016 - end
		// TODO Auto-generated method stub
		int count = 0;
		
		if (vmid==null) return 0;
		
		for (DataEvent de : eventData){
			
			// M. Fontanella - 11 Jan 2016 - begin
			if ( (de.getProviderid().equals(providerid)) && (de.getApplicationid().equals(applicationid))&& (de.getVmid().equals(vmid))&& (de.getEventid().equals(eventid))){
				// ok events referred to the right prov/app/vm/dep
				// M. Fontanella - 11 Jan 2016 - end
				if ((de.getBegintime()<tend)&&(de.getEndtime()>tstart)){
					count++;
				}
				
			}
		}

		
		
		return count;
	}
	

	// M. Fontanella - 11 Jan 2016 - begin
	public int getAllEventsInTimeFrame(String providerid, String applicationid, String vmid, String eventid, long tstart, long tend) {
		// M. Fontanella - 11 Jan 2016 - end
		// TODO Auto-generated method stub
		int count = 0;
		
		
		
		for (DataEvent de : eventData){
			
			// M. Fontanella - 11 Jan 2016 - begin
			if ( (de.getProviderid().equals(providerid)) && (de.getApplicationid().equals(applicationid))&& (de.getVmid().equals(vmid))){
				// ok events referred to the right prov/app/vm/dep
				// M. Fontanella - 11 Jan 2016 - end
				if ((de.getBegintime()<tend)&&(de.getEndtime()>tstart)){
					count++;
				}
				
			}
		}

		
		
		return count;
	}
	

	// M. Fontanella - 11 Jan 2016 - begin
	public List<Long> getAllDeltas(String providerid, String applicationid, String vmid, String eventid, long tstart, long tend) {
		// M. Fontanella - 11 Jan 2016 - end
		// TODO Auto-generated method stub
		Vector<Long> results= new Vector<Long>();
		
		logger.info("$$$ Analysis of coefficients");
		logger.info("$$$ Event start "+tstart);
		logger.info("$$$ Event end "+tend);
		logger.info("$$$ Event duration "+(tend-tstart));
		
		for (DataEvent de : eventData){
			
			// M. Fontanella - 11 Jan 2016 - begin
			if (  (de.getProviderid().equals(providerid)) && (de.getApplicationid().equals(applicationid))&& (de.getVmid().equals(vmid))){
				// M. Fontanella - 11 Jan 2016 - end
				logger.info("$$$ This Event on the same machine "+de.getEndtime()+"-"+de.getBegintime());
				logger.info("$$$ This Event duration "+(de.getEndtime()-de.getBegintime()));
				
				// M. Fontanella - 11 Jan 2016 - begin
				// ok events referred to the right prov/app/vm/dep
				// M. Fontanella - 11 Jan 2016 - end
				Long delta=new Long(0);
				long lowerbtime=0;
				long upperbtime=0;
				if ( (de.getBegintime()<tend) && (de.getEndtime()>tstart)){
					if ( (de.getBegintime()<tstart) ){
						logger.info("$$$ Event start before ");
						lowerbtime = tstart;
					} else {
						logger.info("$$$ Event start after the reference event ");
						lowerbtime = de.getBegintime();
					}
					if ( (de.getEndtime()>tend) ){
						logger.info("$$$ Event end after ");
						upperbtime = tend;
					} else {
						logger.info("$$$ Event end before the reference event ");
						upperbtime = de.getEndtime();
					}
					delta = upperbtime - lowerbtime;
					logger.info("$$$ Event delta "+delta);
					results.add(delta);
				}
				
			}
		}

		
		
		return results;
	}
	
	// M. Fontanella - 18 May 2016 - begin
	public List<Double> getAllProductsDurationWeight(String providerid, String applicationid, String vmid, String eventid, long tstart, long tend) {
		// TODO Auto-generated method stub
		Vector<Double> results= new Vector<Double>();
		
		logger.info("$$$ Analysis of coefficients");
		logger.info("$$$ Event start "+tstart);
		logger.info("$$$ Event end "+tend);
		logger.info("$$$ Event duration "+(tend-tstart));
		
		for (DataEvent de : eventData){
			
			if (  (de.getProviderid().equals(providerid)) && (de.getApplicationid().equals(applicationid))&& (de.getVmid().equals(vmid))){
			
				logger.info("$$$ This Event on the same machine "+de.getEndtime()+"-"+de.getBegintime());
				logger.info("$$$ This Event duration "+(de.getEndtime()-de.getBegintime()));
				logger.info("$$$ This Event weight "+de.getWeight());
				
				// ok events referred to the right prov/app/vm/dep
				Double product=new Double(1.0);
				long lowerbtime=0;
				long upperbtime=0;
				if ( (de.getBegintime()<tend) && (de.getEndtime()>tstart)){
					if ( (de.getBegintime()<tstart) ){
						logger.info("$$$ Event start before ");
						lowerbtime = tstart;
					} else {
						logger.info("$$$ Event start after the reference event ");
						lowerbtime = de.getBegintime();
					}
					if ( (de.getEndtime()>tend) ){
						logger.info("$$$ Event end after ");
						upperbtime = tend;
					} else {
						logger.info("$$$ Event end before the reference event ");
						upperbtime = de.getEndtime();
					}
					product = (double) (upperbtime - lowerbtime) * de.getWeight();
					logger.info("$$$ Event duration x weight "+product);
					results.add(product);
				}
				
			}
		}

		return results;
	}
	// M. Fontanella - 18 May 2016 - end
}
