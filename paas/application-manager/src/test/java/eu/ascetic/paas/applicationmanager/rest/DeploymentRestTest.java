package eu.ascetic.paas.applicationmanager.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.Unit;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.service.EnergyModellerSimple;
import eu.ascetic.paas.applicationmanager.amonitor.ApplicationMonitorClient;
import eu.ascetic.paas.applicationmanager.amonitor.model.EnergyCosumed;
import eu.ascetic.paas.applicationmanager.dao.ApplicationDAO;
import eu.ascetic.paas.applicationmanager.dao.DeploymentDAO;
import eu.ascetic.paas.applicationmanager.event.DeploymentEvent;
import eu.ascetic.paas.applicationmanager.event.deployment.DeploymentEventService;
import eu.ascetic.paas.applicationmanager.model.Application;
import eu.ascetic.paas.applicationmanager.model.Collection;
import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.paas.applicationmanager.model.Dictionary;
import eu.ascetic.paas.applicationmanager.model.EnergyMeasurement;
import eu.ascetic.paas.applicationmanager.model.Image;
import eu.ascetic.paas.applicationmanager.model.VM;
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
 * e-mail: david.garciaperez@atos.net 
 * 
 * Collection of Unit test that verify the correct work of the REST service for Deployment entities
 *
 */

public class DeploymentRestTest {
	private String threeTierWebAppOvfFile = "3tier-webapp.ovf.xml";
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
	public void getDeployments() throws Exception {
		DeploymentRest deploymentRest = new DeploymentRest();
	
		Application application = new Application();
		application.setId(1);
		application.setName("Application Name");
		
		Deployment deployment1 = new Deployment();
		deployment1.setId(1);
		deployment1.setOvf("ovf1");
		deployment1.setPrice("price1");
		deployment1.setStatus("Status1");
		
		Deployment deployment2 = new Deployment();
		deployment2.setId(2);
		deployment2.setOvf("ovf2");
		deployment2.setPrice("price2");
		deployment2.setStatus("Status2");
		
		application.addDeployment(deployment1);
		application.addDeployment(deployment2);
		
		ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
		deploymentRest.applicationDAO = applicationDAO;
		
		when(applicationDAO.getByName("1")).thenReturn(application);
		
		Response response = deploymentRest.getDeployments("1", "");
		
		assertEquals(200, response.getStatus());
		
		String xml = (String) response.getEntity();
		
		JAXBContext jaxbContext = JAXBContext.newInstance(Collection.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		Collection collection = (Collection) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		//Collection
		assertEquals("/applications/1/deployments", collection.getHref());
		assertEquals(0, collection.getItems().getOffset());
		assertEquals(2, collection.getItems().getTotal());
		assertEquals(2, collection.getItems().getDeployments().size());
		//     Deployment 1
		deployment1 = collection.getItems().getDeployments().get(0);
		assertEquals("/applications/1/deployments/1", deployment1.getLinks().get(1).getHref());
		assertEquals("self", deployment1.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment1.getLinks().get(1).getType());
		assertEquals("/applications/1/deployments", deployment1.getLinks().get(0).getHref());
		assertEquals("parent", deployment1.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment1.getLinks().get(0).getType());
		assertEquals("/applications/1/deployments/1/ovf", deployment1.getLinks().get(2).getHref());
		assertEquals("ovf", deployment1.getLinks().get(2).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment1.getLinks().get(2).getType());
		assertEquals("/applications/1/deployments/1/vms", deployment1.getLinks().get(3).getHref());
		assertEquals("vms", deployment1.getLinks().get(3).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment1.getLinks().get(3).getType());
		assertEquals("ovf1", deployment1.getOvf());
		assertEquals("price1", deployment1.getPrice());
		assertEquals("Status1", deployment1.getStatus());
		//      Deployment 2
		deployment2 = collection.getItems().getDeployments().get(1);
		assertEquals("/applications/1/deployments/2", deployment2.getLinks().get(1).getHref());
		assertEquals("self", deployment2.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment1.getLinks().get(1).getType());
		assertEquals("/applications/1/deployments", deployment2.getLinks().get(0).getHref());
		assertEquals("parent", deployment1.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment2.getLinks().get(0).getType());
		assertEquals("/applications/1/deployments/2/ovf", deployment2.getLinks().get(2).getHref());
		assertEquals("ovf", deployment2.getLinks().get(2).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment2.getLinks().get(2).getType());
		assertEquals("/applications/1/deployments/2/vms", deployment2.getLinks().get(3).getHref());
		assertEquals("vms", deployment2.getLinks().get(3).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment1.getLinks().get(3).getType());
		assertEquals("ovf2", deployment2.getOvf());
		assertEquals("price2", deployment2.getPrice());
		assertEquals("Status2", deployment2.getStatus());
	}
	
	@Test
	public void getDeploymentsWithStatusRunning() throws Exception {
		DeploymentRest deploymentRest = new DeploymentRest();
	
		Application application = new Application();
		application.setId(1);
		application.setName("ApplicationName");
		
		Deployment deployment1 = new Deployment();
		deployment1.setId(1);
		deployment1.setOvf("ovf1");
		deployment1.setPrice("price1");
		deployment1.setStatus("RUNNING");
		
		Deployment deployment2 = new Deployment();
		deployment2.setId(2);
		deployment2.setOvf("ovf2");
		deployment2.setPrice("price2");
		deployment2.setStatus("RUNNING");
		
		application.addDeployment(deployment1);
		application.addDeployment(deployment2);
		
		List<Deployment> deployments = new ArrayList<Deployment>();
		deployments.add(deployment1);
		deployments.add(deployment2);
		
		ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
		deploymentRest.applicationDAO = applicationDAO;
		DeploymentDAO deploymentDAO = mock(DeploymentDAO.class);
		deploymentRest.deploymentDAO = deploymentDAO;
		
		when(applicationDAO.getByNameWithoutDeployments("ApplicationName")).thenReturn(application);
		when(deploymentDAO.getDeploymentsForApplicationWithStatus(application, "RUNNING")).thenReturn(deployments);
		
		Response response = deploymentRest.getDeployments("ApplicationName", "RUNNING");
		
		assertEquals(200, response.getStatus());
		
		String xml = (String) response.getEntity();
		
		JAXBContext jaxbContext = JAXBContext.newInstance(Collection.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		Collection collection = (Collection) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		//Collection
		assertEquals("/applications/ApplicationName/deployments", collection.getHref());
		assertEquals(0, collection.getItems().getOffset());
		assertEquals(2, collection.getItems().getTotal());
		assertEquals(2, collection.getItems().getDeployments().size());
		//     Deployment 1
		deployment1 = collection.getItems().getDeployments().get(0);
		assertEquals("/applications/ApplicationName/deployments/1", deployment1.getLinks().get(1).getHref());
		assertEquals("self", deployment1.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment1.getLinks().get(1).getType());
		assertEquals("/applications/ApplicationName/deployments", deployment1.getLinks().get(0).getHref());
		assertEquals("parent", deployment1.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment1.getLinks().get(0).getType());
		assertEquals("/applications/ApplicationName/deployments/1/ovf", deployment1.getLinks().get(2).getHref());
		assertEquals("ovf", deployment1.getLinks().get(2).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment1.getLinks().get(2).getType());
		assertEquals("/applications/ApplicationName/deployments/1/vms", deployment1.getLinks().get(3).getHref());
		assertEquals("vms", deployment1.getLinks().get(3).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment1.getLinks().get(3).getType());
		assertEquals("ovf1", deployment1.getOvf());
		assertEquals("price1", deployment1.getPrice());
		assertEquals("RUNNING", deployment1.getStatus());
		//      Deployment 2
		deployment2 = collection.getItems().getDeployments().get(1);
		assertEquals("/applications/ApplicationName/deployments/2", deployment2.getLinks().get(1).getHref());
		assertEquals("self", deployment2.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment1.getLinks().get(1).getType());
		assertEquals("/applications/ApplicationName/deployments", deployment2.getLinks().get(0).getHref());
		assertEquals("parent", deployment1.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment2.getLinks().get(0).getType());
		assertEquals("/applications/ApplicationName/deployments/2/ovf", deployment2.getLinks().get(2).getHref());
		assertEquals("ovf", deployment2.getLinks().get(2).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment2.getLinks().get(2).getType());
		assertEquals("/applications/ApplicationName/deployments/2/vms", deployment2.getLinks().get(3).getHref());
		assertEquals("vms", deployment2.getLinks().get(3).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment1.getLinks().get(3).getType());
		assertEquals("ovf2", deployment2.getOvf());
		assertEquals("price2", deployment2.getPrice());
		assertEquals("RUNNING", deployment2.getStatus());
	}
	
	@Test
	public void getDeploymentTest() throws Exception {
		Deployment deployment = new Deployment();
		deployment.setId(1);
		deployment.setOvf("ovf1");
		deployment.setPrice("price1");
		deployment.setStatus("Status1");
		
		DeploymentDAO deploymentDAO = mock(DeploymentDAO.class);
		when(deploymentDAO.getById(1)).thenReturn(deployment);
		
		DeploymentRest deploymentRest = new DeploymentRest();
		deploymentRest.deploymentDAO = deploymentDAO;
		
		Response response = deploymentRest.getDeployment("2", "1");
		
		assertEquals(200, response.getStatus());
		
		String xml = (String) response.getEntity();
		
		JAXBContext jaxbContext = JAXBContext.newInstance(Deployment.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		deployment = (Deployment) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		assertEquals("/applications/2/deployments/1", deployment.getLinks().get(1).getHref());
		assertEquals("self", deployment.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment.getLinks().get(1).getType());
		assertEquals("/applications/2/deployments", deployment.getLinks().get(0).getHref());
		assertEquals("parent", deployment.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment.getLinks().get(0).getType());
		assertEquals("/applications/2/deployments/1/ovf", deployment.getLinks().get(2).getHref());
		assertEquals("ovf", deployment.getLinks().get(2).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment.getLinks().get(2).getType());
		assertEquals("/applications/2/deployments/1/vms", deployment.getLinks().get(3).getHref());
		assertEquals("vms", deployment.getLinks().get(3).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment.getLinks().get(3).getType());
		assertEquals("ovf1", deployment.getOvf());
		assertEquals("price1", deployment.getPrice());
		assertEquals("Status1", deployment.getStatus());
	}
	
	@Test
	public void getDeploymentOvfTest() throws Exception {
		Deployment deployment = new Deployment();
		deployment.setId(1);
		deployment.setOvf("ovf1");
		deployment.setPrice("price1");
		deployment.setStatus("Status1");
		
		DeploymentDAO deploymentDAO = mock(DeploymentDAO.class);
		when(deploymentDAO.getById(1)).thenReturn(deployment);
		
		DeploymentRest deploymentRest = new DeploymentRest();
		deploymentRest.deploymentDAO = deploymentDAO;
		
		Response response = deploymentRest.getDeploymentOvf("2", "1");
		
		assertEquals(200, response.getStatus());
		
		String xml = (String) response.getEntity();
		
		assertEquals("ovf1", xml);
	}
	
	@Test
	public void deleteDeploymentWrongIdFormat() {
		DeploymentRest deploymentRest = new DeploymentRest();
		
		Response response = deploymentRest.deleteDeployment("", "xadasd");
		
		assertEquals(400, response.getStatus());
		assertEquals("The deployment id must be a number", (String) response.getEntity());
	}
	
	@Test
	public void deleteDeploymentNotInDB() {
		DeploymentDAO deploymentDAO = mock(DeploymentDAO.class);
		when(deploymentDAO.getById(1)).thenReturn(null);
		
		DeploymentRest deploymentRest = new DeploymentRest();
		deploymentRest.deploymentDAO = deploymentDAO;
		
		Response response = deploymentRest.deleteDeployment("", "1");
		
		assertEquals(404, response.getStatus());
		assertEquals("Deployment id = 1 not found in database", (String) response.getEntity());
	}
	
	@Test
	@SuppressWarnings(value = { "static-access", "unchecked" })
	public void deleteDeployment() {
		Deployment deployment = new Deployment();
		deployment.setId(1);
		deployment.setOvf("ovf1");
		deployment.setPrice("price1");
		deployment.setStatus("Status1");
		
		VM vm1 = new VM();
		vm1.setId(1);
		vm1.setIp("10.0.0.1");
		vm1.setProviderVmId("aaaa-bbbb-1");
		vm1.setStatus("ACTIVE");
		deployment.addVM(vm1);
		
		VM vm2 = new VM();
		vm2.setId(2);
		vm2.setIp("10.0.0.2");
		vm2.setProviderVmId("aaaa-bbbb-2");
		vm2.setStatus("ACTIVE");
		deployment.addVM(vm2);
		
		Image image1 = new Image();
		image1.setDemo(false);
		image1.setOvfHref("ovf-href1");
		image1.setProviderImageId("zzzz-1");
		vm1.addImage(image1);
		
		Image image2 = new Image();
		image2.setDemo(false);
		image2.setOvfHref("ovf-href2");
		image2.setProviderImageId("zzzz-2");
		vm2.addImage(image2);
		
		Image image3 = new Image();
		image3.setDemo(true);
		image3.setOvfHref("ovf-href2");
		image3.setProviderImageId("zzzz-3");
		vm2.addImage(image3);
		
		EnergyModellerSimple modeller = mock(EnergyModellerSimple.class);
		ApplicationMonitorClient amonitorClient = mock(ApplicationMonitorClient.class);
		
		DeploymentDAO deploymentDAO = mock(DeploymentDAO.class);
		when(deploymentDAO.getById(1)).thenReturn(deployment);
		when(deploymentDAO.update(deployment)).thenReturn(true);
		
		VmManagerClient vmManagerClient = mock(VmManagerClient.class);
		
		DeploymentRest deploymentRest = new DeploymentRest();
		deploymentRest.deploymentDAO = deploymentDAO;
		deploymentRest.vmManagerClient = vmManagerClient;
		deploymentRest.energyModeller = modeller;
		deploymentRest.applicationMonitorClient = amonitorClient;
		
		when(modeller.measure(isNull(String.class), 
				              eq(""), 
				              argThat(new BaseMatcher<List<String>>() {
 
																				@Override
																				public boolean matches(Object arg0) {
																					
																					List<String> ids = (List<String>) arg0;
																					
																					boolean isTheList = true;
																					
																					if(!(ids.size() == 2)) isTheList = false; 
																					
																					if(!(ids.get(0).equals("aaaa-bbbb-1"))) isTheList = false;
																					
																					if(!(ids.get(1).equals("aaaa-bbbb-2"))) isTheList = false;

																					return isTheList;
																				}
 
																				@Override
																				public void describeTo(Description arg0) {}
        																	
																			}), 
							 isNull(String.class), 
							 eq(Unit.ENERGY), 
							 isNull(Timestamp.class), 
							 isNull(Timestamp.class))).thenReturn(22.0);
		
		Response response = deploymentRest.deleteDeployment("", "1");
		
		assertEquals(204, response.getStatus());
		assertEquals("", (String) response.getEntity());
		
		verify(deploymentDAO, times(1)).getById(1);
		verify(deploymentDAO, times(1)).update(deployment);
		verify(vmManagerClient, times(1)).deleteVM("aaaa-bbbb-2");
		verify(vmManagerClient, times(1)).deleteVM("aaaa-bbbb-1");
		verify(vmManagerClient, times(1)).deleteImage("zzzz-1");
		verify(vmManagerClient, times(1)).deleteImage("zzzz-2");
		verify(vmManagerClient, times(0)).deleteImage("zzzz-3");
		
		ArgumentCaptor<EnergyCosumed> argument = ArgumentCaptor.forClass(EnergyCosumed.class);
		verify(amonitorClient, times(1)).postFinalEnergyConsumption(argument.capture());
		
		assertEquals("22.0 Wh", argument.getValue().getData().getPower());
	}
	
	@Test
	public void postANewDeploymentInDB() throws JAXBException {
		ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
		
		Application application = new Application();
		application.setId(1);
		application.setName("threeTierWebApp");
		
		// We put in order the different calls to the DB
		when(applicationDAO.getByName("threeTierWebApp")).thenReturn(application, application);
		when(applicationDAO.update(any(Application.class))).thenReturn(true);
		
		DeploymentEventService deploymentEventService = mock(DeploymentEventService.class);
		
		DeploymentRest deploymentRest = new DeploymentRest();
		deploymentRest.applicationDAO = applicationDAO;
		deploymentRest.deploymentEventService = deploymentEventService;
		
		Response response = deploymentRest.postDeployment("1", threeTierWebAppOvfString);
		assertEquals(201, response.getStatus());
		
		String xml = (String) response.getEntity();
		
		JAXBContext jaxbContext = JAXBContext.newInstance(Application.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		Application applicationResponse = (Application) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		// We verify the application was stored correctly
		assertEquals(1, applicationResponse.getId());
		assertEquals("/applications/threeTierWebApp", applicationResponse.getHref());
		assertEquals("threeTierWebApp", applicationResponse.getName());
		assertEquals(1, applicationResponse.getDeployments().size());
		assertEquals(threeTierWebAppOvfString, applicationResponse.getDeployments().get(0).getOvf());
		assertEquals(Dictionary.APPLICATION_STATUS_SUBMITTED, applicationResponse.getDeployments().get(0).getStatus());
		
		// We verify the number of calls to the DAO
		verify(applicationDAO, times(2)).getByName("threeTierWebApp");
		verify(applicationDAO, times(1)).update(any(Application.class));
		
		//We verify that the event is fired
		ArgumentCaptor<DeploymentEvent> argument = ArgumentCaptor.forClass(DeploymentEvent.class);
		verify(deploymentEventService).fireDeploymentEvent(argument.capture());
		
		assertEquals(Dictionary.APPLICATION_STATUS_SUBMITTED, argument.getValue().getDeploymentStatus());
	} 
	
	@Test
	@SuppressWarnings(value = { "static-access", "unchecked" }) 
	public void getEnergyConsumptionTest() throws JAXBException {
		Deployment deployment = new Deployment();
		deployment.setId(1);
		
		VM vm1 = new VM();
		vm1.setProviderVmId("X1");
		deployment.addVM(vm1);
		
		VM vm2 = new VM();
		vm2.setProviderVmId("X2");
		deployment.addVM(vm2);
		
		EnergyModellerSimple energyModeller = mock(EnergyModellerSimple.class);
		DeploymentRest deploymentRest = new DeploymentRest();
		
		deploymentRest.energyModeller = energyModeller;
		
		DeploymentDAO deploymentDAO = mock(DeploymentDAO.class);
		deploymentRest.deploymentDAO = deploymentDAO;
		when(deploymentDAO.getById(1)).thenReturn(deployment);
		
		when(energyModeller.measure(isNull(String.class), 
				                    eq("111"), 
				                    argThat(new BaseMatcher<List<String>>() {
 
																				@Override
																				public boolean matches(Object arg0) {
																					
																					List<String> ids = (List<String>) arg0;
																					
																					boolean isTheList = true;
																					
																					if(!(ids.size() == 2)) isTheList = false; 
																					
																					if(!(ids.get(0).equals("X1"))) isTheList = false;
																					
																					if(!(ids.get(1).equals("X2"))) isTheList = false;

																					return isTheList;
																				}
 
																				@Override
																				public void describeTo(Description arg0) {}
        																	
																			}), 
								     isNull(String.class), 
								     eq(Unit.ENERGY), 
								     isNull(Timestamp.class), 
								     isNull(Timestamp.class))).thenReturn(22.0);

		Response response = deploymentRest.getEnergyConsumption("111", "1");
		assertEquals(200, response.getStatus());
		
		String xml = (String) response.getEntity();
		JAXBContext jaxbContext = JAXBContext.newInstance(EnergyMeasurement.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		EnergyMeasurement energyMeasurement = (EnergyMeasurement) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		assertEquals(22.0, energyMeasurement.getValue(), 0.000001);
		assertEquals("Aggregated energy consumption in Wh for this aplication deployment", energyMeasurement.getDescription());
	}
	
	@Test
	@SuppressWarnings(value = { "static-access", "unchecked" }) 
	public void getEnergyEstimationForEventTest() throws JAXBException {
		Deployment deployment = new Deployment();
		deployment.setId(1);
		
		VM vm1 = new VM();
		vm1.setProviderVmId("X1");
		deployment.addVM(vm1);
		
		VM vm2 = new VM();
		vm2.setProviderVmId("X2");
		deployment.addVM(vm2);
		
		EnergyModellerSimple energyModeller = mock(EnergyModellerSimple.class);
		DeploymentRest deploymentRest = new DeploymentRest();
		
		deploymentRest.energyModeller = energyModeller;
		
		DeploymentDAO deploymentDAO = mock(DeploymentDAO.class);
		deploymentRest.deploymentDAO = deploymentDAO;
		when(deploymentDAO.getById(1)).thenReturn(deployment);
				
		when(energyModeller.measure(isNull(String.class),  eq("111"),  argThat(new BaseMatcher<List<String>>() {
			 
			@Override
			public boolean matches(Object arg0) {
				
				List<String> ids = (List<String>) arg0;
				
				boolean isTheList = true;
				
				if(!(ids.size() == 2)) isTheList = false; 
				
				if(!(ids.get(0).equals("X1"))) isTheList = false;
				
				if(!(ids.get(1).equals("X2"))) isTheList = false;

				return isTheList;
			}

			@Override
			public void describeTo(Description arg0) {}
		
		}), eq("eventX"), eq(Unit.ENERGY), isNull(Timestamp.class), isNull(Timestamp.class))).thenReturn(22.0);
		
		
		Response response = deploymentRest.getEnergyEstimationForEvent("111", "1", "eventX");
		
		String xml = (String) response.getEntity();
		JAXBContext jaxbContext = JAXBContext.newInstance(EnergyMeasurement.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		EnergyMeasurement energyMeasurement = (EnergyMeasurement) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		assertEquals(22.0, energyMeasurement.getValue(), 0.000001);
		assertEquals("Aggregated energy estimation for this aplication deployment and specific event", energyMeasurement.getDescription());
	}
	
	@Test
	public void getVmsProviderIdsTest() {
		Deployment deployment = new Deployment();
		
		VM vm1 = new VM();
		vm1.setProviderVmId("X1");
		deployment.addVM(vm1);
		
		VM vm2 = new VM();
		vm2.setProviderVmId("X2");
		deployment.addVM(vm2);
		
		DeploymentRest rest = new DeploymentRest();
		
		List<String> ids = rest.getVmsProviderIds(deployment);
		
		assertEquals(2, ids.size());
		assertEquals("X1", ids.get(0));
		assertEquals("X2", ids.get(1));
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

