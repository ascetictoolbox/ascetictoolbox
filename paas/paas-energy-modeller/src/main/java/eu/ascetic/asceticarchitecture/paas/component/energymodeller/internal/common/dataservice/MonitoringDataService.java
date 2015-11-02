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

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.dao.impl.EnergyModellerMonitoringDAOImpl;

public class MonitoringDataService {

	private EnergyModellerMonitoringDAOImpl dataDao;
	
	public void setDataDAO(EnergyModellerMonitoringDAOImpl dataDao) {
		this.dataDao = dataDao;
	}
	
	public void startMonitoring(String applicationid,String deploymentid,String eventid){
		dataDao.createMonitoring(applicationid, deploymentid, eventid);
	}
	
	public void stopMonitoring(String applicationid,String deploymentid){
		dataDao.terminateMonitoring(applicationid, deploymentid);
	}
	

	
}
