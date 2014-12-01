/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.interfaces;

import java.sql.Timestamp;
import java.util.List;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.ApplicationSample;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.EventSample;


/**
 * @author davide sommacampagna
 *
 */

public interface PaaSEnergyModeller {

	// used

	/**
	 * @param providerid
	 * @param applicationid
	 * @param vmids
	 * @return the value for total energy consumption of the provided application,requries the list of vmids to compute all informations
	 */
	public double energyApplicationConsumption( String providerid, String applicationid,List<String> vmids, String eventid);	
	/**
	 * @param providerid
	 * @param applicationid
	 * @param deploymentid
	 * @param eventid
	 * @return the value for average energy estimation of the provided application,running on the vm list provided. eventid can be specified 
	 */
	public double energyEstimation( String providerid, String applicationid,List<String> vmids, String eventid);	

	/**
	 * @param providerid
	 * @param applicationid
	 * @param vmids
	 * @return the value for total energy consumption of the provided application,requries the list of vmids to compute all informations
	 */
	public double applicationConsumptionInInterval( String providerid, String applicationid,String vmids, String eventid,String unit, Timestamp start, Timestamp end);	
	
	/**
	 * @param providerid
	 * @param applicationid
	 * @param vmids
	 * @return samples for each event happening in an application within a given time period
	 */
	public List<EventSample> eventsData( String providerid, String applicationid,List<String> vmids, String eventid, Timestamp start, Timestamp end);	

	/**
	 * @param providerid
	 * @param applicationid
	 * @param vmids
	 * @return samples from the application  data in a given period of time with a sampling frequency
	 */
	public List<ApplicationSample> applicationData( String providerid, String applicationid,List<String> vmids, long samplingperiod,Timestamp start, Timestamp end);	

	
	/**
	 * @param providerid
	 * @param applicationid
	 * @param deploymentid
	 * @param eventid
	 * @return the value for average energy estimation of the provided application,running on the vm list provided. eventid can be specified 
	 */
	public double estimation( String providerid, String applicationid,List<String> vmids, String eventid, String unit);	
	
	/**
	 * @param providerid
	 * @param applicationid
	 * @param deploymentid
	 * @return true if the modelling has been already started or false if not. register the modeling of the application inside the em database
	 */
//	public boolean startModellingApplicationEnergy(String providerid, String applicationid,String deploymentid);
	
	/**
	 * @param providerid
	 * @param applicationid
	 * @param deploymentid
	 * @return true if the modelling has stopped or false if it was not running. register that modeling stopped inside the database
	 */
//	public boolean stopModellingApplicationEnergy(String providerid, String applicationid,String deploymentid);

	/**
	 * @param providerid
	 * @param applicationid
	 * @param deploymentid
	 * @return will train application model in future
	 */	
//	boolean trainApplication(String providerid, String applicationid,String deploymentid, String eventid);
	

}
