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
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.dao;

import java.sql.Timestamp;
import java.util.List;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.table.DataEvent;

public interface DataeEventDAO {

    public void save(DataEvent data);

	List<DataEvent> getByApplicationIdTime(String applicationid, String vmid,String eventid, Timestamp start, Timestamp end);

	List<DataEvent> getByApplicationId(String applicationid, String vmid, String eventid);
	
	List<DataEvent> getByDeployIdTime(String applicationid, String deploymentid, String vmid,String eventid, Timestamp start, Timestamp end);

	List<DataEvent> getByDeployId(String applicationid, String deploymentid, String vmid, String eventid);
	
	int getEventsInTimeFrame(String applicationid, String vmid, String eventid, long tstart, long tend);
	
	int getAllEventsInTimeFrame(String applicationid, String vmid, String eventid, long tstart, long tend);
    
}
