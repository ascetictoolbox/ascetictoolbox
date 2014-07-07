package eu.ascetic.asceticarchitecture.paas.component.common.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import eu.ascetic.asceticarchitecture.paas.component.common.model.EnergyModellerTraining;

public class EnergyModellerTrainingMapper implements RowMapper<EnergyModellerTraining>{

	@Override
	public EnergyModellerTraining mapRow(ResultSet result, int rowNum) throws SQLException {
		
		EnergyModellerTraining data = new EnergyModellerTraining();
		data.setTrainingid(result.getString("trainingid"));
		data.setApplicationid(result.getString("applicationid"));
		data.setDeploymentid(result.getString("deploymentid"));
		data.setEvents(result.getString("events"));
		data.setStatus(result.getBoolean("status"));
		data.setStarted(result.getTimestamp("started"));
		data.setEnded(result.getTimestamp("ended"));
		return data;
	}

}
