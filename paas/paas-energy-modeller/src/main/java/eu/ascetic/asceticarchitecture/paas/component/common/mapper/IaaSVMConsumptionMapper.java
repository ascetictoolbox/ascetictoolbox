/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.common.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import eu.ascetic.asceticarchitecture.paas.component.common.model.IaaSVMConsumption;

public class IaaSVMConsumptionMapper implements RowMapper<IaaSVMConsumption>{

	@Override
	public IaaSVMConsumption mapRow(ResultSet result, int rowNum) throws SQLException {
		
		IaaSVMConsumption data = new IaaSVMConsumption();
		data.setVmid(result.getString("vm_id"));
		data.setHostid(result.getString("host_id"));
		data.setClock(result.getString("clock"));
		data.setEnergy(result.getString("energy"));
		data.setCpu(result.getString("cpu"));
		return data;
	}

}
