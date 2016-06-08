package eu.ascetic.paas.applicationmanager.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

import es.bsc.vmmclient.models.VmCost;
import eu.ascetic.amqp.client.AmqpMessageReceiver;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.Unit;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.interfaces.PaaSEnergyModeller;
import eu.ascetic.asceticarchitecture.paas.type.VMinfo;
import eu.ascetic.paas.applicationmanager.amonitor.ApplicationMonitorClient;
import eu.ascetic.paas.applicationmanager.amonitor.model.EnergyCosumed;
import eu.ascetic.paas.applicationmanager.amqp.AbstractTest;
import eu.ascetic.paas.applicationmanager.amqp.AmqpListListener;
import eu.ascetic.paas.applicationmanager.conf.Configuration;
import eu.ascetic.paas.applicationmanager.dao.ApplicationDAO;
import eu.ascetic.paas.applicationmanager.dao.DeploymentDAO;
import eu.ascetic.paas.applicationmanager.dao.VMDAO;
import eu.ascetic.paas.applicationmanager.em.amqp.EnergyModellerMessage;
import eu.ascetic.paas.applicationmanager.em.amqp.EnergyModellerQueueController;
import eu.ascetic.paas.applicationmanager.event.DeploymentEvent;
import eu.ascetic.paas.applicationmanager.event.deployment.DeploymentEventService;
import eu.ascetic.paas.applicationmanager.model.Application;
import eu.ascetic.paas.applicationmanager.model.Collection;
import eu.ascetic.paas.applicationmanager.model.Cost;
import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.paas.applicationmanager.model.Dictionary;
import eu.ascetic.paas.applicationmanager.model.EnergyMeasurement;
import eu.ascetic.paas.applicationmanager.model.Image;
import eu.ascetic.paas.applicationmanager.model.PowerMeasurement;
import eu.ascetic.paas.applicationmanager.model.VM;
import eu.ascetic.paas.applicationmanager.model.converter.ModelConverter;
import eu.ascetic.paas.applicationmanager.pm.PriceModellerClient;
import eu.ascetic.paas.applicationmanager.providerregistry.PRClient;
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

public class DeploymentRestTest extends AbstractTest {
	private String threeTierWebAppOvfFile = "3tier-webapp.ovf.xml";
	private String threeTierWebAppOvfString;
	private String ovfFile = "saas.ovf";
	private String ovfFileString;
	
	/**
	 * We just read an ovf example... 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Before
	public void setup() throws IOException, URISyntaxException {
		File file = new File(this.getClass().getResource( "/" + threeTierWebAppOvfFile ).toURI());		
		threeTierWebAppOvfString = readFile(file.getAbsolutePath(), StandardCharsets.UTF_8);
		file = new File(this.getClass().getResource( "/" + ovfFile ).toURI());		
		ovfFileString = readFile(file.getAbsolutePath(), StandardCharsets.UTF_8);
	}
	
	@Test
	public void deploymentWithDeploymentName() {
		DeploymentRest deploymentRest = new DeploymentRest();
		
		Deployment deployment = deploymentRest.createDeploymentToApplication(ovfFileString);
		assertEquals("SuperDeploymentName", deployment.getDeploymentName());
		
		deployment = deploymentRest.createDeploymentToApplication(threeTierWebAppOvfString);
		assertNull(deployment.getDeploymentName());
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
	public void getDeploymentsJSON() throws Exception {
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
		
		Response response = deploymentRest.getDeploymentsJSON("1", "");
		
		assertEquals(200, response.getStatus());
		
		String json = (String) response.getEntity();
		
		Collection collection = ModelConverter.jsonCollectionToObject(json);
		
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
	public void getDeploymentJSONTest() throws Exception {
		Deployment deployment = new Deployment();
		deployment.setId(1);
		deployment.setOvf("ovf1");
		deployment.setPrice("price1");
		deployment.setStatus("Status1");
		
		DeploymentDAO deploymentDAO = mock(DeploymentDAO.class);
		when(deploymentDAO.getById(1)).thenReturn(deployment);
		
		DeploymentRest deploymentRest = new DeploymentRest();
		deploymentRest.deploymentDAO = deploymentDAO;
		
		Response response = deploymentRest.getDeploymentJSON("2", "1");
		
		assertEquals(200, response.getStatus());
		
		String json = (String) response.getEntity();

		deployment = ModelConverter.jsonDeploymentToObject(json);
		
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
	public void deleteDeployment() throws Exception {
		// We set a listener to get the sent message from the MessageQueue
		AmqpMessageReceiver receiver = new AmqpMessageReceiver(Configuration.amqpAddress, Configuration.amqpUsername, Configuration.amqpPassword,  "APPLICATION.>", true);
		AmqpListListener listener = new AmqpListListener();
		receiver.setMessageConsumer(listener);
		
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
		vm1.setOvfId("ovf-vm1");
		deployment.addVM(vm1);
		
		VM vm2 = new VM();
		vm2.setId(2);
		vm2.setIp("10.0.0.2");
		vm2.setProviderVmId("aaaa-bbbb-2");
		vm2.setStatus("ACTIVE");
		vm2.setOvfId("ovf-vm2");
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
		
		PaaSEnergyModeller modeller = mock(PaaSEnergyModeller.class);
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
				              eq("app-name"), 
				              eq("1"),
				              argThat(new BaseMatcher<List<String>>() {
 
																				@Override
																				public boolean matches(Object arg0) {
																					
																					List<String> ids = (List<String>) arg0;
																					
																					boolean isTheList = true;
																					
																					if(!(ids.size() == 2)) isTheList = false; 
																					
																					if(!(ids.get(0).equals("1"))) isTheList = false;
																					
																					if(!(ids.get(1).equals("2"))) isTheList = false;

																					return isTheList;
																				}
 
																				@Override
																				public void describeTo(Description arg0) {}
        																	
																			}), 
							 isNull(String.class), 
							 eq(Unit.ENERGY), 
							 isNull(Timestamp.class), 
							 isNull(Timestamp.class))).thenReturn(22.0);
		
		Response response = deploymentRest.deleteDeployment("app-name", "1");
		
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
		
		// We verify that the right messages were sent to the AMQP
		Thread.sleep(500l);
		assertEquals(3, listener.getTextMessages().size());
		
		assertEquals("APPLICATION.app-name.DEPLOYMENT.1.VM.1.DELETED", listener.getTextMessages().get(0).getJMSDestination().toString());
		assertEquals("app-name", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(0).getText()).getApplicationId());
		assertEquals("1", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(0).getText()).getDeploymentId());
		assertEquals("Status1", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(0).getText()).getStatus());
		assertEquals("ovf-vm1", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(0).getText()).getVms().get(0).getOvfId());
		
		assertEquals("APPLICATION.app-name.DEPLOYMENT.1.VM.2.DELETED", listener.getTextMessages().get(1).getJMSDestination().toString());
		assertEquals("app-name", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(1).getText()).getApplicationId());
		assertEquals("1", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(1).getText()).getDeploymentId());
		assertEquals("Status1", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(1).getText()).getStatus());
		assertEquals("ovf-vm2", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(1).getText()).getVms().get(0).getOvfId());
		
		assertEquals("APPLICATION.app-name.DEPLOYMENT.1.TERMINATED", listener.getTextMessages().get(2).getJMSDestination().toString());
		assertEquals("app-name", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(2).getText()).getApplicationId());
		assertEquals("1", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(2).getText()).getDeploymentId());
		assertEquals("TERMINATED", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(2).getText()).getStatus());
	
		receiver.close();
	}
	
	@Test
	public void postANewDeploymentInDB() throws Exception {
		// We set a listener to get the sent message from the MessageQueue
		AmqpMessageReceiver receiver = new AmqpMessageReceiver(Configuration.amqpAddress, Configuration.amqpUsername, Configuration.amqpPassword,  "APPLICATION.>", true);
		AmqpListListener listener = new AmqpListListener();
		receiver.setMessageConsumer(listener);
		
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
		
		Response response = deploymentRest.postDeployment("1", "automatic", null, threeTierWebAppOvfString);
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
		assertEquals("threeTierWebApp", argument.getValue().getApplicationName());
		assertEquals(true, argument.getValue().isAutomaticNegotiation());
		
		// We verify that the right messages were sent to the AMQP
		Thread.sleep(1000l);
		assertEquals(1, listener.getTextMessages().size());
		
		assertEquals("APPLICATION.threeTierWebApp.DEPLOYMENT.0.SUBMITTED", listener.getTextMessages().get(0).getJMSDestination().toString());
		assertEquals("threeTierWebApp", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(0).getText()).getApplicationId());
		assertEquals("0", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(0).getText()).getDeploymentId());
		assertEquals("SUBMITTED", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(0).getText()).getStatus());
		
		receiver.close();
	} 
	
	@Test
	public void postANewDeploymentInJSON() throws Exception {
		// We set a listener to get the sent message from the MessageQueue
		AmqpMessageReceiver receiver = new AmqpMessageReceiver(Configuration.amqpAddress, Configuration.amqpUsername, Configuration.amqpPassword,  "APPLICATION.>", true);
		AmqpListListener listener = new AmqpListListener();
		receiver.setMessageConsumer(listener);
		
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
		
		Response response = deploymentRest.postDeploymentJSON("1", "automatic", null, threeTierWebAppOvfString);
		assertEquals(201, response.getStatus());
		
		String json = (String) response.getEntity();
		
		Application applicationResponse = ModelConverter.jsonApplicationToObject(json);
		
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
		assertEquals("threeTierWebApp", argument.getValue().getApplicationName());
		assertEquals(true, argument.getValue().isAutomaticNegotiation());
		
		// We verify that the right messages were sent to the AMQP
		Thread.sleep(1000l);
		assertEquals(1, listener.getTextMessages().size());
		
		assertEquals("APPLICATION.threeTierWebApp.DEPLOYMENT.0.SUBMITTED", listener.getTextMessages().get(0).getJMSDestination().toString());
		assertEquals("threeTierWebApp", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(0).getText()).getApplicationId());
		assertEquals("0", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(0).getText()).getDeploymentId());
		assertEquals("SUBMITTED", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(0).getText()).getStatus());
		
		receiver.close();
	} 
	
	@Test
	public void postANewDeploymentInDBManualNegotiationTest() throws Exception {
		// We set a listener to get the sent message from the MessageQueue
		AmqpMessageReceiver receiver = new AmqpMessageReceiver(Configuration.amqpAddress, Configuration.amqpUsername, Configuration.amqpPassword,  "APPLICATION.>", true);
		AmqpListListener listener = new AmqpListListener();
		receiver.setMessageConsumer(listener);
		
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
		
		Response response = deploymentRest.postDeployment("1", "manual", "3", threeTierWebAppOvfString);
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
		assertEquals(3, applicationResponse.getDeployments().get(0).getSchema());
		assertEquals(Dictionary.APPLICATION_STATUS_SUBMITTED, applicationResponse.getDeployments().get(0).getStatus());
		
		// We verify the number of calls to the DAO
		verify(applicationDAO, times(2)).getByName("threeTierWebApp");
		verify(applicationDAO, times(1)).update(any(Application.class));
		
		//We verify that the event is fired
		ArgumentCaptor<DeploymentEvent> argument = ArgumentCaptor.forClass(DeploymentEvent.class);
		verify(deploymentEventService).fireDeploymentEvent(argument.capture());
		
		assertEquals(Dictionary.APPLICATION_STATUS_SUBMITTED, argument.getValue().getDeploymentStatus());
		assertEquals("threeTierWebApp", argument.getValue().getApplicationName());
		assertEquals(false, argument.getValue().isAutomaticNegotiation());
		
		// We verify that the right messages were sent to the AMQP
		Thread.sleep(1000l);
		assertEquals(1, listener.getTextMessages().size());
		
		assertEquals("APPLICATION.threeTierWebApp.DEPLOYMENT.0.SUBMITTED", listener.getTextMessages().get(0).getJMSDestination().toString());
		assertEquals("threeTierWebApp", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(0).getText()).getApplicationId());
		assertEquals("0", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(0).getText()).getDeploymentId());
		assertEquals("SUBMITTED", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(0).getText()).getStatus());
		
		receiver.close();
	} 
	
	@Test
	@SuppressWarnings(value = { "static-access", "unchecked" }) 
	public void getEnergyConsumptionTest() throws JAXBException {
		Deployment deployment = new Deployment();
		deployment.setId(1);
		
		VM vm1 = new VM();
		vm1.setId(1);
		vm1.setProviderVmId("X1");
		deployment.addVM(vm1);
		
		VM vm2 = new VM();
		vm2.setId(2);
		vm2.setProviderVmId("X2");
		deployment.addVM(vm2);
		
		PaaSEnergyModeller energyModeller = mock(PaaSEnergyModeller.class);
		DeploymentRest deploymentRest = new DeploymentRest();
		
		deploymentRest.energyModeller = energyModeller;
		
		DeploymentDAO deploymentDAO = mock(DeploymentDAO.class);
		deploymentRest.deploymentDAO = deploymentDAO;
		when(deploymentDAO.getById(1)).thenReturn(deployment);
		
		when(energyModeller.measure(isNull(String.class), 
				                    eq("111"), 
				                    eq("1"),
				                    argThat(new BaseMatcher<List<String>>() {
 
																				@Override
																				public boolean matches(Object arg0) {
																					
																					List<String> ids = (List<String>) arg0;
																					
																					boolean isTheList = true;
																					
																					if(!(ids.size() == 2)) isTheList = false; 
																					
																					if(!(ids.get(0).equals("1"))) isTheList = false;
																					
																					if(!(ids.get(1).equals("2"))) isTheList = false;

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
	public void getPowerConsumptionTest() throws JAXBException {
		Deployment deployment = new Deployment();
		deployment.setId(1);
		
		VM vm1 = new VM();
		vm1.setId(1);
		vm1.setProviderVmId("X1");
		deployment.addVM(vm1);
		
		VM vm2 = new VM();
		vm2.setId(2);
		vm2.setProviderVmId("X2");
		deployment.addVM(vm2);
		
		PaaSEnergyModeller energyModeller = mock(PaaSEnergyModeller.class);
		DeploymentRest deploymentRest = new DeploymentRest();
		
		deploymentRest.energyModeller = energyModeller;
		
		DeploymentDAO deploymentDAO = mock(DeploymentDAO.class);
		deploymentRest.deploymentDAO = deploymentDAO;
		when(deploymentDAO.getById(1)).thenReturn(deployment);
		
		when(energyModeller.measure(isNull(String.class), 
				                    eq("111"), 
				                    eq("1"),
				                    argThat(new BaseMatcher<List<String>>() {
 
																				@Override
																				public boolean matches(Object arg0) {
																					
																					List<String> ids = (List<String>) arg0;
																					
																					boolean isTheList = true;
																					
																					if(!(ids.size() == 2)) isTheList = false; 
																					
																					if(!(ids.get(0).equals("1"))) isTheList = false;
																					
																					if(!(ids.get(1).equals("2"))) isTheList = false;

																					return isTheList;
																				}
 
																				@Override
																				public void describeTo(Description arg0) {}
        																	
																			}), 
								     isNull(String.class), 
								     eq(Unit.POWER), 
								     isNull(Timestamp.class), 
								     isNull(Timestamp.class))).thenReturn(22.0);

		Response response = deploymentRest.getPowerConsumption("111", "1");
		assertEquals(200, response.getStatus());
		
		String xml = (String) response.getEntity();
		JAXBContext jaxbContext = JAXBContext.newInstance(PowerMeasurement.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		PowerMeasurement powerMeasurement = (PowerMeasurement) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		assertEquals(22.0, powerMeasurement.getValue(), 0.000001);
		assertEquals("Aggregated power consumption in W for this aplication deployment", powerMeasurement.getDescription());
	}
	
	@Test
	@SuppressWarnings(value = { "static-access", "unchecked" }) 
	public void getEnergyEstimationForEventTest() throws JAXBException {
		Deployment deployment = new Deployment();
		deployment.setId(1);
		
		VM vm1 = new VM();
		vm1.setId(1);
		vm1.setProviderVmId("X1");
		deployment.addVM(vm1);
		
		VM vm2 = new VM();
		vm2.setId(2);
		vm2.setProviderVmId("X2");
		deployment.addVM(vm2);
		
		PaaSEnergyModeller energyModeller = mock(PaaSEnergyModeller.class);
		DeploymentRest deploymentRest = new DeploymentRest();
		
		deploymentRest.energyModeller = energyModeller;
		
		DeploymentDAO deploymentDAO = mock(DeploymentDAO.class);
		deploymentRest.deploymentDAO = deploymentDAO;
		when(deploymentDAO.getById(1)).thenReturn(deployment);
				
		when(energyModeller.estimate(isNull(String.class),  eq("111"), eq("1"),  argThat(new BaseMatcher<List<String>>() {
			 
			@Override
			public boolean matches(Object arg0) {
				
				List<String> ids = (List<String>) arg0;
				
				boolean isTheList = true;
				
				if(!(ids.size() == 2)) isTheList = false; 
				
				if(!(ids.get(0).equals("1"))) isTheList = false;
				
				if(!(ids.get(1).equals("2"))) isTheList = false;

				return isTheList;
			}

			@Override
			public void describeTo(Description arg0) {}
		
		}), eq("eventX"), eq(Unit.ENERGY), eq(0l))).thenReturn(22.0);
		
		
		Response response = deploymentRest.getEnergyEstimationForEvent("111", "1", "eventX");
		
		String xml = (String) response.getEntity();
		JAXBContext jaxbContext = JAXBContext.newInstance(EnergyMeasurement.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		EnergyMeasurement energyMeasurement = (EnergyMeasurement) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		assertEquals(22.0, energyMeasurement.getValue(), 0.000001);
		assertEquals("Aggregated energy estimation for this aplication deployment and specific event", energyMeasurement.getDescription());
	}
	
	@Test
	@SuppressWarnings(value = { "static-access", "unchecked" }) 
	public void getPowerEstimationForEventTest() throws JAXBException {
		Deployment deployment = new Deployment();
		deployment.setId(1);
		
		VM vm1 = new VM();
		vm1.setId(1);
		vm1.setProviderVmId("X1");
		deployment.addVM(vm1);
		
		VM vm2 = new VM();
		vm2.setId(2);
		vm2.setProviderVmId("X2");
		deployment.addVM(vm2);
		
		PaaSEnergyModeller energyModeller = mock(PaaSEnergyModeller.class);
		DeploymentRest deploymentRest = new DeploymentRest();
		
		deploymentRest.energyModeller = energyModeller;
		
		DeploymentDAO deploymentDAO = mock(DeploymentDAO.class);
		deploymentRest.deploymentDAO = deploymentDAO;
		when(deploymentDAO.getById(1)).thenReturn(deployment);
				
		when(energyModeller.estimate(isNull(String.class),  eq("111"), eq("1"),  argThat(new BaseMatcher<List<String>>() {
			 
			@Override
			public boolean matches(Object arg0) {
				
				List<String> ids = (List<String>) arg0;
				
				boolean isTheList = true;
				
				if(!(ids.size() == 2)) isTheList = false; 
				
				if(!(ids.get(0).equals("1"))) isTheList = false;
				
				if(!(ids.get(1).equals("2"))) isTheList = false;

				return isTheList;
			}

			@Override
			public void describeTo(Description arg0) {}
		
		}), eq("eventX"), eq(Unit.POWER), eq(0l))).thenReturn(22.0);
		
		
		Response response = deploymentRest.getPowerEstimationForEvent("111", "1", "eventX");
		
		String xml = (String) response.getEntity();
		JAXBContext jaxbContext = JAXBContext.newInstance(PowerMeasurement.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		PowerMeasurement powerMeasurement = (PowerMeasurement) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		assertEquals(22.0, powerMeasurement.getValue(), 0.000001);
		assertEquals("Aggregated power estimation for this aplication deployment and specific event", powerMeasurement.getDescription());
	}
	
	@Test
	@SuppressWarnings(value = { "static-access", "unchecked" }) 
	public void getEnergyConsumptionForEventTest() throws JAXBException {
		Deployment deployment = new Deployment();
		deployment.setId(1);
		
		VM vm1 = new VM();
		vm1.setId(1);
		vm1.setProviderVmId("X1");
		deployment.addVM(vm1);
		
		VM vm2 = new VM();
		vm2.setId(2);
		vm2.setProviderVmId("X2");
		deployment.addVM(vm2);
		
		PaaSEnergyModeller energyModeller = mock(PaaSEnergyModeller.class);
		DeploymentRest deploymentRest = new DeploymentRest();
		
		deploymentRest.energyModeller = energyModeller;
		
		DeploymentDAO deploymentDAO = mock(DeploymentDAO.class);
		deploymentRest.deploymentDAO = deploymentDAO;
		when(deploymentDAO.getById(1)).thenReturn(deployment);
				
		when(energyModeller.measure(isNull(String.class),  eq("111"), eq("1"),  argThat(new BaseMatcher<List<String>>() {
			 
			@Override
			public boolean matches(Object arg0) {
				
				List<String> ids = (List<String>) arg0;
				
				boolean isTheList = true;
				
				if(!(ids.size() == 2)) isTheList = false; 
				
				if(!(ids.get(0).equals("1"))) isTheList = false;
				
				if(!(ids.get(1).equals("2"))) isTheList = false;

				return isTheList;
			}

			@Override
			public void describeTo(Description arg0) {}
		
		}), eq("eventX"), eq(Unit.ENERGY), isNull(Timestamp.class), isNull(Timestamp.class))).thenReturn(22.0);
		
		
		Response response = deploymentRest.getEnergyMeasurementForEvent("111", "1", "eventX");
		
		String xml = (String) response.getEntity();
		JAXBContext jaxbContext = JAXBContext.newInstance(EnergyMeasurement.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		EnergyMeasurement energyMeasurement = (EnergyMeasurement) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		assertEquals(22.0, energyMeasurement.getValue(), 0.000001);
		assertEquals("Aggregated energy consumption for this aplication deployment and specific event", energyMeasurement.getDescription());
	}
	
	@Test
	@SuppressWarnings(value = { "static-access", "unchecked" }) 
	public void getPowerMeasurementForEventTest() throws JAXBException {
		Deployment deployment = new Deployment();
		deployment.setId(1);
		
		VM vm1 = new VM();
		vm1.setId(1);
		vm1.setProviderVmId("X1");
		deployment.addVM(vm1);
		
		VM vm2 = new VM();
		vm2.setId(2);
		vm2.setProviderVmId("X2");
		deployment.addVM(vm2);
		
		PaaSEnergyModeller energyModeller = mock(PaaSEnergyModeller.class);
		DeploymentRest deploymentRest = new DeploymentRest();
		
		deploymentRest.energyModeller = energyModeller;
		
		DeploymentDAO deploymentDAO = mock(DeploymentDAO.class);
		deploymentRest.deploymentDAO = deploymentDAO;
		when(deploymentDAO.getById(1)).thenReturn(deployment);
				
		when(energyModeller.measure(isNull(String.class),  eq("111"), eq("1"),  argThat(new BaseMatcher<List<String>>() {
			 
			@Override
			public boolean matches(Object arg0) {
				
				List<String> ids = (List<String>) arg0;
				
				boolean isTheList = true;
				
				if(!(ids.size() == 2)) isTheList = false; 
				
				if(!(ids.get(0).equals("1"))) isTheList = false;
				
				if(!(ids.get(1).equals("2"))) isTheList = false;

				return isTheList;
			}

			@Override
			public void describeTo(Description arg0) {}
		
		}), eq("eventX"), eq(Unit.POWER), isNull(Timestamp.class), isNull(Timestamp.class))).thenReturn(22.0);
		
		
		Response response = deploymentRest.getPowerConsumptionForEvent("111", "1", "eventX");
		
		String xml = (String) response.getEntity();
		JAXBContext jaxbContext = JAXBContext.newInstance(PowerMeasurement.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		PowerMeasurement powerMeasurement = (PowerMeasurement) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		assertEquals(22.0, powerMeasurement.getValue(), 0.000001);
		assertEquals("Aggregated power consumption for this aplication deployment and specific event", powerMeasurement.getDescription());
	}
	
	@Test
	public void getVmsIdsTest() {
		Deployment deployment = new Deployment();
		
		VM vm1 = new VM();
		vm1.setId(1);
		vm1.setProviderVmId("X1");
		deployment.addVM(vm1);
		
		VM vm2 = new VM();
		vm2.setId(2);
		vm2.setProviderVmId("X2");
		deployment.addVM(vm2);
		
		DeploymentRest rest = new DeploymentRest();
		
		List<String> ids = rest.getVmsIds(deployment);
		
		assertEquals(2, ids.size());
		assertEquals("1", ids.get(0));
		assertEquals("2", ids.get(1));
	}
	
	@Test
	public void providerIdsAndItsVMIdsTest() {
		Deployment deployment = new Deployment();
		
		VM vm1 = new VM();
		vm1.setId(1);
		vm1.setProviderVmId("X1");
		vm1.setProviderId("1");
		deployment.addVM(vm1);
		
		VM vm2 = new VM();
		vm2.setId(2);
		vm2.setProviderVmId("X2");
		vm2.setProviderId("2");
		deployment.addVM(vm2);
		
		VM vm3 = new VM();
		vm3.setId(3);
		vm3.setProviderVmId("X3");
		vm3.setProviderId("2");
		deployment.addVM(vm3);
		
		VM vm4 = new VM();
		vm4.setId(4);
		vm4.setProviderVmId("X4");
		vm4.setProviderId("");
		deployment.addVM(vm4);
		
		VM vm5 = new VM();
		vm5.setId(5);
		vm5.setProviderVmId("X5");
		vm5.setProviderId(null);
		deployment.addVM(vm5);
		
		DeploymentRest rest = new DeploymentRest();
		
		Map<String, List<String>> ids = rest.providerIdsAndItsVMIds(deployment);
		
		assertEquals(3, ids.size());
		assertEquals("X1", ids.get("1").get(0));
		assertEquals("X2", ids.get("2").get(0));
		assertEquals("X3", ids.get("2").get(1));
		assertEquals("X4", ids.get("-1").get(0));
		assertEquals("X5", ids.get("-1").get(1));
	}
	
	@Test
	public void getVmProvidersIdsTest() {
		Deployment deployment = new Deployment();
		
		VM vm1 = new VM();
		vm1.setId(1);
		vm1.setProviderVmId("X1");
		deployment.addVM(vm1);
		
		VM vm2 = new VM();
		vm2.setId(2);
		vm2.setProviderVmId("X2");
		deployment.addVM(vm2);
		
		DeploymentRest rest = new DeploymentRest();
		
		List<String> ids = rest.getVmsProviderIds(deployment);
		
		assertEquals(2, ids.size());
		assertEquals("X1", ids.get(0));
		assertEquals("X2", ids.get(1));
	}
	
	@Test
	@SuppressWarnings(value = { "static-access", "unchecked" }) 
	public void getCostTest() throws JAXBException {
		Deployment deployment = new Deployment();
		deployment.setId(1);
		deployment.setSchema(3);
		
		VM vm1 = new VM();
		vm1.setId(1);
		vm1.setProviderVmId("X1");
		vm1.setProviderId("1");
		deployment.addVM(vm1);
		
		VM vm2 = new VM();
		vm2.setId(2);
		vm2.setProviderVmId("X2");
		vm2.setProviderId("2");
		deployment.addVM(vm2);
		
		PRClient prClient = mock(PRClient.class);
		
		DeploymentRest deploymentRest = new DeploymentRest();
		deploymentRest.prClient = prClient;
		
		DeploymentDAO deploymentDAO = mock(DeploymentDAO.class);
		deploymentRest.deploymentDAO = deploymentDAO;
		when(deploymentDAO.getById(1)).thenReturn(deployment);
		
		VmManagerClient vmManagerClient1 = mock(VmManagerClient.class);
		VmManagerClient vmManagerClient2 = mock(VmManagerClient.class);
		when(prClient.getVMMClient(1)).thenReturn(vmManagerClient1);
		when(prClient.getVMMClient(2)).thenReturn(vmManagerClient2);
		
		List<String> ids = new ArrayList<String>();
		ids.add("X1");
		List<VmCost> vmCosts = new ArrayList<VmCost>();
		vmCosts.add(new VmCost("X1", 2.1));
		when(vmManagerClient1.getVMCosts(ids)).thenReturn(vmCosts);
		
		List<String> ids2 = new ArrayList<String>();
		ids2.add("X2");
		List<VmCost> vmCosts2 = new ArrayList<VmCost>();
		vmCosts2.add(new VmCost("X2", 3.2));
		when(vmManagerClient2.getVMCosts(ids2)).thenReturn(vmCosts2);
		
		PriceModellerClient pmClient = mock(PriceModellerClient.class);
		deploymentRest.priceModellerClient = pmClient;
		
		when(pmClient.getAppTotalCharges(1, 3, 5.300000000000001)).thenReturn(6.7);
		
		Response response = deploymentRest.getCost("111", "1");
		
		String xml = (String) response.getEntity();
		JAXBContext jaxbContext = JAXBContext.newInstance(Cost.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		Cost cost = (Cost) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		assertEquals(6.7, cost.getCharges(), 0.0001);
		assertEquals(-1.0, cost.getPowerValue(), 0.0001);
		assertEquals(-1.0, cost.getEnergyValue(), 0.0001);
	}
	
	@Test
	@SuppressWarnings(value = { "static-access" }) 
	public void testGetCostEstimationForDeploymentAndEvent() throws Exception {
		DeploymentRest deploymentRest = new DeploymentRest();
		PaaSEnergyModeller energyModeller = mock(PaaSEnergyModeller.class);
		deploymentRest.energyModeller = energyModeller;
		PriceModellerClient priceModellerClient = mock(PriceModellerClient.class);
		deploymentRest.priceModellerClient = priceModellerClient;
		EnergyModellerQueueController emController = mock(EnergyModellerQueueController.class);
		deploymentRest.energyModellerQueueController = emController;
		DeploymentDAO deploymentDAO = mock(DeploymentDAO.class);
		deploymentRest.deploymentDAO = deploymentDAO;
		
		VM vm = new VM();
		vm.setId(2);
		vm.setProviderVmId("abab");
		vm.setCpuActual(1);
		vm.setRamActual(10);
		vm.setDiskActual(1);
		
		Deployment deployment = new Deployment();
		deployment.setSchema(2);
		deployment.addVM(vm);
		
		when(deploymentDAO.getById(2)).thenReturn(deployment);
		
		
		List<String> ids = new ArrayList<String>();
		ids.add("2");
		when(energyModeller.estimate(null,  "app-name", "2", ids, "loquesea", Unit.ENERGY, 0l)).thenReturn(22.0);
		when(energyModeller.estimate(null,  "app-name", "2", ids, "loquesea", Unit.POWER, 0l)).thenReturn(23.0);
		
		EnergyModellerMessage secMessage = new EnergyModellerMessage();
		secMessage.setValue("10");
		String secKey = EnergyModellerQueueController.generateKey("app-name", "loquesea", "2", ids, EnergyModellerQueueController.SEC);
		when(emController.getPredictionMessage(secKey)).thenReturn(secMessage);
		
		LinkedList<VMinfo> vmInfos = new LinkedList<VMinfo>();
		for(VM vmFromList : deployment.getVms()) {
			 VMinfo vmInfo = new VMinfo(vmFromList.getRamActual(), 
					 					vmFromList.getCpuActual(), 
					 					vmFromList.getDiskActual() * 1024l,
					 					10l);
			 
			 vmInfos.add(vmInfo);
		}
		
		when(priceModellerClient.getEventPredictedChargesOfApp(eq(2), any(vmInfos.getClass()), eq(22.0), eq(deployment.getSchema()))).thenReturn(1.1d);
		
		Response response = deploymentRest.getCostEstimation("app-name", "2", "loquesea");
		assertEquals(200, response.getStatus());
		
		String xml = (String) response.getEntity();
		
		JAXBContext jaxbContext = JAXBContext.newInstance(Cost.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		Cost cost = (Cost) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		assertEquals("/applications/app-name/deployments/2/events/loquesea/cost-estimation", cost.getHref());
		assertEquals(1.1d, cost.getCharges().doubleValue(), 0.0001);
		assertEquals("Energy estimation in WATTHOURS", cost.getEnergyDescription());
		assertEquals("Power estimation in WATTS", cost.getPowerDescription());
		assertEquals(23.0d, cost.getPowerValue(), 0.0001);
		assertEquals(22.0d, cost.getEnergyValue().doubleValue(), 0.0001);
		assertEquals("Charges estimation in EUROS", cost.getChargesDescription());
		assertEquals(2, cost.getLinks().size());
		assertEquals("/applications/app-name/deployments/2", cost.getLinks().get(0).getHref());
		assertEquals("parent", cost.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, cost.getLinks().get(0).getType());
		assertEquals("/applications/app-name/deployments/2/events/loquesea/cost-estimation", cost.getLinks().get(1).getHref());
		assertEquals("self", cost.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, cost.getLinks().get(1).getType());
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

