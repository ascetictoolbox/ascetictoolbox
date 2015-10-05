package eu.ascetic.paas.applicationmanager.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.sql.Timestamp;

import es.bsc.vmmclient.models.Vm;
import es.bsc.vmmclient.models.VmDeployed;
import eu.ascetic.amqp.client.AmqpMessageReceiver;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.EventSample;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.Unit;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.interfaces.PaaSEnergyModeller;
import eu.ascetic.paas.applicationmanager.amqp.AbstractTest;
import eu.ascetic.paas.applicationmanager.amqp.AmqpListListener;
import eu.ascetic.paas.applicationmanager.conf.Configuration;
import eu.ascetic.paas.applicationmanager.dao.ApplicationDAO;
import eu.ascetic.paas.applicationmanager.dao.DeploymentDAO;
import eu.ascetic.paas.applicationmanager.dao.ImageDAO;
import eu.ascetic.paas.applicationmanager.dao.VMDAO;
import eu.ascetic.paas.applicationmanager.em.amqp.EnergyModellerMessage;
import eu.ascetic.paas.applicationmanager.em.amqp.EnergyModellerQueueController;
import eu.ascetic.paas.applicationmanager.event.deployment.matchers.ImageToUploadWithEquals;
import eu.ascetic.paas.applicationmanager.event.deployment.matchers.VmWithEquals;
import eu.ascetic.paas.applicationmanager.model.Application;
import eu.ascetic.paas.applicationmanager.model.Collection;
import eu.ascetic.paas.applicationmanager.model.Cost;
import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.paas.applicationmanager.model.Dictionary;
import eu.ascetic.paas.applicationmanager.model.EnergyMeasurement;
import eu.ascetic.paas.applicationmanager.model.Image;
import eu.ascetic.paas.applicationmanager.model.VM;
import eu.ascetic.paas.applicationmanager.model.converter.ModelConverter;
import eu.ascetic.paas.applicationmanager.pm.PriceModellerClient;
import eu.ascetic.paas.applicationmanager.vmmanager.client.VmManagerClient;

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
 * Collection of Unit test that verify the correct work of the REST service for VMs entities
 *
 */

public class VMRestTest extends AbstractTest {
	private String threeTierWebAppOvfFile = "3tier-webapp.ovf.vmc.xml";
	private String threeTierWebAppOvfString;
	
	/**
	 * We just read an ovf example... 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Before
	public void setup() throws IOException, URISyntaxException {
		File file = new File(this.getClass().getResource( "/" + threeTierWebAppOvfFile ).toURI());		
		threeTierWebAppOvfString = readFile(file.getAbsolutePath(), StandardCharsets.UTF_8);
	}
	
	@Test
	public void getVMsTest() throws JAXBException {
		VM vm1 = new VM();
		vm1.setId(33);
		vm1.setIp("0.0.0.0");
		vm1.setOvfId("vm-ovf-id");
		vm1.setProviderId("provider-id");
		vm1.setSlaAgreement("sla-agreement-id");
		vm1.setStatus("ACTIVE");
		
		VM vm2 = new VM();
		vm2.setId(44);
		vm2.setIp("1.1.1.1");
		vm2.setOvfId("vm-ovf-id2");
		vm2.setProviderId("provider-id2");
		vm2.setSlaAgreement("sla-agreement-id2");
		vm2.setStatus("DELETED");
		
		Deployment deployment = new Deployment();
		deployment.addVM(vm1);
		deployment.addVM(vm2);
		
		DeploymentDAO deploymentDAO = mock(DeploymentDAO.class);
		when(deploymentDAO.getById(223)).thenReturn(deployment);
		
		VMRest vmRest = new VMRest();
		vmRest.deploymentDAO = deploymentDAO;
		
		Response response = vmRest.getVMs("app-id", "223");
		
		assertEquals(200, response.getStatus());
		
		String xml = (String) response.getEntity();
		
		JAXBContext jaxbContext = JAXBContext.newInstance(Collection.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		Collection collection = (Collection) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		assertEquals("/applications/app-id/deployments/223/vms", collection.getHref());
		assertEquals(0, collection.getItems().getOffset());
		assertEquals(2, collection.getItems().getTotal());
		// Links
		assertEquals(2, collection.getLinks().size());
		assertEquals("/applications/app-id/deployments/223", collection.getLinks().get(0).getHref());
		assertEquals("parent", collection.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, collection.getLinks().get(0).getType());
		assertEquals("/applications/app-id/deployments/223/vms", collection.getLinks().get(1).getHref());
		assertEquals("self", collection.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, collection.getLinks().get(1).getType());
		// # VMs
		assertEquals(2, collection.getItems().getVms().size());
		// VM #1
		VM vm = collection.getItems().getVms().get(0);
		assertEquals("/applications/app-id/deployments/223/vms/33", vm.getHref());
		assertEquals(33, vm.getId());
		assertEquals("0.0.0.0", vm.getIp());
		assertEquals("ACTIVE", vm.getStatus());
		assertEquals("vm-ovf-id", vm.getOvfId());
		assertEquals("provider-id", vm.getProviderId());
		assertEquals(2, vm.getLinks().size());
		assertEquals("/applications/app-id/deployments/223/vms", vm.getLinks().get(0).getHref());
		assertEquals("parent", vm.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, vm.getLinks().get(0).getType());
		assertEquals("/applications/app-id/deployments/223/vms/33", vm.getLinks().get(1).getHref());
		assertEquals("self", vm.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, vm.getLinks().get(1).getType());
		// VM #2
		vm = collection.getItems().getVms().get(1);
		assertEquals("/applications/app-id/deployments/223/vms/44", vm.getHref());
		assertEquals(44, vm.getId());
		assertEquals("1.1.1.1", vm.getIp());
		assertEquals("DELETED", vm.getStatus());
		assertEquals("vm-ovf-id2", vm.getOvfId());
		assertEquals("provider-id2", vm.getProviderId());
		assertEquals(2, vm.getLinks().size());
		assertEquals("/applications/app-id/deployments/223/vms", vm.getLinks().get(0).getHref());
		assertEquals("parent", vm.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, vm.getLinks().get(0).getType());
		assertEquals("/applications/app-id/deployments/223/vms/44", vm.getLinks().get(1).getHref());
		assertEquals("self", vm.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, vm.getLinks().get(1).getType());
	}
	
	@Test
	public void getVMTest() throws JAXBException {
		// Preparations of things for the test
		VM vm = new VM();
		vm.setId(33);
		vm.setIp("0.0.0.0");
		vm.setOvfId("vm-ovf-id");
		vm.setProviderId("provider-id");
		vm.setSlaAgreement("sla-agreement-id");
		vm.setStatus("ACTIVE");
		
		VMDAO vmDAO = mock(VMDAO.class);
		when(vmDAO.getById(33)).thenReturn(vm);
		
		VMRest vmRest = new VMRest();
		vmRest.vmDAO = vmDAO;
		
		Response response = vmRest.getVM("app-name", "22", "33");
		assertEquals(200, response.getStatus());
		
		String xml = (String) response.getEntity();
		
		JAXBContext jaxbContext = JAXBContext.newInstance(VM.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		VM vmFromRest = (VM) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		assertEquals(33, vmFromRest.getId());
		assertEquals("0.0.0.0", vmFromRest.getIp());
		assertEquals("ACTIVE", vmFromRest.getStatus());
	}

	@Test
	@SuppressWarnings(value = { "static-access" }) 
	public void testGetEnergyEstimationForAVMAndEvent() throws Exception {
		VMRest vmRest = new VMRest();
		PaaSEnergyModeller energyModeller = mock(PaaSEnergyModeller.class);
		vmRest.energyModeller = energyModeller;
		VMDAO vmDAO = mock(VMDAO.class);
		vmRest.vmDAO = vmDAO;
		
		VM vm = new VM();
		vm.setId(2);
		vm.setProviderVmId("abab");
		
		when(vmDAO.getById(444)).thenReturn(vm);
		
		List<String> ids = new ArrayList<String>();
		ids.add("2");
		when(energyModeller.estimate(null,  "111", "333", ids, "eventX", Unit.ENERGY, 0l)).thenReturn(22.0);
		//when(energyModeller.energyEstimation(null, "111", ids, "eventX")).thenReturn(22.0);
		
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
	public void testGetCostEstimationForAVMAndEvent() throws Exception {
		VMRest vmRest = new VMRest();
		PaaSEnergyModeller energyModeller = mock(PaaSEnergyModeller.class);
		vmRest.energyModeller = energyModeller;
		PriceModellerClient priceModellerClient = mock(PriceModellerClient.class);
		vmRest.priceModellerClient = priceModellerClient;
		EnergyModellerQueueController emController = mock(EnergyModellerQueueController.class);
		vmRest.energyModellerQueueController = emController;
		VMDAO vmDAO = mock(VMDAO.class);
		vmRest.vmDAO = vmDAO;
		
		VM vm = new VM();
		vm.setId(2);
		vm.setProviderVmId("abab");
		vm.setCpuActual(1);
		vm.setRamActual(10);
		
		when(vmDAO.getById(2)).thenReturn(vm);
		
		List<String> ids = new ArrayList<String>();
		ids.add("2");
		when(energyModeller.estimate(null,  "app-name", "1", ids, "loquesea", Unit.ENERGY, 0l)).thenReturn(22.0);
		when(energyModeller.estimate(null,  "app-name", "1", ids, "loquesea", Unit.POWER, 0l)).thenReturn(23.0);
		
		EnergyModellerMessage secMessage = new EnergyModellerMessage();
		secMessage.setValue("10");
		EnergyModellerMessage countMessage = new EnergyModellerMessage();
		countMessage.setValue("22");
		String secKey = EnergyModellerQueueController.generateKey("app-name", "loquesea", "1", ids, EnergyModellerQueueController.SEC);
		String countKey = EnergyModellerQueueController.generateKey("app-name", "loquesea", "1", ids, EnergyModellerQueueController.COUNT);
		when(emController.getPredictionMessage(secKey)).thenReturn(secMessage);
		when(emController.getPredictionMessage(countKey)).thenReturn(countMessage);
		
		when(priceModellerClient.getEventPredictedCharges(1, 1, 10, 10000000d, 22.0d, 1, 10,22)).thenReturn(1.1d);
		
		Response response = vmRest.getCostEstimation("app-name", "1", "2", "loquesea");
		assertEquals(200, response.getStatus());
		
		String xml = (String) response.getEntity();
		
		JAXBContext jaxbContext = JAXBContext.newInstance(Cost.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		Cost cost = (Cost) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		assertEquals("/applications/app-name/deployments/1/vms/2/events/loquesea/cost-estimation", cost.getHref());
		assertEquals(1.1d, cost.getCharges().doubleValue(), 0.0001);
		assertEquals("Energy estimation in WATTHOURS", cost.getEnergyDescription());
		assertEquals("Power estimation in WATTS", cost.getPowerDescription());
		assertEquals(23.0d, cost.getPowerValue(), 0.0001);
		assertEquals(22.0d, cost.getEnergyValue().doubleValue(), 0.0001);
		assertEquals("Charges estimation in EUROS", cost.getChargesDescription());
		assertEquals(2, cost.getLinks().size());
		assertEquals("/applications/app-name/deployments/1/vms/2", cost.getLinks().get(0).getHref());
		assertEquals("parent", cost.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, cost.getLinks().get(0).getType());
		assertEquals("/applications/app-name/deployments/1/vms/2/events/loquesea/cost-estimation", cost.getLinks().get(1).getHref());
		assertEquals("self", cost.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, cost.getLinks().get(1).getType());
	}
	
	@Test
	@SuppressWarnings(value = { "static-access" }) 
	public void testGetCostEstimationForAVMAndEventNullSec() throws Exception {
		VMRest vmRest = new VMRest();
		PaaSEnergyModeller energyModeller = mock(PaaSEnergyModeller.class);
		vmRest.energyModeller = energyModeller;
		PriceModellerClient priceModellerClient = mock(PriceModellerClient.class);
		vmRest.priceModellerClient = priceModellerClient;
		EnergyModellerQueueController emController = mock(EnergyModellerQueueController.class);
		vmRest.energyModellerQueueController = emController;
		VMDAO vmDAO = mock(VMDAO.class);
		vmRest.vmDAO = vmDAO;
		
		VM vm = new VM();
		vm.setId(2);
		vm.setProviderVmId("abab");
		vm.setCpuActual(1);
		vm.setRamActual(10);
		
		when(vmDAO.getById(2)).thenReturn(vm);
		
		List<String> ids = new ArrayList<String>();
		ids.add("2");
		when(energyModeller.estimate(null,  "app-name", "1", ids, "loquesea", Unit.ENERGY, 0l)).thenReturn(22.0);
		when(energyModeller.estimate(null,  "app-name", "1", ids, "loquesea", Unit.POWER, 0l)).thenReturn(23.0);
		
		EnergyModellerMessage countMessage = new EnergyModellerMessage();
		countMessage.setValue("22");
		String secKey = EnergyModellerQueueController.generateKey("app-name", "loquesea", "1", ids, EnergyModellerQueueController.SEC);
		String countKey = EnergyModellerQueueController.generateKey("app-name", "loquesea", "1", ids, EnergyModellerQueueController.COUNT);
		when(emController.getPredictionMessage(secKey)).thenReturn(null);
		when(emController.getPredictionMessage(countKey)).thenReturn(countMessage);
		
		Response response = vmRest.getCostEstimation("app-name", "1", "2", "loquesea");
		assertEquals(200, response.getStatus());
		
		String xml = (String) response.getEntity();
		
		JAXBContext jaxbContext = JAXBContext.newInstance(Cost.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		Cost cost = (Cost) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		assertEquals("/applications/app-name/deployments/1/vms/2/events/loquesea/cost-estimation", cost.getHref());
		assertEquals(-1.0d, cost.getCharges().doubleValue(), 0.0001);
		assertEquals("Energy estimation in WATTHOURS", cost.getEnergyDescription());
		assertEquals("Power estimation in WATTS", cost.getPowerDescription());
		assertEquals(23.0d, cost.getPowerValue(), 0.0001);
		assertEquals(22.0d, cost.getEnergyValue().doubleValue(), 0.0001);
		assertEquals("Charges estimation in EUROS", cost.getChargesDescription());
		assertEquals(2, cost.getLinks().size());
		assertEquals("/applications/app-name/deployments/1/vms/2", cost.getLinks().get(0).getHref());
		assertEquals("parent", cost.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, cost.getLinks().get(0).getType());
		assertEquals("/applications/app-name/deployments/1/vms/2/events/loquesea/cost-estimation", cost.getLinks().get(1).getHref());
		assertEquals("self", cost.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, cost.getLinks().get(1).getType());
	}
	
	@Test
	@SuppressWarnings(value = { "static-access" }) 
	public void testGetCostEstimationForAVMNullCountAndEvent() throws Exception {
		VMRest vmRest = new VMRest();
		PaaSEnergyModeller energyModeller = mock(PaaSEnergyModeller.class);
		vmRest.energyModeller = energyModeller;
		PriceModellerClient priceModellerClient = mock(PriceModellerClient.class);
		vmRest.priceModellerClient = priceModellerClient;
		EnergyModellerQueueController emController = mock(EnergyModellerQueueController.class);
		vmRest.energyModellerQueueController = emController;
		VMDAO vmDAO = mock(VMDAO.class);
		vmRest.vmDAO = vmDAO;
		
		VM vm = new VM();
		vm.setId(2);
		vm.setProviderVmId("abab");
		vm.setCpuActual(1);
		vm.setRamActual(10);
		
		when(vmDAO.getById(2)).thenReturn(vm);
		
		List<String> ids = new ArrayList<String>();
		ids.add("2");
		when(energyModeller.estimate(null,  "app-name", "1", ids, "loquesea", Unit.ENERGY, 0l)).thenReturn(22.0);
		when(energyModeller.estimate(null,  "app-name", "1", ids, "loquesea", Unit.POWER, 0l)).thenReturn(23.0);
		
		EnergyModellerMessage secMessage = new EnergyModellerMessage();
		secMessage.setValue("10");
		String secKey = EnergyModellerQueueController.generateKey("app-name", "loquesea", "1", ids, EnergyModellerQueueController.SEC);
		String countKey = EnergyModellerQueueController.generateKey("app-name", "loquesea", "1", ids, EnergyModellerQueueController.COUNT);
		when(emController.getPredictionMessage(secKey)).thenReturn(secMessage);
		when(emController.getPredictionMessage(countKey)).thenReturn(null);
		
		Response response = vmRest.getCostEstimation("app-name", "1", "2", "loquesea");
		assertEquals(200, response.getStatus());
		
		String xml = (String) response.getEntity();
		
		JAXBContext jaxbContext = JAXBContext.newInstance(Cost.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		Cost cost = (Cost) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		assertEquals("/applications/app-name/deployments/1/vms/2/events/loquesea/cost-estimation", cost.getHref());
		assertEquals(-1.0d, cost.getCharges().doubleValue(), 0.0001);
		assertEquals("Energy estimation in WATTHOURS", cost.getEnergyDescription());
		assertEquals("Power estimation in WATTS", cost.getPowerDescription());
		assertEquals(23.0d, cost.getPowerValue(), 0.0001);
		assertEquals(22.0d, cost.getEnergyValue().doubleValue(), 0.0001);
		assertEquals("Charges estimation in EUROS", cost.getChargesDescription());
		assertEquals(2, cost.getLinks().size());
		assertEquals("/applications/app-name/deployments/1/vms/2", cost.getLinks().get(0).getHref());
		assertEquals("parent", cost.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, cost.getLinks().get(0).getType());
		assertEquals("/applications/app-name/deployments/1/vms/2/events/loquesea/cost-estimation", cost.getLinks().get(1).getHref());
		assertEquals("self", cost.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, cost.getLinks().get(1).getType());
	}
	
	@Test
	@SuppressWarnings(value = { "static-access" }) 
	public void testGetEnergyConsumptionForAVMAndEvent() throws Exception {
		VMRest vmRest = new VMRest();
		PaaSEnergyModeller energyModeller = mock(PaaSEnergyModeller.class);
		vmRest.energyModeller = energyModeller;
		VMDAO vmDAO = mock(VMDAO.class);
		vmRest.vmDAO = vmDAO;
		
		VM vm = new VM();
		vm.setId(3);
		vm.setProviderVmId("abab");
		
		when(vmDAO.getById(444)).thenReturn(vm);
		
		List<String> ids = new ArrayList<String>();
		ids.add("3");
		when(energyModeller.measure(null, "111", "333", ids, "eventX",  Unit.ENERGY, null, null)).thenReturn(22.0);
		
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
	@SuppressWarnings(value = { "static-access"}) 
	public void testGetEnergyConsumptionForAVMAndEventStarTimeUntilNow() throws Exception {
		VMRest vmRest = new VMRest();
		PaaSEnergyModeller energyModeller = mock(PaaSEnergyModeller.class);
		vmRest.energyModeller = energyModeller;
		VMDAO vmDAO = mock(VMDAO.class);
		vmRest.vmDAO = vmDAO;
		
		VM vm = new VM();
		vm.setId(1);
		vm.setProviderVmId("abab");
		
		when(vmDAO.getById(444)).thenReturn(vm);
		
		List<String> ids = new ArrayList<String>();
		ids.add("1");
		
		when(energyModeller.measure(any(String.class), eq("111"), eq("333"), eq(ids), eq("eventX"), eq(Unit.ENERGY), eq(new Timestamp(20l)), any(Timestamp.class))).thenReturn(33.0);
		
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
	@SuppressWarnings(value = { "static-access"}) 
	public void testGetEnergyConsumptionForAVMAndEventStarTimeUntilSecondTime() throws Exception {
		VMRest vmRest = new VMRest();
		PaaSEnergyModeller energyModeller = mock(PaaSEnergyModeller.class);
		vmRest.energyModeller = energyModeller;
		VMDAO vmDAO = mock(VMDAO.class);
		vmRest.vmDAO = vmDAO;
		
		VM vm = new VM();
		vm.setId(3);
		vm.setProviderVmId("abab");
		
		when(vmDAO.getById(444)).thenReturn(vm);
		
		List<String> ids = new ArrayList<String>();
		ids.add("3");
		when(energyModeller.measure(any(String.class), eq("111"), eq("333"), eq(ids), eq("eventX"), eq(Unit.ENERGY), eq(new Timestamp(20l)), eq(new Timestamp(33l)))).thenReturn(44.0);
		
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
	public void getEnergySampleWithoutIntervalTest() throws Exception {
		//We prepare first the return values
		EventSample eventSample1 = new EventSample();
		eventSample1.setCvalue(1.0);
		eventSample1.setEvalue(2.0);
		eventSample1.setPvalue(3.0);
		eventSample1.setTimestampBeging(1l);
		eventSample1.setTimestampEnd(2l);
		eventSample1.setVmid("vmid");
		eventSample1.setAppid("appid");
		
		EventSample eventSample2 = new EventSample();
		eventSample2.setCvalue(4.0);
		eventSample2.setEvalue(5.0);
		eventSample2.setPvalue(6.0);
		eventSample2.setTimestampBeging(3l);
		eventSample2.setTimestampEnd(4l);
		eventSample2.setVmid("vmid2");
		eventSample2.setAppid("appid2");
		
		List<EventSample> eventSamples = new ArrayList<EventSample>();
		eventSamples.add(eventSample1);
		eventSamples.add(eventSample2);
		
		// We need to mock VMDAO and EnergyModeller
		VMRest vmRest = new VMRest();
		PaaSEnergyModeller energyModeller = mock(PaaSEnergyModeller.class);
		vmRest.energyModeller = energyModeller;
		VMDAO vmDAO = mock(VMDAO.class);
		vmRest.vmDAO = vmDAO;
		
		VM vm = new VM();
		vm.setId(4);
		vm.setProviderVmId("abab");
		when(vmDAO.getById(444)).thenReturn(vm);
		
		List<String> vmIds = new ArrayList<String>();
		vmIds.add("4");
		
		when(energyModeller.eventsData(null, "", "", vmIds, "eventX", new Timestamp(2l), new Timestamp(3l))).thenReturn(eventSamples);
		
		// We perform the action
		Response response = vmRest.getEnergySample("", "", "444", "eventX", 2l, 3l, 0l);
		assertEquals(200, response.getStatus());
		
		String xml = (String) response.getEntity();
		
		JAXBContext jaxbContext = JAXBContext.newInstance(Collection.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		Collection collection = (Collection) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		assertEquals("/applications//deployments//vms/444/events/eventX/event-samples", collection.getHref());
		assertEquals(0, collection.getItems().getOffset());
		assertEquals(2, collection.getItems().getTotal());
		//Links
		assertEquals(2, collection.getLinks().size());
		assertEquals("/applications//deployments//vms/444/events/eventX", collection.getLinks().get(0).getHref());
		assertEquals("parent", collection.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, collection.getLinks().get(0).getType());
		assertEquals("/applications//deployments//vms/444/events/eventX/event-samples", collection.getLinks().get(1).getHref());
		assertEquals("self", collection.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, collection.getLinks().get(1).getType());
		// EnergySamples
		assertEquals(2, collection.getItems().getEventSamples().size());
		// EnergySamples 1
 		assertEquals(1.0, collection.getItems().getEventSamples().get(0).getCvalue(), 0.00001);
		assertEquals(2.0, collection.getItems().getEventSamples().get(0).getEvalue(), 0.00001);
		assertEquals(3.0, collection.getItems().getEventSamples().get(0).getPvalue(), 0.00001);
		assertEquals(1l, collection.getItems().getEventSamples().get(0).getTimestampBeging());
		assertEquals(2l, collection.getItems().getEventSamples().get(0).getTimestampEnd());
		assertEquals("vmid", collection.getItems().getEventSamples().get(0).getVmid());
		assertEquals("appid", collection.getItems().getEventSamples().get(0).getAppid());
		// EnergySamples 2
 		assertEquals(4.0, collection.getItems().getEventSamples().get(1).getCvalue(), 0.00001);
		assertEquals(5.0, collection.getItems().getEventSamples().get(1).getEvalue(), 0.00001);
		assertEquals(6.0, collection.getItems().getEventSamples().get(1).getPvalue(), 0.00001);
		assertEquals(3l, collection.getItems().getEventSamples().get(1).getTimestampBeging());
		assertEquals(4l, collection.getItems().getEventSamples().get(1).getTimestampEnd());
		assertEquals("vmid2", collection.getItems().getEventSamples().get(1).getVmid());
		assertEquals("appid2", collection.getItems().getEventSamples().get(1).getAppid());
		
		
		// We verify the calls to the mocks
		verify(vmDAO, times(1)).getById(444);
		verify(energyModeller, times(1)).eventsData(null, "", "", vmIds, "eventX", new Timestamp(2l), new Timestamp(3l));
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
	
	@Test
	public void postVMMalformedRequestError() {
		
		VMRest vmRest = new VMRest();
		
		// Malformed XML
		String notValidXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><vm1></vm1>";
		Response response = vmRest.postVM("", "", notValidXML);
		assertEquals(400, response.getStatus());
		assertEquals("Malformed XML request!!!", (String) response.getEntity());
		
		// Missing ovf-id field
		notValidXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" 
				+ "<vm xmlns=\"http://application_manager.ascetic.eu/doc/schemas/xml\" href=\"/applications/101/deployments/22/vms/34\">"
	 				+ "<id>34</id>"
	 				+ "<provider-vm-id>vm-id</provider-vm-id>"
	 				+ "<provider-id>222</provider-id>"
	 				+ "<sla-aggrement-id>sla</sla-aggrement-id>"
	 				+ "<ip>127.0.0.1</ip>"
	 				+ "<link rel=\"deployment\" href=\"/applications/101/deployments/22\" type=\"application/xml\" />"
	 				+ "<link rel=\"self\" href=\"/applications/101/deployments/22/vms/33\" type=\"application/xml\" />"
	 			+ "</vm>";
		response = vmRest.postVM("", "", notValidXML);
		assertEquals(400, response.getStatus());
		assertEquals("Missing ovf-id!!!", (String) response.getEntity());
	}
	
	@Test
	public void postVMDeploymentWrongState() {
		// Pre
		DeploymentDAO deploymentDAO = mock(DeploymentDAO.class);
		
		Deployment deployment = new Deployment();
		deployment.setOvf(threeTierWebAppOvfString);
		deployment.setStatus(Dictionary.APPLICATION_STATUS_CONTEXTUALIZING);
		
		when(deploymentDAO.getById(11)).thenReturn(deployment);
		
		String vmRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" 
						+ "<vm xmlns=\"http://application_manager.ascetic.eu/doc/schemas/xml\" >"
							+ "<ovf-id>haproxy1</ovf-id>"
						+ "</vm>";
		
		// Test
		VMRest vmRest = new VMRest();
		vmRest.deploymentDAO = deploymentDAO;
		
		Response response = vmRest.postVM("", "11", vmRequest);
		assertEquals(400, response.getStatus());
		assertEquals("No Active deployment!!!", (String) response.getEntity());
	}
	
	@Test
	public void deleteVMBadDeploymentID() {
		// Pre
		DeploymentDAO deploymentDAO = mock(DeploymentDAO.class);
		
		Deployment deployment = new Deployment();
		deployment.setOvf(threeTierWebAppOvfString);
		deployment.setStatus(Dictionary.APPLICATION_STATUS_CONTEXTUALIZING);
		
		when(deploymentDAO.getById(11)).thenReturn(deployment);
		
		
		// Test
		VMRest vmRest = new VMRest();
		vmRest.deploymentDAO = deploymentDAO;
		
		Response response = vmRest.deleteVM("", "aa", "33");
		assertEquals(400, response.getStatus());
		assertEquals("Invalid deploymentID number!!!", (String) response.getEntity());
	}
	
	@Test
	public void deleteVMDeploymentWrongState() {
		// Pre
		DeploymentDAO deploymentDAO = mock(DeploymentDAO.class);
		
		Deployment deployment = new Deployment();
		deployment.setOvf(threeTierWebAppOvfString);
		deployment.setStatus(Dictionary.APPLICATION_STATUS_CONTEXTUALIZING);
		
		when(deploymentDAO.getById(11)).thenReturn(deployment);
		
		
		// Test
		VMRest vmRest = new VMRest();
		vmRest.deploymentDAO = deploymentDAO;
		
		Response response = vmRest.deleteVM("", "11", "33");
		assertEquals(400, response.getStatus());
		assertEquals("No Active deployment!!!", (String) response.getEntity());
	}
	
	@Test
	public void deleteVMVmIdType() {
		// Pre
		DeploymentDAO deploymentDAO = mock(DeploymentDAO.class);
		
		Deployment deployment = new Deployment();
		deployment.setOvf(threeTierWebAppOvfString);
		deployment.setStatus(Dictionary.APPLICATION_STATUS_DEPLOYED);
		
		when(deploymentDAO.getById(11)).thenReturn(deployment);
		
		
		// Test
		VMRest vmRest = new VMRest();
		vmRest.deploymentDAO = deploymentDAO;
		
		Response response = vmRest.deleteVM("", "11", "a3");
		assertEquals(400, response.getStatus());
		assertEquals("Invalid vmId number!!!", (String) response.getEntity());
	}
	
	@Test
	public void deleteVMNoInDB() {
		// Pre
		DeploymentDAO deploymentDAO = mock(DeploymentDAO.class);
		
		Deployment deployment = new Deployment();
		deployment.setOvf(threeTierWebAppOvfString);
		deployment.setStatus(Dictionary.APPLICATION_STATUS_DEPLOYED);
		
		when(deploymentDAO.getById(11)).thenReturn(deployment);
		
		VMDAO vmDAO = mock(VMDAO.class);
		when(vmDAO.getById(33)).thenReturn(null);
		
		
		// Test
		VMRest vmRest = new VMRest();
		vmRest.deploymentDAO = deploymentDAO;
		vmRest.vmDAO = vmDAO;
		
		Response response = vmRest.deleteVM("", "11", "33");
		assertEquals(400, response.getStatus());
		assertEquals("No VM by that Id in the database!!!", (String) response.getEntity());
	}
	
	@Test
	public void deleteVMNoPossibleToDeleteMoreVMs() {
		// Pre
		DeploymentDAO deploymentDAO = mock(DeploymentDAO.class);
		
		Deployment deployment = new Deployment();
		deployment.setOvf(threeTierWebAppOvfString);
		deployment.setStatus(Dictionary.APPLICATION_STATUS_DEPLOYED);
		
		when(deploymentDAO.getById(11)).thenReturn(deployment);
		
		VM vm = new VM();
		vm.setOvfId("haproxy");
		
		VMDAO vmDAO = mock(VMDAO.class);
		when(vmDAO.getById(33)).thenReturn(vm);
		
		List<VM> vms = new ArrayList<VM>();
		vms.add(vm);
		when(vmDAO.getVMsWithOVfIdForDeploymentNotDeleted("haproxy", 11)).thenReturn(vms);
		
		
		// Test
		VMRest vmRest = new VMRest();
		vmRest.deploymentDAO = deploymentDAO;
		vmRest.vmDAO = vmDAO;
		
		Response response = vmRest.deleteVM("", "11", "33");
		assertEquals(400, response.getStatus());
		assertEquals("haproxy number of VMs already at its minimum!!!", (String) response.getEntity());
	}
	
	@Test
	public void deleteVM() {
		// Pre
		DeploymentDAO deploymentDAO = mock(DeploymentDAO.class);
		
		Deployment deployment = new Deployment();
		deployment.setOvf(threeTierWebAppOvfString);
		deployment.setStatus(Dictionary.APPLICATION_STATUS_DEPLOYED);
		
		when(deploymentDAO.getById(11)).thenReturn(deployment);
		
		VM vm = new VM();
		vm.setOvfId("haproxy");
		vm.setProviderVmId("provider-id");
		
		VMDAO vmDAO = mock(VMDAO.class);
		when(vmDAO.getById(33)).thenReturn(vm);
		
		List<VM> vms = new ArrayList<VM>();
		vms.add(vm);
		vms.add(new VM());
		when(vmDAO.getVMsWithOVfIdForDeploymentNotDeleted("haproxy", 11)).thenReturn(vms);
		
		VmManagerClient vmMaClient = mock(VmManagerClient.class);
		
		// Test
		VMRest vmRest = new VMRest();
		vmRest.deploymentDAO = deploymentDAO;
		vmRest.vmDAO = vmDAO;
		vmRest.vmManagerClient = vmMaClient;
		
		Response response = vmRest.deleteVM("", "11", "33");
		assertEquals(204, response.getStatus());
		assertEquals("", (String) response.getEntity());
		
		//verify
		verify(vmMaClient).deleteVM("provider-id");
		ArgumentCaptor<VM> vmCaptor = ArgumentCaptor.forClass(VM.class);
		verify(vmDAO, times(1)).update(vmCaptor.capture());
		assertEquals(vm.getOvfId(), vmCaptor.getValue().getOvfId());
		assertEquals(vm.getProviderVmId(), vmCaptor.getValue().getProviderVmId());
	}
	
	@Test
	public void postVMOvfIdDoesNotExits() {
		// Pre
		DeploymentDAO deploymentDAO = mock(DeploymentDAO.class);
		
		Deployment deployment = new Deployment();
		deployment.setOvf(threeTierWebAppOvfString);
		deployment.setStatus(Dictionary.APPLICATION_STATUS_DEPLOYED);
		
		when(deploymentDAO.getById(11)).thenReturn(deployment);
		
		String vmRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" 
						+ "<vm xmlns=\"http://application_manager.ascetic.eu/doc/schemas/xml\" >"
							+ "<ovf-id>haproxy1</ovf-id>"
						+ "</vm>";
		
		// Test
		VMRest vmRest = new VMRest();
		vmRest.deploymentDAO = deploymentDAO;
		
		Response response = vmRest.postVM("", "11", vmRequest);
		assertEquals(400, response.getStatus());
		assertEquals("No VM avaiblabe by that ovf-id for this deployment!!!", (String) response.getEntity());
	}
	
	@Test
	public void postVMDeploymentNotInDB() {
		// Pre
		DeploymentDAO deploymentDAO = mock(DeploymentDAO.class);
		when(deploymentDAO.getById(11)).thenReturn(null);
		
		String vmRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" 
				+ "<vm xmlns=\"http://application_manager.ascetic.eu/doc/schemas/xml\" >"
					+ "<ovf-id>pepito</ovf-id>"
				+ "</vm>";
		
		// Test 1
		VMRest vmRest = new VMRest();
		vmRest.deploymentDAO = deploymentDAO;
		
		Response response = vmRest.postVM("", "aa", vmRequest);
		assertEquals(400, response.getStatus());
		assertEquals("Invalid deploymentID number!!!", (String) response.getEntity());
		
		// Test 2
		response = vmRest.postVM("", "11", vmRequest);
		assertEquals(400, response.getStatus());
		assertEquals("No deployment by that ID in the DB!!!", (String) response.getEntity());
	}
	
	@Test
	public void postVMResultOutOfTheLimits() {
		// Pre First Test
		DeploymentDAO deploymentDAO = mock(DeploymentDAO.class);
		
		Deployment deployment = new Deployment();
		deployment.setOvf(threeTierWebAppOvfString);
		deployment.setStatus(Dictionary.APPLICATION_STATUS_DEPLOYED);
		
		when(deploymentDAO.getById(11)).thenReturn(deployment);
		
		String vmRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" 
						+ "<vm xmlns=\"http://application_manager.ascetic.eu/doc/schemas/xml\" >"
							+ "<ovf-id>haproxy</ovf-id>"
						+ "</vm>";
		
		VM vm1 = new VM();
		VM vm2 = new VM();
		VM vm3 = new VM();
		List<VM> vms = new ArrayList<VM>();
		vms.add(vm1);
		vms.add(vm2);
		vms.add(vm3);
		
		VMDAO vmDAO = mock(VMDAO.class);
		when(vmDAO.getVMsWithOVfIdForDeploymentNotDeleted("haproxy", 11)).thenReturn(vms);
		
		// First Test
		VMRest vmRest = new VMRest();
		vmRest.deploymentDAO = deploymentDAO;
		vmRest.vmDAO = vmDAO;
		
		Response response = vmRest.postVM("", "11", vmRequest);
		assertEquals(400, response.getStatus());
		assertEquals("haproxy number of VMs already at its maximum!!!", (String) response.getEntity());
	}
	
	@Test
	public void postVMTest() throws Exception {
		// We set a listener to get the sent message from the MessageQueue
		AmqpMessageReceiver receiver = new AmqpMessageReceiver(Configuration.amqpAddress, Configuration.amqpUsername, Configuration.amqpPassword,  "APPLICATION.>", true);
		AmqpListListener listener = new AmqpListListener();
		receiver.setMessageConsumer(listener);
		
		// Pre Test 1 - Image already exits in the DB
		DeploymentDAO deploymentDAO = mock(DeploymentDAO.class);
		
		Deployment deployment = new Deployment();
		deployment.setId(11);
		deployment.setOvf(threeTierWebAppOvfString);
		deployment.setStatus("DEPLOYED");
		
		when(deploymentDAO.getById(11)).thenReturn(deployment);
		
		String vmRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" 
						+ "<vm xmlns=\"http://application_manager.ascetic.eu/doc/schemas/xml\" >"
							+ "<ovf-id>haproxy</ovf-id>"
						+ "</vm>";
		
		Image image = new Image();
		image.setId(22);
		image.setOvfId("haproxy-img");
		image.setOvfHref("/DFS/ascetic/vm-images/threeTierWebApp/haproxy.img");
		image.setProviderId("haproxy-uuid");
		image.setProviderImageId("haproxy-uuid");
		
		ImageDAO imageDAO = mock(ImageDAO.class);
		when(imageDAO.getById(22)).thenReturn(image);
		
		VM vm1 = new VM();
		vm1.addImage(image);
		List<VM> vms = new ArrayList<VM>();
		vms.add(vm1);
		
		VMDAO vmDAO = mock(VMDAO.class);
		when(vmDAO.getVMsWithOVfIdForDeploymentNotDeleted("haproxy", 11)).thenReturn(vms);
		
		VmManagerClient vmMaClient = mock(VmManagerClient.class);
		
		List<Vm> vms1 = new ArrayList<Vm>();
		VmWithEquals vm1e = new VmWithEquals("HAProxy_2","haproxy-uuid",1,1024,20, 0,"/DFS/ascetic/vm-images/threeTierWebApp/haproxy.iso_2","threeTierWebApp", "haproxy", "sla-id", false);
		System.out.println("VM with equals: " + vm1e);
		vms1.add(vm1e);
		List<String> ids1 = new ArrayList<String>();
		ids1.add("haproxy-vm1");
		
		when(vmMaClient.deployVMs(eq(vms1))).thenReturn(ids1);
		
		// We mock the calls to get VMs
		when(vmMaClient.getVM("haproxy-vm1")).thenReturn(new VmDeployed("haproxyVM", "haproxy-img", 1, 2, 3, 0, "", "", "", "", "", "10.0.0.1", "ACTIVE", new Date(), ""));
		
		// Test 1
		VMRest vmRest = new VMRest();
		vmRest.deploymentDAO = deploymentDAO;
		vmRest.vmDAO = vmDAO;
		vmRest.vmManagerClient = vmMaClient;
		vmRest.imageDAO = imageDAO;
		
		Response response = vmRest.postVM("threeTierWebApp", "11", vmRequest);
		assertEquals(200, response.getStatus());
		assertTrue(!(null == response.getEntity()));
		VM vmFromRest = ModelConverter.xmlVMToObject((String) response.getEntity());
		assertEquals("haproxy", vmFromRest.getOvfId());
		assertEquals("ACTIVE", vmFromRest.getStatus());
		assertEquals("haproxy-uuid", vmFromRest.getImages().get(0).getProviderImageId());
		assertEquals(2l, vmFromRest.getNumberVMsMax());
		assertEquals(1l, vmFromRest.getNumberVMsMin());
		
		//Post test 1 verifications:
		// We verify that at the end we store in the database the right object
		ArgumentCaptor<Deployment> deploymentCaptor = ArgumentCaptor.forClass(Deployment.class);
		verify(deploymentDAO, times(1)).update(deploymentCaptor.capture());
		assertEquals(1, deploymentCaptor.getValue().getVms().size());
		assertEquals("haproxy", deploymentCaptor.getValue().getVms().get(0).getOvfId());
		assertEquals("10.0.0.1", deploymentCaptor.getValue().getVms().get(0).getIp());
		assertEquals("haproxy-vm1", deploymentCaptor.getValue().getVms().get(0).getProviderVmId());
		assertEquals("ACTIVE", deploymentCaptor.getValue().getVms().get(0).getStatus());
		assertEquals(1, deploymentCaptor.getValue().getVms().get(0).getImages().size());
		assertEquals("/DFS/ascetic/vm-images/threeTierWebApp/haproxy.img", deploymentCaptor.getValue().getVms().get(0).getImages().get(0).getOvfHref());
		assertEquals("haproxy-img", deploymentCaptor.getValue().getVms().get(0).getImages().get(0).getOvfId());
		assertEquals("haproxy-uuid", deploymentCaptor.getValue().getVms().get(0).getImages().get(0).getProviderImageId());
		
		// We verify that the right messages were sent to the AMQP
		Thread.sleep(500l);
		assertEquals(1, listener.getTextMessages().size());
		
		assertEquals("APPLICATION.threeTierWebApp.DEPLOYMENT.11.VM.0.DEPLOYED", listener.getTextMessages().get(0).getJMSDestination().toString());
		assertEquals("threeTierWebApp", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(0).getText()).getApplicationId());
		assertEquals("11", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(0).getText()).getDeploymentId());
		assertEquals("DEPLOYED", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(0).getText()).getStatus());
		assertEquals("haproxy", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(0).getText()).getVms().get(0).getOvfId());
		
		receiver.close();
	}
	
	@Test
	public void postVMTestWithNotUploadedImage() throws Exception {
		// We set a listener to get the sent message from the MessageQueue
		AmqpMessageReceiver receiver = new AmqpMessageReceiver(Configuration.amqpAddress, Configuration.amqpUsername, Configuration.amqpPassword,  "APPLICATION.>", true);
		AmqpListListener listener = new AmqpListListener();
		receiver.setMessageConsumer(listener);
		
		// Pre Test 1 - Image already exits in the DB
		DeploymentDAO deploymentDAO = mock(DeploymentDAO.class);
		
		Deployment deployment = new Deployment();
		deployment.setId(11);
		deployment.setOvf(threeTierWebAppOvfString);
		deployment.setStatus("DEPLOYED");
		
		when(deploymentDAO.getById(11)).thenReturn(deployment);
		
		String vmRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" 
						+ "<vm xmlns=\"http://application_manager.ascetic.eu/doc/schemas/xml\" >"
							+ "<ovf-id>haproxy</ovf-id>"
						+ "</vm>";
		
		List<VM> vms = new ArrayList<VM>();
		
		VMDAO vmDAO = mock(VMDAO.class);
		when(vmDAO.getVMsWithOVfIdForDeploymentNotDeleted("haproxy", 11)).thenReturn(vms);
		
		VmManagerClient vmMaClient = mock(VmManagerClient.class);
		// We mock the class to the VmManagerClient
		when(vmMaClient.uploadImage(eq(new ImageToUploadWithEquals("haproxy.img","/DFS/ascetic/vm-images/threeTierWebApp/haproxy.img")))).thenReturn("haproxy-uuid");
		
		List<Vm> vms1 = new ArrayList<Vm>();
		VmWithEquals vm1e = new VmWithEquals("HAProxy_1","haproxy-uuid",1,1024,20, 0,"/DFS/ascetic/vm-images/threeTierWebApp/haproxy.iso_1","threeTierWebApp", "haproxy", "sla-id", false);
		System.out.println("VM with equals: " + vm1e);
		vms1.add(vm1e);
		List<String> ids1 = new ArrayList<String>();
		ids1.add("haproxy-vm1");
		
		when(vmMaClient.deployVMs(eq(vms1))).thenReturn(ids1);
		// We mock the calls to get VMs
		when(vmMaClient.getVM("haproxy-vm1")).thenReturn(new VmDeployed("haproxyVM", "haproxy-img", 1, 2, 3, 0, "", "", "", "", "", "10.0.0.1", "ACTIVE", new Date(), ""));
		
		//We mock the image DAO
		ImageDAO imageDAO = mock(ImageDAO.class);
		Image image1 = new Image();
		image1.setOvfId("haproxy-img");
		image1.setOvfHref("/DFS/ascetic/vm-images/threeTierWebApp/haproxy.img");
		image1.setProviderImageId("haproxy-uuid");
		when(imageDAO.save(eq(image1))).thenReturn(true);
		when(imageDAO.getById(0)).thenReturn(image1);
		
		// Application DAO mock
		ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
		
		Application application = new Application();
		application.setName("threeTierWebApp");
		
		when(applicationDAO.getByName("threeTierWebApp")).thenReturn(application);
		when(applicationDAO.update(application)).thenReturn(true);
		
		// Test 1
		VMRest vmRest = new VMRest();
		vmRest.deploymentDAO = deploymentDAO;
		vmRest.vmDAO = vmDAO;
		vmRest.vmManagerClient = vmMaClient;
		vmRest.imageDAO = imageDAO;
		vmRest.applicationDAO = applicationDAO;
		
		Response response = vmRest.postVM("threeTierWebApp", "11", vmRequest);
		assertEquals(200, response.getStatus());
		assertTrue(!(null == response.getEntity()));
		VM vmFromRest = ModelConverter.xmlVMToObject((String) response.getEntity());
		assertEquals("haproxy", vmFromRest.getOvfId());
		assertEquals("ACTIVE", vmFromRest.getStatus());
		assertEquals("haproxy-uuid", vmFromRest.getImages().get(0).getProviderImageId());
		assertEquals(2l, vmFromRest.getNumberVMsMax());
		assertEquals(1l, vmFromRest.getNumberVMsMin());
		
		//Post test 1 verifications:
		// We verify that at the end we store in the database the right object
		ArgumentCaptor<Deployment> deploymentCaptor = ArgumentCaptor.forClass(Deployment.class);
		verify(deploymentDAO, times(1)).update(deploymentCaptor.capture());
		assertEquals(1, deploymentCaptor.getValue().getVms().size());
		assertEquals("haproxy", deploymentCaptor.getValue().getVms().get(0).getOvfId());
		assertEquals("10.0.0.1", deploymentCaptor.getValue().getVms().get(0).getIp());
		assertEquals("haproxy-vm1", deploymentCaptor.getValue().getVms().get(0).getProviderVmId());
		assertEquals("ACTIVE", deploymentCaptor.getValue().getVms().get(0).getStatus());
		assertEquals(1, deploymentCaptor.getValue().getVms().get(0).getImages().size());
		assertEquals("/DFS/ascetic/vm-images/threeTierWebApp/haproxy.img", deploymentCaptor.getValue().getVms().get(0).getImages().get(0).getOvfHref());
		assertEquals("haproxy-img", deploymentCaptor.getValue().getVms().get(0).getImages().get(0).getOvfId());
		assertEquals("haproxy-uuid", deploymentCaptor.getValue().getVms().get(0).getImages().get(0).getProviderImageId());
		
		// We verify that the right messages were sent to the AMQP
		Thread.sleep(500l);
		assertEquals(1, listener.getTextMessages().size());
		
		assertEquals("APPLICATION.threeTierWebApp.DEPLOYMENT.11.VM.0.DEPLOYED", listener.getTextMessages().get(0).getJMSDestination().toString());
		assertEquals("threeTierWebApp", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(0).getText()).getApplicationId());
		assertEquals("11", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(0).getText()).getDeploymentId());
		assertEquals("DEPLOYED", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(0).getText()).getStatus());
		assertEquals("haproxy", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(0).getText()).getVms().get(0).getOvfId());
		
		receiver.close();
	}
	
	/**
	 * It just reads a file form the disk... 
	 * @param path
	 * @param encoding
	 * @return
	 * @throws IOException
	 */
	protected String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}
}
