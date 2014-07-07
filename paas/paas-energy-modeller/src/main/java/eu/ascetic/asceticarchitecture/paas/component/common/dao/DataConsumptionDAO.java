package eu.ascetic.asceticarchitecture.paas.component.common.dao;

import java.util.List;

import javax.sql.DataSource;

import eu.ascetic.asceticarchitecture.paas.component.common.model.DataConsumption;

public interface DataConsumptionDAO {
    
	    
		public void initialize();
		
		public void setDataSource(DataSource dataSource);
	
	    public void save(DataConsumption data);
	    
	    public List<DataConsumption> getByApplicationId(String applicationid);
	    
	    public List<DataConsumption> getByDeploymentId(String deploymentyid);
	    
	    public List<DataConsumption> getByVMId(String vmid);
	    
	    public List<DataConsumption> getByEventId(String eventid);
	
	    
}
