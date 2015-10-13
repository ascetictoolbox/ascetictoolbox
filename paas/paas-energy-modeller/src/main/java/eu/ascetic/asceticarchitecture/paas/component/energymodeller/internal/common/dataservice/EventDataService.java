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
