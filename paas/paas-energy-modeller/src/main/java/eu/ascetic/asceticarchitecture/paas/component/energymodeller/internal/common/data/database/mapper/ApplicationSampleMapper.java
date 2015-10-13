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
