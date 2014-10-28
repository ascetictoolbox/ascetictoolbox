package eu.ascetic.paas.applicationmanager.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.junit.Test;

import java.sql.Timestamp;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.service.EnergyModellerSimple;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.EnergySample;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.Sample;
import eu.ascetic.paas.applicationmanager.dao.VMDAO;
import eu.ascetic.paas.applicationmanager.model.Collection;
import eu.ascetic.paas.applicationmanager.model.EnergyMeasurement;
import eu.ascetic.paas.applicationmanager.model.VM;

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
 * e-mail david.garciaperez@atos.net 
 * 
 * Collection of Unit test that verify the correct work of the REST service for VMs entities
 *
 */

public class VMRestTest {

	@Test
	@SuppressWarnings(value = { "static-access" }) 
	public void testGetEnergyEstimationForAVMAndEvent() throws Exception {
		VMRest vmRest = new VMRest();
		EnergyModellerSimple energyModeller = mock(EnergyModellerSimple.class);
		vmRest.energyModeller = energyModeller;
		VMDAO vmDAO = mock(VMDAO.class);
		vmRest.vmDAO = vmDAO;
		
		VM vm = new VM();
		vm.setProviderVmId("abab");
		
		when(vmDAO.getById(444)).thenReturn(vm);
		
		List<String> ids = new ArrayList<String>();
		ids.add("abab");
		when(energyModeller.energyEstimation(null, "111", ids, "eventX")).thenReturn(22.0);
		
		Response response = vmRest.getEnergyEstimation("111", "333", "444", "eventX");
		assertEquals(200, response.getStatus());
		
		String xml = (String) response.getEntity();
		
		JAXBContext jaxbContext = JAXBContext.newInstance(EnergyMeasurement.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		EnergyMeasurement energyMeasurement = (EnergyMeasurement) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		assertEquals("/applications/111/deployments/333/vms/444/events/eventX/energy-estimation", energyMeasurement.getHref());
		assertEquals(22.0, energyMeasurement.getValue(), 0.00001);
		assertEquals("Aggregated energy estimation in Wh for an event in a specific VM", energyMeasurement.getDescription());
		assertEquals(2, energyMeasurement.getLinks().size());
		assertEquals("/applications/111/deployments/333/vms/444/events/eventX", energyMeasurement.getLinks().get(0).getHref());
		assertEquals("parent", energyMeasurement.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, energyMeasurement.getLinks().get(0).getType());
		assertEquals("/applications/111/deployments/333/vms/444/events/eventX/energy-estimation", energyMeasurement.getLinks().get(1).getHref());
		assertEquals("self",energyMeasurement.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, energyMeasurement.getLinks().get(1).getType());
	}
	
	@Test
	@SuppressWarnings(value = { "static-access" }) 
	public void testGetEnergyConsumptionForAVMAndEvent() throws Exception {
		VMRest vmRest = new VMRest();
		EnergyModellerSimple energyModeller = mock(EnergyModellerSimple.class);
		vmRest.energyModeller = energyModeller;
		VMDAO vmDAO = mock(VMDAO.class);
		vmRest.vmDAO = vmDAO;
		
		VM vm = new VM();
		vm.setProviderVmId("abab");
		
		when(vmDAO.getById(444)).thenReturn(vm);
		
		List<String> ids = new ArrayList<String>();
		ids.add("abab");
		when(energyModeller.energyApplicationConsumption(null, "111", ids, "eventX")).thenReturn(22.0);
		
		Response response = vmRest.getEnergyConsumption("111", "333", "444", "eventX", 0, 0);
		assertEquals(200, response.getStatus());
		
		String xml = (String) response.getEntity();
		
		JAXBContext jaxbContext = JAXBContext.newInstance(EnergyMeasurement.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		EnergyMeasurement energyMeasurement = (EnergyMeasurement) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		assertEquals("/applications/111/deployments/333/vms/444/events/eventX/energy-consumption", energyMeasurement.getHref());
		assertEquals(22.0, energyMeasurement.getValue(), 0.00001);
		assertEquals("Aggregated energy consumption in Wh for an event in a specific VM", energyMeasurement.getDescription());
		assertEquals(2, energyMeasurement.getLinks().size());
		assertEquals("/applications/111/deployments/333/vms/444/events/eventX", energyMeasurement.getLinks().get(0).getHref());
		assertEquals("parent", energyMeasurement.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, energyMeasurement.getLinks().get(0).getType());
		assertEquals("/applications/111/deployments/333/vms/444/events/eventX/energy-consumption", energyMeasurement.getLinks().get(1).getHref());
		assertEquals("self",energyMeasurement.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, energyMeasurement.getLinks().get(1).getType());
	}
	
	@Test
	@SuppressWarnings(value = { "static-access" }) 
	public void testGetEnergyConsumptionForAVMAndEventStarTimeUntilNow() throws Exception {
		VMRest vmRest = new VMRest();
		EnergyModellerSimple energyModeller = mock(EnergyModellerSimple.class);
		vmRest.energyModeller = energyModeller;
		VMDAO vmDAO = mock(VMDAO.class);
		vmRest.vmDAO = vmDAO;
		
		VM vm = new VM();
		vm.setProviderVmId("abab");
		
		when(vmDAO.getById(444)).thenReturn(vm);
		
		List<String> ids = new ArrayList<String>();
		ids.add("abab");
		when(energyModeller.energyApplicationConsumptionTimeInterval(any(String.class), eq("111"), eq("abab"), eq("eventX"),  eq(new Timestamp(20l)), any(Timestamp.class))).thenReturn(33.0);
		
		Response response = vmRest.getEnergyConsumption("111", "333", "444", "eventX", 20l, 0l);
		assertEquals(200, response.getStatus());
		
		String xml = (String) response.getEntity();
		
		JAXBContext jaxbContext = JAXBContext.newInstance(EnergyMeasurement.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		EnergyMeasurement energyMeasurement = (EnergyMeasurement) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		assertEquals("/applications/111/deployments/333/vms/444/events/eventX/energy-consumption", energyMeasurement.getHref());
		assertEquals(33.0, energyMeasurement.getValue(), 0.00001);
		assertEquals("Aggregated energy consumption in Wh for an event in a specific VM", energyMeasurement.getDescription());
		assertEquals(2, energyMeasurement.getLinks().size());
		assertEquals("/applications/111/deployments/333/vms/444/events/eventX", energyMeasurement.getLinks().get(0).getHref());
		assertEquals("parent", energyMeasurement.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, energyMeasurement.getLinks().get(0).getType());
		assertEquals("/applications/111/deployments/333/vms/444/events/eventX/energy-consumption", energyMeasurement.getLinks().get(1).getHref());
		assertEquals("self",energyMeasurement.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, energyMeasurement.getLinks().get(1).getType());
	}
	
	@Test
	@SuppressWarnings(value = { "static-access" }) 
	public void testGetEnergyConsumptionForAVMAndEventStarTimeUntilSecondTime() throws Exception {
		VMRest vmRest = new VMRest();
		EnergyModellerSimple energyModeller = mock(EnergyModellerSimple.class);
		vmRest.energyModeller = energyModeller;
		VMDAO vmDAO = mock(VMDAO.class);
		vmRest.vmDAO = vmDAO;
		
		VM vm = new VM();
		vm.setProviderVmId("abab");
		
		when(vmDAO.getById(444)).thenReturn(vm);
		
		List<String> ids = new ArrayList<String>();
		ids.add("abab");
		when(energyModeller.energyApplicationConsumptionTimeInterval(any(String.class), eq("111"), eq("abab"), eq("eventX"), eq(new Timestamp(20l)), eq(new Timestamp(33l)))).thenReturn(44.0);
		
		Response response = vmRest.getEnergyConsumption("111", "333", "444", "eventX", 20l, 33l);
		assertEquals(200, response.getStatus());
		
		String xml = (String) response.getEntity();
		
		JAXBContext jaxbContext = JAXBContext.newInstance(EnergyMeasurement.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		EnergyMeasurement energyMeasurement = (EnergyMeasurement) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		assertEquals("/applications/111/deployments/333/vms/444/events/eventX/energy-consumption", energyMeasurement.getHref());
		assertEquals(44.0, energyMeasurement.getValue(), 0.00001);
		assertEquals("Aggregated energy consumption in Wh for an event in a specific VM", energyMeasurement.getDescription());
		assertEquals(2, energyMeasurement.getLinks().size());
		assertEquals("/applications/111/deployments/333/vms/444/events/eventX", energyMeasurement.getLinks().get(0).getHref());
		assertEquals("parent", energyMeasurement.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, energyMeasurement.getLinks().get(0).getType());
		assertEquals("/applications/111/deployments/333/vms/444/events/eventX/energy-consumption", energyMeasurement.getLinks().get(1).getHref());
		assertEquals("self",energyMeasurement.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, energyMeasurement.getLinks().get(1).getType());
	}
	
	@Test
	@SuppressWarnings(value = { "static-access" })
	public void getEnergySampleWithIntervalTest() throws Exception {
		Sample energySample3 = new Sample();
		energySample3.setCvalue(7.0);
		energySample3.setEvalue(8.0);
		energySample3.setPvalue(9.0);
		energySample3.setTimestampBeging(5l);
		energySample3.setTimestampEnd(6l);
		energySample3.setVmid("vm3");
		Sample energySample4 = new Sample();
		energySample4.setCvalue(10.0);
		energySample4.setEvalue(11.0);
		energySample4.setPvalue(12.0);
		energySample4.setTimestampBeging(7l);
		energySample4.setTimestampEnd(8l);
		energySample4.setVmid("vm4");
		List<Sample> energySamples2 = new ArrayList<Sample>();
		energySamples2.add(energySample3);
		energySamples2.add(energySample4);
		
		// We need to mock VMDAO and EnergyModeller
		VMRest vmRest = new VMRest();
		EnergyModellerSimple energyModeller = mock(EnergyModellerSimple.class);
		vmRest.energyModeller = energyModeller;
		VMDAO vmDAO = mock(VMDAO.class);
		vmRest.vmDAO = vmDAO;
		
		VM vm = new VM();
		vm.setProviderVmId("abab");
		when(vmDAO.getById(444)).thenReturn(vm);
		
		when(energyModeller.applicationData(null, "", "abab", "eventX", 1l, new Timestamp(2l), new Timestamp(3l))).thenReturn(energySamples2);
		
		// We perform the action
		Response response = vmRest.getEnergySample("", "", "444", "eventX", 2l, 3l, 1000l);
		assertEquals(200, response.getStatus());
		
		String xml = (String) response.getEntity();
		
		JAXBContext jaxbContext = JAXBContext.newInstance(Collection.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		Collection collection = (Collection) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		assertEquals("/applications//deployments//vms/444/events/eventX/energy-sample", collection.getHref());
		assertEquals(0, collection.getItems().getOffset());
		assertEquals(2, collection.getItems().getTotal());
		//Links
		assertEquals(2, collection.getLinks().size());
		assertEquals("/applications//deployments//vms/444/events/eventX", collection.getLinks().get(0).getHref());
		assertEquals("parent", collection.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, collection.getLinks().get(0).getType());
		assertEquals("/applications//deployments//vms/444/events/eventX/energy-sample", collection.getLinks().get(1).getHref());
		assertEquals("self", collection.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, collection.getLinks().get(1).getType());
		// EnergySamples
		assertEquals(2, collection.getItems().getEnergySamples().size());
		// EnergySamples 1
		assertEquals(7.0, collection.getItems().getEnergySamples().get(0).getCvalue(), 0.00001);
		assertEquals(8.0, collection.getItems().getEnergySamples().get(0).getEvalue(), 0.00001);
		assertEquals(9.0, collection.getItems().getEnergySamples().get(0).getPvalue(), 0.00001);
		assertEquals(5l, collection.getItems().getEnergySamples().get(0).getTimestampBeging());
		assertEquals(6l, collection.getItems().getEnergySamples().get(0).getTimestampEnd());
		assertEquals("vm3", collection.getItems().getEnergySamples().get(0).getVmid());
		// EnergySamples 2
		assertEquals(10.0, collection.getItems().getEnergySamples().get(1).getCvalue(), 0.00001);
		assertEquals(11.0, collection.getItems().getEnergySamples().get(1).getEvalue(), 0.00001);
		assertEquals(12.0, collection.getItems().getEnergySamples().get(1).getPvalue(), 0.00001);
		assertEquals(7l, collection.getItems().getEnergySamples().get(1).getTimestampBeging());
		assertEquals(8l, collection.getItems().getEnergySamples().get(1).getTimestampEnd());
		assertEquals("vm4", collection.getItems().getEnergySamples().get(1).getVmid());
		
		// We verify the calls to the mocks
		verify(vmDAO, times(1)).getById(444);
		verify(energyModeller, times(1)).applicationData(null, "", "abab", "eventX", 1l, new Timestamp(2l), new Timestamp(3l));
	}
	
	@Test
	@SuppressWarnings(value = { "static-access" }) 
	public void getEnergySampleWithoutIntervalTest() throws Exception {
		//We prepare first the return values
		EnergySample energySample1 = new EnergySample();
		energySample1.setE_value(2.0);
		energySample1.setP_value(3.0);
		energySample1.setTimestampBeging(1l);
		energySample1.setTimestampEnd(2l);
		energySample1.setVmid("vm1");
		EnergySample energySample2 = new EnergySample();
		energySample2.setE_value(5.0);
		energySample2.setP_value(6.0);
		energySample2.setTimestampBeging(3l);
		energySample2.setTimestampEnd(4l);
		energySample2.setVmid("vm2");
		
		List<EnergySample> energySamples1 = new ArrayList<EnergySample>();
		energySamples1.add(energySample1);
		energySamples1.add(energySample2);
		
		// We need to mock VMDAO and EnergyModeller
		VMRest vmRest = new VMRest();
		EnergyModellerSimple energyModeller = mock(EnergyModellerSimple.class);
		vmRest.energyModeller = energyModeller;
		VMDAO vmDAO = mock(VMDAO.class);
		vmRest.vmDAO = vmDAO;
		
		VM vm = new VM();
		vm.setProviderVmId("abab");
		when(vmDAO.getById(444)).thenReturn(vm);
		
		when(energyModeller.energyApplicationConsumptionData(null, "", "abab", "eventX", new Timestamp(2l), new Timestamp(3l))).thenReturn(energySamples1);
		
		// We perform the action
		Response response = vmRest.getEnergySample("", "", "444", "eventX", 2l, 3l, 0l);
		assertEquals(200, response.getStatus());
		
		String xml = (String) response.getEntity();
		
		JAXBContext jaxbContext = JAXBContext.newInstance(Collection.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		Collection collection = (Collection) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		assertEquals("/applications//deployments//vms/444/events/eventX/energy-sample", collection.getHref());
		assertEquals(0, collection.getItems().getOffset());
		assertEquals(2, collection.getItems().getTotal());
		//Links
		assertEquals(2, collection.getLinks().size());
		assertEquals("/applications//deployments//vms/444/events/eventX", collection.getLinks().get(0).getHref());
		assertEquals("parent", collection.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, collection.getLinks().get(0).getType());
		assertEquals("/applications//deployments//vms/444/events/eventX/energy-sample", collection.getLinks().get(1).getHref());
		assertEquals("self", collection.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, collection.getLinks().get(1).getType());
		// EnergySamples
		assertEquals(2, collection.getItems().getEnergySamples().size());
		// EnergySamples 1
		assertEquals(0.0, collection.getItems().getEnergySamples().get(0).getCvalue(), 0.00001);
		assertEquals(2.0, collection.getItems().getEnergySamples().get(0).getEvalue(), 0.00001);
		assertEquals(3.0, collection.getItems().getEnergySamples().get(0).getPvalue(), 0.00001);
		assertEquals(1l, collection.getItems().getEnergySamples().get(0).getTimestampBeging());
		assertEquals(2l, collection.getItems().getEnergySamples().get(0).getTimestampEnd());
		assertEquals("vm1", collection.getItems().getEnergySamples().get(0).getVmid());
		// EnergySamples 2
		assertEquals(0.0, collection.getItems().getEnergySamples().get(1).getCvalue(), 0.00001);
		assertEquals(5.0, collection.getItems().getEnergySamples().get(1).getEvalue(), 0.00001);
		assertEquals(6.0, collection.getItems().getEnergySamples().get(1).getPvalue(), 0.00001);
		assertEquals(3l, collection.getItems().getEnergySamples().get(1).getTimestampBeging());
		assertEquals(4l, collection.getItems().getEnergySamples().get(1).getTimestampEnd());
		assertEquals("vm2", collection.getItems().getEnergySamples().get(1).getVmid());
		
		
		// We verify the calls to the mocks
		verify(vmDAO, times(1)).getById(444);
		verify(energyModeller, times(1)).energyApplicationConsumptionData(null, "", "abab", "eventX", new Timestamp(2l), new Timestamp(3l));
	}
	
	@Test
	public void getEnergySampleBadRequestTest() {
		VMRest vmRest = new VMRest();
		
		Response response = vmRest.getEnergySample("", "", "", "", 0, 0, 0);
		assertEquals(400, response.getStatus());
		assertEquals("It is mandatory to specify startTime and endTime!!!", (String) response.getEntity());
		
		response = vmRest.getEnergySample("", "", "", "", 1, 0, 0);
		assertEquals(400, response.getStatus());
		assertEquals("It is mandatory to specify startTime and endTime!!!", (String) response.getEntity());
		
		response = vmRest.getEnergySample("", "", "", "", 0, 1, 0);
		assertEquals(400, response.getStatus());
		assertEquals("It is mandatory to specify startTime and endTime!!!", (String) response.getEntity());
	}
}
