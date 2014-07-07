package eu.ascetic.asceticarchitecture.paas.component.common.dao;

import java.util.List;

import javax.sql.DataSource;

import eu.ascetic.asceticarchitecture.paas.component.common.model.DataEvent;

public interface DataeEventDAO {

	public void initialize();
	
	public void setDataSource(DataSource dataSource);
	
    public void save(DataEvent data);
    
    public List<DataEvent> getByApplicationId(String applicationid);
    
    public List<DataEvent> getByDeploymentId(String deploymentyid);
    
    public List<DataEvent> getByVMId(String vmid);
    	
}
