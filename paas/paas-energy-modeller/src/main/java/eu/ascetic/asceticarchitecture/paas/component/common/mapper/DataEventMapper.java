/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.common.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import eu.ascetic.asceticarchitecture.paas.component.common.database.table.DataEvent;

public class DataEventMapper implements RowMapper<DataEvent>{

	@Override
	public DataEvent mapRow(ResultSet result, int rowNum) throws SQLException {
		
		DataEvent data = new DataEvent();
		data.setApplicationid(result.getString("applicationid"));
		data.setDeploymentid(result.getString("deploymentid"));
		data.setVmid(result.getString("vmid"));
		data.setEnergy(result.getDouble("energy"));
		data.setEventid(result.getString("eventid"));
		data.setBegintime(result.getLong("starttime"));
		data.setEndtime(result.getLong("endtime"));
		data.setData(result.getString("data"));
		return data;
	}

}
