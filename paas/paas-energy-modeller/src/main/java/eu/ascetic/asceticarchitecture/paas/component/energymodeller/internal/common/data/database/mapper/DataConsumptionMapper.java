/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.table.DataConsumption;

public class DataConsumptionMapper implements RowMapper<DataConsumption>{

	@Override
	public DataConsumption mapRow(ResultSet result, int rowNum) throws SQLException {
		
		DataConsumption data = new DataConsumption();
		data.setApplicationid(result.getString("applicationid"));
		data.setDeploymentid(result.getString("deploymentid"));
		data.setVmid(result.getString("vmid"));
		data.setEventid(result.getString("eventid"));
		data.setTime(result.getLong("time"));
		data.setVmenergy(result.getDouble("vmenergy"));
		data.setVmpower(result.getDouble("vmpower"));
		data.setVmcpu(result.getDouble("vmcpu"));
		
		return data;
	}

}
