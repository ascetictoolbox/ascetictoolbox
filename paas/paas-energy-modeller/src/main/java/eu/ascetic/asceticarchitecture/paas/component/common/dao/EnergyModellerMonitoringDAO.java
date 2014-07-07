package eu.ascetic.asceticarchitecture.paas.component.common.dao;

import java.util.List;

import javax.sql.DataSource;

import eu.ascetic.asceticarchitecture.paas.component.common.model.EnergyModellerMonitoring;

public interface EnergyModellerMonitoringDAO {
		
		public void initialize();
		
		public void setDataSource(DataSource dataSource);	
	    
		public void save(EnergyModellerMonitoring data);
	    
	    public void terminateModel(String applicationid,String deploymentid);
	    
	    public List<EnergyModellerMonitoring> getByDeploymentId(String applicationid,String deploymentid);
	    
	    public List<EnergyModellerMonitoring> getByStatus();
		   
}
