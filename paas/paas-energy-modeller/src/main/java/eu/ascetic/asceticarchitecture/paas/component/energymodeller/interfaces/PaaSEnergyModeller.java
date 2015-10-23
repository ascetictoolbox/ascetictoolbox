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
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.interfaces;

import java.sql.Timestamp;
import java.util.List;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.ApplicationSample;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.EventSample;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.Unit;


/**
 * @author davide sommacampagna
 	 * This interface provides:
	 * 
	 * Y2 new Interfaces to support Estimation
	 * Y2 new Interfaces to support active monitoring 
	 * Y2 new Interfaces to support training 
	 * Y1 measure interface to get current accumulated consumption
	 * Y1 interfaces to collect data samples about application and its consumption
 */

public interface PaaSEnergyModeller {

	// Y2 new Interfaces to support Estimation	
	
	/**
	 * @param providerid
	 * @param applicationid
	 * @param deploymentid
	 * @param eventid
	 * @return the value for average energy estimation of the provided application,running on the vm list provided. eventid can be specified 
	 */
	public double estimate( String providerid, String applicationid, String deploymentid, List<String> vmids, String eventid, Unit unit, long window);	
	
	
	
	// Y2 new Interfaces to support active monitoring 	
	
	/**
	 * @param providerid
	 * @param applicationid
	 * @param vmids
	 * @return subscribe the publishing of measurement for an appl/event, if timewindow is >0 then is a forecast subscription referred to forecast window
	 */
	public boolean subscribeMonitoring(String providerid, String applicationid, String deploymentid);
	
	/**
	 * @param providerid
	 * @param applicationid
	 * @param vmids
	 * @return unsubscribe the publishing of prediction for an appl/event
	 */
	public boolean unsubscribeMonitoring(String providerid, String applicationid, String deploymentid);

	
	// Y2 new Interfaces to support training 
	
	/**
	 * @param providerid
	 * @param applicationid
	 * @param deploymentid
	 * @return will train application model in future
	 */	
	boolean trainApplication(String providerid, String applicationid,String deploymentid, String eventid);

	// Y1 measure interface to get current accumulated consumption	

	/**
	 * @param providerid
	 * @param applicationid
	 * @param deploymentid
	 * @param eventid
	 * @return the value for average energy estimation of the provided application,running on the vm list provided. eventid can be specified 
	 */
	public double measure( String providerid, String applicationid,String deployment, List<String> vmids, String eventid, Unit unit,Timestamp start, Timestamp end);	
	
	// Y1 interfaces to collect data samples about application and its consumption
	
	/**
	 * @param providerid
	 * @param applicationid
	 * @param vmids
	 * @return samples for each event happening in an application within a given time period
	 */
	public List<EventSample> eventsData( String providerid, String applicationid,String deployment, List<String> vmids, String eventid, Timestamp start, Timestamp end);	

	/**
	 * @param providerid
	 * @param applicationid
	 * @param vmids
	 * @return samples from the application  data in a given period of time with a sampling frequency
	 */
	public List<ApplicationSample> applicationData( String providerid, String applicationid, String deployment, List<String> vmids, long samplingperiod,Timestamp start, Timestamp end);	


	/**
	 * @param token
	 * @param command
	 * @return it asks the PEM to perform management operation (whipe data...) to be used for internal purpose
	 */
	public void manageComponent(String token, String command);
	
//	@Deprecated
//	public double measure( String providerid, String applicationid,List<String> vmids, String eventid, Unit unit,Timestamp start, Timestamp end);	
//


}
