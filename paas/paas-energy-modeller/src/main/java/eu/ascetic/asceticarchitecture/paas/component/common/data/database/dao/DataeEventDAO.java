/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.common.data.database.dao;

import java.sql.Timestamp;
import java.util.List;

import javax.sql.DataSource;

import eu.ascetic.asceticarchitecture.paas.component.common.data.database.table.DataEvent;

public interface DataeEventDAO {

	public void initialize();
	
	public void setDataSource(DataSource dataSource);
	
    public void save(DataEvent data);

	List<DataEvent> getByApplicationIdTime(String applicationid, String vmid,String eventid, Timestamp start, Timestamp end);

	List<DataEvent> getByApplicationId(String applicationid, String vmid, String eventid);
    
//    public List<DataEvent> getByApplicationId(String applicationid,String vmid,String eventid);
//    
//    public Timestamp getLastByApplicationId(String applicationid);
//    
//    public List<DataEvent> getByDeploymentId(String deploymentyid,String vmid, String eventid);
//    
//    public List<DataEvent> getByVMId(String vmid,String eventid);
//    
//    public Timestamp getLastEventForVM(String applicationid, String vmid, String eventid);
//	
//	public Timestamp getFirstEventTimeVM(String applicationid, String deploymentid,String vmid, String eventid);
//
////	public Timestamp getLastEventTimeVM(String applicationid, String deploymentid,String vmid, String eventid);
//	
//	public int getEventCountVM(String applicationid,String deploymentid, String vmid, String eventid);
//
//	List<DataEvent> getByApplicationIdTime(String applicationid,String vmid,String eventid,Timestamp start, Timestamp end);
	
	
    
}
