package eu.ascetic.asceticarchitecture.paas.component.energymodeller.interfaces;


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


	
}
