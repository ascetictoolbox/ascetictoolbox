package eu.ascetic.providerregistry.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Type;
import java.util.List;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import es.bsc.vmmclient.models.VmRequirements;
import es.bsc.vmmclient.models.Slot;
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
 * Unit tests for the REST API to know the free slots information
 * 
 */
public class ProviderSlotTest {
	private String payload = "[{\"hostname\":\"wally152\",\"freeMemoryMb\":8598.0,\"freeCpus\":2.0,\"freeDiskGb\":1767.0},{\"hostname\":\"wally153\",\"freeMemoryMb\":5270.0,\"freeCpus\":2.0,\"freeDiskGb\":1793.0},{\"hostname\":\"wally154\",\"freeMemoryMb\":-9322.0,\"freeCpus\":-5.0,\"freeDiskGb\":1668.0}]";
	private MockWebServer mServer;
	private String mBaseURL = "http://localhost:";
	
	@Before
	public void before() {
		mServer = new MockWebServer();
		mServer.start();
		mBaseURL = mBaseURL + mServer.getPort();
	}

	@Test
	public void getSlot404Test() throws Exception {
		ProviderDAO providerDAO = mock(ProviderDAO.class);
		when(providerDAO.getById(1)).thenReturn(null);
		
		ProviderSlot providerSlot = new ProviderSlot();
		providerSlot.providerDAO = providerDAO;
		
		Response response = providerSlot.getSlot(1);
		
		assertEquals(404, response.getStatus());
		String providerXML = (String) response.getEntity();
		assertEquals("No provider by that id found in the databae.", providerXML);
	}
	
	@Test
	public void getSlotTest() throws Exception {
		
		mServer.addPath("/slots", payload);
		
		// We create the Provider information
		Provider provider = new Provider();
		provider.setVmmUrl(mBaseURL);
		
		ProviderDAO providerDAO = mock(ProviderDAO.class);
		when(providerDAO.getById(1)).thenReturn(provider);
		
		ProviderSlot providerSlot = new ProviderSlot();
		providerSlot.providerDAO = providerDAO;
		
		Response response = providerSlot.getSlot(1);
		
		assertEquals(200, response.getStatus());
		String nodesJson = (String) response.getEntity();
		assertEquals(payload, nodesJson);
	}
	
	@Test
	public void postSlot404Test() throws Exception {
		ProviderDAO providerDAO = mock(ProviderDAO.class);
		when(providerDAO.getById(1)).thenReturn(null);
		
		ProviderSlot providerSlot = new ProviderSlot();
		providerSlot.providerDAO = providerDAO;
		
		Response response = providerSlot.postSlots(1, "");
		
		assertEquals(404, response.getStatus());
		String providerXML = (String) response.getEntity();
		assertEquals("No provider by that id found in the databae.", providerXML);
	}
	
	@Test
	public void postSlotInvalidPayload() throws Exception {
		// We create the Provider information
		Provider provider = new Provider();
		provider.setVmmUrl(mBaseURL);
		
		ProviderDAO providerDAO = mock(ProviderDAO.class);
		when(providerDAO.getById(1)).thenReturn(provider);
		
		ProviderSlot providerSlot = new ProviderSlot();
		providerSlot.providerDAO = providerDAO;
		
		Response response = providerSlot.postSlots(1, "invalid");
		
		assertEquals(400, response.getStatus());
		String responseBody = (String) response.getEntity();
		assertEquals("Invalid payload: invalid", responseBody);
	}
	
	@Test
	public void postSlot() throws Exception {
		// We create the Provider information
		Provider provider = new Provider();
		provider.setVmmUrl(mBaseURL);
		
		ProviderDAO providerDAO = mock(ProviderDAO.class);
		when(providerDAO.getById(1)).thenReturn(provider);
		
		ProviderSlot providerSlot = new ProviderSlot();
		providerSlot.providerDAO = providerDAO;

	    String clientPayload = "{" +
	    			 		"\"cpus\": 2," +
	    			 		"\"ramMb\": 512, " +
	    			 		"\"diskGb\": 333, " +
	    			 		"\"swapMb\": 0" +
	    			 	"}";
	    
	    mServer.addPath("/slots", payload);
		
		Response response = providerSlot.postSlots(1, clientPayload);
		
		String payloadToVMM = mServer.getRequestBody();
		Gson gson = new Gson();
		VmRequirements vmRequirements = gson.fromJson(payloadToVMM, VmRequirements.class);
		assertEquals(2, vmRequirements.getCpus());
		assertEquals(2222, vmRequirements.getRamMb());
		assertEquals(33333, vmRequirements.getDiskGb());
		assertEquals(4444, vmRequirements.getSwapMb());
		
		assertEquals(200, response.getStatus());
		
		String responseBody = (String) response.getEntity();
		Type collectionType = new TypeToken<List<Slot>>(){}.getType();
		List<Slot> slots = gson.fromJson(responseBody, collectionType);
		assertEquals("wally152", slots.get(0).getHostname());
		assertEquals(2.0, slots.get(0).getFreeCpus(), 0.001);
	}
}
