/**
   Copyright 2014-2015 Hewlett-Packard Development Company, L.P.  
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.table.EnergyModellerMonitoring;

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
