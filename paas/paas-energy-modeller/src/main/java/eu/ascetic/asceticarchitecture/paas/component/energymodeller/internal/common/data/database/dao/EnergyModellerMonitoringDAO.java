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

import java.util.List;

import javax.sql.DataSource;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.table.EnergyModellerMonitoring;

public interface EnergyModellerMonitoringDAO {
		
		public void initialize();
		
		public void setDataSource(DataSource dataSource);	
	    
		public void createTraining(String applicationid,String deploymentid, String events);
	    
	    public void createMonitoring(String applicationid,String deploymentid, String events);
		
	    public void terminateTraining(String applicationid,String deploymentid);
	    
	    public void terminateMonitoring(String applicationid,String deploymentid);
	    
	    public List<EnergyModellerMonitoring> getByDeploymentId(String applicationid,String deploymentid);
	    	    
	    public List<EnergyModellerMonitoring> getTrainingActive();
	    
	    public List<EnergyModellerMonitoring> getMonitoringActive();
	    
	    
		   
}
