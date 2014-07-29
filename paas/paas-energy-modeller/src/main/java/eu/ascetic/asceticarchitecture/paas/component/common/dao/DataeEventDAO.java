package eu.ascetic.asceticarchitecture.paas.component.common.dao;

import java.sql.Timestamp;
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
    
    public Timestamp getLastEventForVM(String applicationid, String vmid, String eventid);

	public Timestamp getFirstEventTime(String applicationid, String deploymentid,String eventid);

	public Timestamp getLastEventTime(String applicationid, String deploymentid, String eventid);
	
	public Timestamp getFirstEventTimeVM(String applicationid, String deploymentid,String vmid, String eventid);

	public Timestamp getLastEventTimeVM(String applicationid, String deploymentid,String vmid, String eventid);
    
	public int getEventCount(String applicationid,String deploymentid, String eventid);
	
	public int getEventCountVM(String applicationid,String deploymentid, String vmid, String eventid);
	
	
    
}
