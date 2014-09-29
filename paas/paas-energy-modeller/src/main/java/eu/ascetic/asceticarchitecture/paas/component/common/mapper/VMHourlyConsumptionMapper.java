/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.common.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import eu.ascetic.asceticarchitecture.paas.component.common.model.VMConsumptionPerHour;

public class VMHourlyConsumptionMapper implements RowMapper<VMConsumptionPerHour>{

	@Override
	public VMConsumptionPerHour mapRow(ResultSet result, int rowNum) throws SQLException {
		
		VMConsumptionPerHour data = new VMConsumptionPerHour();
		data.setYear(result.getString("yeart"));
		data.setMonth(result.getString("mon"));
		data.setDay(result.getString("dayt"));
		data.setHour(result.getString("hh"));
		data.setEnergy(result.getString("energy"));
		data.setPower(result.getString("power"));
		data.setLoad(result.getString("loadvm"));
		return data;
	}
	
	
}
