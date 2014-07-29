package eu.ascetic.asceticarchitecture.paas.component.common.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import eu.ascetic.asceticarchitecture.paas.component.common.model.DataConsumption;

public class DataConsumptionMapper implements RowMapper<DataConsumption>{

	@Override
	public DataConsumption mapRow(ResultSet result, int rowNum) throws SQLException {
		
		DataConsumption data = new DataConsumption();
		data.setApplicationid(result.getString("applicationid"));
		data.setDeploymentid(result.getString("deploymentid"));
		data.setVmid(result.getString("vmid"));
		data.setEventid(result.getString("eventid"));
		data.setCpu(result.getDouble("cpu"));
		data.setMemory(result.getDouble("memory"));
		data.setDisk(result.getDouble("disk"));
		data.setNetwork(result.getDouble("network"));
		data.setTime(result.getTimestamp("time"));
		data.setHostcpu(result.getDouble("hostcpu"));
		data.setVmtotalcpu(result.getDouble("vmtotalcpu"));
		data.setHosttotalcpu(result.getDouble("hosttotalcpu"));
		data.setVmenergy(result.getDouble("vmenergy"));
		data.setHostenergy(result.getDouble("hostenergy"));

		
		return data;
	}

}
