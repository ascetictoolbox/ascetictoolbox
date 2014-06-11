package es.bsc.vmmanagercore.db;

import java.util.ArrayList;

import es.bsc.vmmanagercore.model.SchedulingAlgorithm;

/**
 * 
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public interface VmManagerDb {
	
	public void closeConnection();
	
	public void cleanDb();
	
	public void insertVm(String vmId, String appId);
	
	public void deleteVm(String vmId);
	
	public String getAppIdOfVm(String vmId);
	
	public void deleteAllVms();
	
	public ArrayList<String> getAllVmIds();
	
	public ArrayList<String> getVmsOfApp(String appId);
	
	public SchedulingAlgorithm getCurrentSchedulingAlg();
	
	public ArrayList<SchedulingAlgorithm> getAvailableSchedulingAlg();
	
	public void setCurrentSchedulingAlg(SchedulingAlgorithm alg);
}