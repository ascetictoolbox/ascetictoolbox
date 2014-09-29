package eu.ascetic.paas.applicationmanager.rest;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.junit.Test;

import eu.ascetic.paas.applicationmanager.model.EnergyMeasurement;

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
 * @author: David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * @email david.garciaperez@atos.net 
 * 
 * Collection of Unit test that verify the correct work of the REST service for VMs entities
 *
 */

public class VMRestTest {

	@Test
	public void testGetEnergyEstimationForAVMAndEvent() throws Exception {
		VMRest vmRest = new VMRest();
		
		Response response = vmRest.getEnergyConsumption("111", "333", "444", "eventX");
		assertEquals(200, response.getStatus());
		
		String xml = (String) response.getEntity();
		
		JAXBContext jaxbContext = JAXBContext.newInstance(EnergyMeasurement.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		EnergyMeasurement energyMeasurement = (EnergyMeasurement) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		assertEquals("/applications/111/deployments/333/vms/444/events/eventX/energy-estimation", energyMeasurement.getHref());
		//(22.0, energyMeasurement.getValue(), 0.00001);
		assertEquals("Aggregated energy estimation for an event in a vm in a specific VM", energyMeasurement.getDescription());
		assertEquals(2, energyMeasurement.getLinks().size());
		assertEquals("/applications/111/deployments/333/vms/444/events/eventX", energyMeasurement.getLinks().get(0).getHref());
		assertEquals("parent", energyMeasurement.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, energyMeasurement.getLinks().get(0).getType());
		assertEquals("/applications/111/deployments/333/vms/444/events/eventX/energy-estimation", energyMeasurement.getLinks().get(1).getHref());
		assertEquals("self",energyMeasurement.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, energyMeasurement.getLinks().get(1).getType());
	}
}
