package eu.ascetic.paas.applicationmanager.amonitor.model.parser;

import static org.junit.Assert.assertEquals;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.junit.Test;

import eu.ascetic.paas.applicationmanager.amonitor.model.Data;
import eu.ascetic.paas.applicationmanager.amonitor.model.EnergyCosumed;

/**
 * 
 * Copyright 2014 ATOS SPAIN S.A. 
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * email: david.garciaperez@atos.net
 * 
 * Unit test for the class ParserTestClient
 */
public class ParserTest {

	@Test
	public void getJSONEnergyConsumedTest() throws Exception {
		EnergyCosumed energyConsumed = new EnergyCosumed();
		energyConsumed.setAppId("appId");
		energyConsumed.setInstanceId("instaceId");
		
		Data data = new Data();
		data.setEnd("end");
		data.setStart("start");
		data.setPower("power");
		energyConsumed.setData(data);
		
		String energyConsumedJson = Parser.getJSONEnergyConsumed(energyConsumed);
		
		ObjectMapper mapper = new ObjectMapper();
		
		mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	    mapper.configure(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		mapper.setSerializationInclusion(Inclusion.NON_NULL);


		EnergyCosumed energyCosumedFromJson = mapper.readValue(energyConsumedJson, EnergyCosumed.class);
		
		assertEquals("appId", energyCosumedFromJson.getAppId());
		assertEquals("instaceId", energyCosumedFromJson.getInstanceId());
		assertEquals("end", energyCosumedFromJson.getData().getEnd());
		assertEquals("start", energyCosumedFromJson.getData().getStart());
		assertEquals("power", energyCosumedFromJson.getData().getPower());
	}
}
