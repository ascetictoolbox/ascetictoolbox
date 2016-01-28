package eu.ascetic.paas.applicationmanager.amonitor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.junit.Before;
import org.junit.Test;

import eu.ascetic.paas.applicationmanager.amonitor.model.Data;
import eu.ascetic.paas.applicationmanager.amonitor.model.EnergyCosumed;
import eu.ascetic.paas.applicationmanager.conf.Configuration;
import eu.ascetic.paas.applicationmanager.dao.testUtil.MockWebServer;

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
 * e-mail david.garciaperez@atos.net
 * 
 * This class is the Unit test that verifies the correct work of the Application Manager Client for ASCETiC
 */
public class ApplicationMonitorHCTest {
	private MockWebServer mServer;
	private String mBaseURL = "http://localhost:";
	
	@Before
	public void before() {
		mServer = new MockWebServer();
		mServer.start();
		mBaseURL = mBaseURL + mServer.getPort();
	}
	
	@Test
	public void pojo() {
		ApplicationMonitorClientHC aMonitorClient = new ApplicationMonitorClientHC();
		aMonitorClient.setURL("aaa");
		
		assertEquals("aaa", aMonitorClient.getURL());
	}
	
	@Test
	public void postFinalEnergyConsumptionTest() throws Exception {
		Configuration.applicationMonitorUrl = mBaseURL;
		
		System.out.println("##################### " + Configuration.applicationMonitorUrl);
		
		mServer.addPath("/apps", "");
		
		EnergyCosumed energyConsumed = new EnergyCosumed();
		energyConsumed.setAppId("appId");
		energyConsumed.setInstanceId("instaceId");
		
		Data data = new Data();
		data.setEnd("end");
		data.setStart("start");
		data.setPower("power");
		energyConsumed.setData(data);
		
		ApplicationMonitorClient applicationManagerClient = new ApplicationMonitorClientHC();

		boolean result = applicationManagerClient.postFinalEnergyConsumption(energyConsumed);
		
		assertTrue(result);
		
		String payload = mServer.getRequestBody();
		ObjectMapper mapper = new ObjectMapper();
		
		mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	    mapper.configure(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		mapper.setSerializationInclusion(Inclusion.NON_NULL);

		EnergyCosumed energyCosumedFromJson = mapper.readValue(payload, EnergyCosumed.class);
		
		assertEquals("appId", energyCosumedFromJson.getAppId());
		assertEquals("instaceId", energyCosumedFromJson.getInstanceId());
		assertEquals("end", energyCosumedFromJson.getData().getEnd());
		assertEquals("start", energyCosumedFromJson.getData().getStart());
		assertEquals("power", energyCosumedFromJson.getData().getPower());
	}
}
