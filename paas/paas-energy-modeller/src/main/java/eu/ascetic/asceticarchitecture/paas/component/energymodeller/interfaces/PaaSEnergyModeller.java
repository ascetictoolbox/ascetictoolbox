package eu.ascetic.asceticarchitecture.paas.component.energymodeller.interfaces;

import java.sql.Timestamp;
import java.util.List;


/**
 * @author davide sommacampagna
 *
 */

public interface PaaSEnergyModeller {
	
	/**
	 * @return true if component has been initialized correctly, false if not
	 */
	//public void initialize();
	
	/**
	 * @param providerid
	 * @param applicationid
	 * @param deploymentid
	 * @return true if the modelling has been already started or false if not. register the modeling of the application inside the em database
	 */
	public boolean startModellingApplicationEnergy(String providerid, String applicationid,String deploymentid);
	
	/**
	 * @param providerid
	 * @param applicationid
	 * @param deploymentid
	 * @return true if the modelling has stopped or false if it was not running. register that modeling stopped inside the database
	 */
	public boolean stopModellingApplicationEnergy(String providerid, String applicationid,String deploymentid);
	

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
	 * @param vmids
	 * @return the value for total energy consumption of the provided application,requries the list of vmids to compute all informations
	 */
	public double energyApplicationConsumptionTimeInterval( String providerid, String applicationid,List<String> vmids, String eventid, Timestamp start, Timestamp end);	
	
	/**
	 * @param providerid
	 * @param applicationid
	 * @param vmids
	 * @return the value for total energy consumption of the provided application,requries the list of vmids to compute all informations
	 */
	public double[] energyApplicationConsumptionData( String providerid, String applicationid,List<String> vmids, String eventid, Timestamp start, Timestamp end);	


	/**
	 * @param providerid
	 * @param applicationid
	 * @param deploymentid
	 * @param eventid
	 * @return the value for average energy estimation of the provided application,running on the vm list provided. eventid can be specified 
	 */
	public double energyEstimation( String providerid, String applicationid,List<String> vmids, String eventid);	
	
	/**
	 * TBD
	 */
	public double energyConsumptionAtWorkload( String providerid, String applicationid,List<String> vmids, String eventid, double workload);
	
	/**
	 * TBD
	 */
	public double energyConsumptionAtTime( String providerid, String applicationid,List<String> vmids, String eventid, Timestamp time);
	
	/**
	 * TBD
	 */
	public double energyEstimationForTime( String providerid, String applicationid,List<String> vmids, String eventid, Timestamp time);
	
	/**
	 * @param providerid
	 * @param applicationid
	 * @param vmid
	 * @param eventid
	 * @return the value for the hourly energy estimation of the provided application,provider and deployment. vmid can be specified instead of deploymnet
	 */
	public double energyEstimationForVM( String providerid, String applicationid,String vmid, String eventid);
	
	/**
	 * @param providerid
	 * @param applicationid
	 * @param deploymentid
	 * @param eventid
	 * @return true if the application is started and running, false if it has been already performed.
	 */
	public boolean trainApplication( String providerid, String applicationid,String deploymentid, String eventid) ;

	/**
	 * @param providerid
	 * @param applicationid
	 * @param deploymentid
	 * @return the value for aggregated energy consumption of the provided application,provider and deployment. aggregate for all vms in the deployment
	 */
	public double energyApplicationConsumption( String providerid, String applicationid,String deploymentid);	

	/**
	 * @param providerid
	 * @param applicationid
	 * @param deploymentid
	 * @param eventid
	 * @return the value for the hourly energy estimation of the provided application,provider and deployment. eventid can be specified 
	 */
	public double energyEstimation( String providerid, String applicationid,String deploymentid, String eventid);
}
