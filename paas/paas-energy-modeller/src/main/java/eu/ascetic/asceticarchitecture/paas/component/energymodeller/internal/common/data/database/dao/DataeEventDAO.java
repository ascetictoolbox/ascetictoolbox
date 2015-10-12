/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.dao;

import java.sql.Timestamp;
import java.util.List;

import javax.sql.DataSource;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.table.DataEvent;

public interface DataeEventDAO {

    public void save(DataEvent data);

	List<DataEvent> getByApplicationIdTime(String applicationid, String vmid,String eventid, Timestamp start, Timestamp end);

	List<DataEvent> getByApplicationId(String applicationid, String vmid, String eventid);
	
	List<DataEvent> getByDeployIdTime(String applicationid, String deploymentid, String vmid,String eventid, Timestamp start, Timestamp end);

	List<DataEvent> getByDeployId(String applicationid, String deploymentid, String vmid, String eventid);
    
}
