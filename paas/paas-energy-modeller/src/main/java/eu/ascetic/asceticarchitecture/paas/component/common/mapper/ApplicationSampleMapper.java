/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.common.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.ApplicationSample;

public class ApplicationSampleMapper implements RowMapper<ApplicationSample>{

	@Override
	public ApplicationSample mapRow(ResultSet result, int rowNum) throws SQLException {
		
		ApplicationSample data = new ApplicationSample();
		data.setVmid(result.getString("vmid"));
		data.setTime(result.getLong("time"));
		data.setE_value(result.getDouble("vmenergy"));
		data.setP_value(result.getDouble("vmpower"));
		return data;
	}

}
