package eu.ascetic.providerregistry.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;

import eu.ascetic.providerregistry.model.Provider;
import eu.ascetic.providerregistry.service.ProviderDAO;

/**
 * 
 * Copyright 2016 ATOS SPAIN S.A. 
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
 * @email david.garciaperez@atos.net 
 * 
 * Unit tests for Provider Host Information.
 * 
 */
public class ProviderHostInfoTest {
	private MockWebServer mServer;
	private String mBaseURL = "http://localhost:";
	
	@Before
	public void before() {
		mServer = new MockWebServer();
		mServer.start();
		mBaseURL = mBaseURL + mServer.getPort();
	}

	@Test
	public void getHostInfo404Test() throws Exception {
		ProviderDAO providerDAO = mock(ProviderDAO.class);
		when(providerDAO.getById(1)).thenReturn(null);
		
		ProviderHostInfo providerHostInfo = new ProviderHostInfo();
		providerHostInfo.providerDAO = providerDAO;
		
		Response response = providerHostInfo.getHostInfo(1);
		
		assertEquals(404, response.getStatus());
		String providerXML = (String) response.getEntity();
		assertEquals("No provider by that id found in the databae.", providerXML);
	}
	
	@Test
	public void getHostInfoTest() throws Exception {
		String payload = "{\"nodes\":[{\"hostname\":\"wally152\",\"totalCpus\":8,\"totalMemoryMb\":16022.0,\"totalDiskGb\":1805.0,\"assignedCpus\":3.0,\"assignedMemoryMb\":4096.0,\"assignedDiskGb\":41.0,\"currentPower\":0.0,\"turnedOff\":{\"value\":1}},{\"hostname\":\"wally153\",\"totalCpus\":8,\"totalMemoryMb\":16022.0,\"totalDiskGb\":1805.0,\"assignedCpus\":2.0,\"assignedMemoryMb\":3072.0,\"assignedDiskGb\":4.0,\"currentPower\":0.0,\"turnedOff\":{\"value\":1}},{\"hostname\":\"wally154\",\"totalCpus\":8,\"totalMemoryMb\":16022.0,\"totalDiskGb\":1805.0,\"assignedCpus\":8.0,\"assignedMemoryMb\":16896.0,\"assignedDiskGb\":120.0,\"currentPower\":0.0,\"turnedOff\":{\"value\":1}}]}";
		
		mServer.addPath("/nodes", payload);
		
		// We create the Provider information
		Provider provider = new Provider();
		provider.setVmmUrl(mBaseURL);
		
		ProviderDAO providerDAO = mock(ProviderDAO.class);
		when(providerDAO.getById(1)).thenReturn(provider);
		
		ProviderHostInfo providerHostInfo = new ProviderHostInfo();
		providerHostInfo.providerDAO = providerDAO;
		
		Response response = providerHostInfo.getHostInfo(1);
		
		assertEquals(200, response.getStatus());
		String nodesJson = (String) response.getEntity();
		assertEquals(payload, nodesJson);
	}
}
