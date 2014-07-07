package eu.ascetic.asceticarchitecture.paas.component.common.dao;

import java.util.List;

import javax.sql.DataSource;

import eu.ascetic.asceticarchitecture.paas.component.common.model.EnergyModellerTraining;

public interface EnergyModellerTrainingDAO {
	
	
	public void initialize();
	
	public void setDataSource(DataSource dataSource);
	
    public void save(EnergyModellerTraining data);
    
    public void terminateTraining(String applicationid,String deploymentid);
    
    public List<EnergyModellerTraining> getByStatus();
    
    public List<EnergyModellerTraining> getByDeploymentId(String applicationid,String deploymentid);
	
}
