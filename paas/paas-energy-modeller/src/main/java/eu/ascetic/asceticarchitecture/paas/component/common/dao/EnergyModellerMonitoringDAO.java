package eu.ascetic.asceticarchitecture.paas.component.common.dao;

import java.util.List;

import javax.sql.DataSource;

import eu.ascetic.asceticarchitecture.paas.component.common.model.EnergyModellerMonitoring;

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
