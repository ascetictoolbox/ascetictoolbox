/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.common.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.EnergySample;

public class EnergySampleMapper implements RowMapper<EnergySample>{

	@Override
	public EnergySample mapRow(ResultSet result, int rowNum) throws SQLException {
		
		EnergySample data = new EnergySample();
		data.setVmid(result.getString("vmid"));
		data.setTimestampBeging(result.getLong("time")*1000);
		data.setE_value(result.getDouble("vmenergy"));
		data.setP_value(result.getDouble("vmpower"));
		return data;
	}

}
