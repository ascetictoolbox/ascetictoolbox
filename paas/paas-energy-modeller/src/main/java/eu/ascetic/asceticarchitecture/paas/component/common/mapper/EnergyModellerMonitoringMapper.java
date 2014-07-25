package eu.ascetic.asceticarchitecture.paas.component.common.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import eu.ascetic.asceticarchitecture.paas.component.common.model.EnergyModellerMonitoring;

public class EnergyModellerMonitoringMapper implements RowMapper<EnergyModellerMonitoring>{

	@Override
	public EnergyModellerMonitoring mapRow(ResultSet result, int rowNum) throws SQLException {
		
		EnergyModellerMonitoring data = new EnergyModellerMonitoring();
		data.setMonitoringid(result.getString("monitoringid"));
		data.setApplicationid(result.getString("applicationid"));
		data.setDeploymentid(result.getString("deploymentid"));
		data.setType(result.getString("type"));
		data.setStatus(result.getBoolean("status"));
		data.setStarted(result.getTimestamp("started"));
		data.setEnded(result.getTimestamp("ended"));
		data.setEvents(result.getString("events"));
		return data;
	}

}
