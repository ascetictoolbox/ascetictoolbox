package eu.ascetic.paas.applicationmanager.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import eu.ascetic.amqp.client.AmqpMessageReceiver;
import eu.ascetic.paas.applicationmanager.amqp.AbstractTest;
import eu.ascetic.paas.applicationmanager.amqp.AmqpListListener;
import eu.ascetic.paas.applicationmanager.conf.Configuration;
import eu.ascetic.paas.applicationmanager.dao.ApplicationDAO;
import eu.ascetic.paas.applicationmanager.event.DeploymentEvent;
import eu.ascetic.paas.applicationmanager.event.deployment.DeploymentEventService;
import eu.ascetic.paas.applicationmanager.model.Application;
import eu.ascetic.paas.applicationmanager.model.Collection;
import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.paas.applicationmanager.model.Dictionary;
import eu.ascetic.paas.applicationmanager.model.converter.ModelConverter;

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
 * Set of unit tests that verify the correct work of the Application Manager Rest Interface
 *
 */

public class ApplicationRestTest extends AbstractTest {
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
	public void getApplicationsTest() throws JAXBException {
		ApplicationRest applicationRest = prepareApplicationRestForGetApplicationsTest();
		
		Response response = applicationRest.getApplications();
		
		assertEquals(200, response.getStatus());
		String xml = (String) response.getEntity();
		
		Collection collection = ModelConverter.xmlCollectionToObject(xml);
		
		evaluateResutlsOfGetCollectionsOfApplications(collection);
	}
	
	private ApplicationRest prepareApplicationRestForGetApplicationsTest() {
		ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
		
		Application application1 = new Application();
		application1.setId(1);
		application1.setName("name-1");

		Application application2 = new Application();
		application2.setId(2);
		application2.setName("name-2");
		
		List<Application> applications = new ArrayList<Application>();
		applications.add(application1);
		applications.add(application2);
		
		when(applicationDAO.getAllWithOutDeployments()).thenReturn(applications);
		
		ApplicationRest applicationRest = new ApplicationRest();
		applicationRest.applicationDAO = applicationDAO;
		
		return applicationRest;
	}
	
	private void evaluateResutlsOfGetCollectionsOfApplications(Collection collection) {
		//Collection
		assertEquals("/applications", collection.getHref());
		assertEquals(0, collection.getItems().getOffset());
		assertEquals(2, collection.getItems().getTotal());
		assertEquals(2, collection.getItems().getApplications().size());
		//Application 1
		assertEquals(1, collection.getItems().getApplications().get(0).getId());
		assertEquals("/applications/name-1", collection.getItems().getApplications().get(0).getHref());
		assertEquals(4, collection.getItems().getApplications().get(0).getLinks().size());
		assertEquals("/applications", collection.getItems().getApplications().get(0).getLinks().get(0).getHref());
		assertEquals("parent", collection.getItems().getApplications().get(0).getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, collection.getItems().getApplications().get(0).getLinks().get(0).getType());
		assertEquals("/applications/name-1", collection.getItems().getApplications().get(0).getLinks().get(1).getHref());
		assertEquals("self", collection.getItems().getApplications().get(0).getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, collection.getItems().getApplications().get(0).getLinks().get(1).getType());
		assertEquals("/applications/name-1/deployments", collection.getItems().getApplications().get(0).getLinks().get(2).getHref());
		assertEquals("deployments", collection.getItems().getApplications().get(0).getLinks().get(2).getRel());
		assertEquals(MediaType.APPLICATION_XML, collection.getItems().getApplications().get(0).getLinks().get(2).getType());
		//Application 2
		assertEquals(2, collection.getItems().getApplications().get(1).getId());
		assertEquals("/applications/name-2", collection.getItems().getApplications().get(1).getHref());
		assertEquals(4, collection.getItems().getApplications().get(0).getLinks().size());
		assertEquals("/applications", collection.getItems().getApplications().get(1).getLinks().get(0).getHref());
		assertEquals("parent", collection.getItems().getApplications().get(1).getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, collection.getItems().getApplications().get(1).getLinks().get(0).getType());
		assertEquals("/applications/name-2", collection.getItems().getApplications().get(1).getLinks().get(1).getHref());
		assertEquals("self", collection.getItems().getApplications().get(1).getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, collection.getItems().getApplications().get(1).getLinks().get(1).getType());
		assertEquals("/applications/name-2/deployments", collection.getItems().getApplications().get(1).getLinks().get(2).getHref());
		assertEquals("deployments", collection.getItems().getApplications().get(1).getLinks().get(2).getRel());
		assertEquals(MediaType.APPLICATION_XML, collection.getItems().getApplications().get(1).getLinks().get(2).getType());
		// Collection Links
		assertEquals("/", collection.getLinks().get(0).getHref());
		assertEquals("parent", collection.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, collection.getLinks().get(0).getType());
		assertEquals("/applications", collection.getLinks().get(1).getHref());
		assertEquals("self", collection.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, collection.getLinks().get(1).getType());
	}
	
	@Test
	public void getApplicationsJSONTest() {
		ApplicationRest applicationRest = prepareApplicationRestForGetApplicationsTest();
		
		Response response = applicationRest.getApplicationsJSON();
		
		assertEquals(200, response.getStatus());
		
		String json = (String) response.getEntity();
		
		Collection collection = ModelConverter.jsonCollectionToObject(json);
		
		evaluateResutlsOfGetCollectionsOfApplications(collection);
	}
	
	@Test
	public void getApplicationTest() {
		ApplicationRest applicationRest = getApplicationRestForGetApplicationTests();
		
		Response response = applicationRest.getApplication("1");
		
		assertEquals(200, response.getStatus());
		
		String xml = (String) response.getEntity();
		
		Application applicationResponse = ModelConverter.xmlApplicationToObject(xml);
		
		verifyGetApplicationTests(applicationResponse);
	}
	
	private ApplicationRest getApplicationRestForGetApplicationTests() {
		ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
		
		Application application = new Application();
		application.setId(1);
		application.setName("name");
		
		when(applicationDAO.getByNameWithoutDeployments("1")).thenReturn(application);
		
		ApplicationRest applicationRest = new ApplicationRest();
		applicationRest.applicationDAO = applicationDAO;
		
		return applicationRest;
	}
	
	private void verifyGetApplicationTests(Application applicationResponse) {
		assertEquals(1, applicationResponse.getId());
		assertEquals("/applications/name", applicationResponse.getHref());
		assertEquals(4, applicationResponse.getLinks().size());
		assertEquals("/applications", applicationResponse.getLinks().get(0).getHref());
		assertEquals("parent", applicationResponse.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, applicationResponse.getLinks().get(0).getType());
		assertEquals("/applications/name", applicationResponse.getLinks().get(1).getHref());
		assertEquals("self",applicationResponse.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, applicationResponse.getLinks().get(1).getType());
		assertEquals("/applications/name/deployments", applicationResponse.getLinks().get(2).getHref());
		assertEquals("deployments",applicationResponse.getLinks().get(2).getRel());
		assertEquals(MediaType.APPLICATION_XML, applicationResponse.getLinks().get(2).getType());
	}
	
	@Test
	public void getApplicationJSONTest() {
		ApplicationRest applicationRest = getApplicationRestForGetApplicationTests();
		Response response = applicationRest.getApplicationJSON("1");
		
		assertEquals(200, response.getStatus());
		
		String json = (String) response.getEntity();

		Application applicationResponse = ModelConverter.jsonApplicationToObject(json);
		
		verifyGetApplicationTests(applicationResponse);
	}
	
	
	@Test
	public void postAnApplicationInDB() throws Exception {
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
		
		ApplicationRest applicationRest = new ApplicationRest();
		applicationRest.applicationDAO = applicationDAO;
		applicationRest.deploymentEventService = deploymentEventService;
		
		Response response = applicationRest.postApplication("automatic", null, threeTierWebAppOvfString);
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
		assertEquals(1, applicationResponse.getDeployments().get(0).getSchema());
		assertEquals(Dictionary.APPLICATION_STATUS_SUBMITTED, applicationResponse.getDeployments().get(0).getStatus());
		Pattern p = Pattern.compile("\\d\\d/\\d\\d/\\d\\d\\d\\d:\\d\\d:\\d\\d:\\d\\d \\+\\d\\d\\d\\d");
		Matcher m = p.matcher(applicationResponse.getDeployments().get(0).getStartDate());
		assertTrue(m.matches());
		
		// We verify the number of calls to the DAO
		verify(applicationDAO, times(2)).getByName("threeTierWebApp");
		verify(applicationDAO, times(1)).update(any(Application.class));
		
		//We verify that the event is fired
		ArgumentCaptor<DeploymentEvent> argument = ArgumentCaptor.forClass(DeploymentEvent.class);
		verify(deploymentEventService).fireDeploymentEvent(argument.capture());
		
		assertEquals(Dictionary.APPLICATION_STATUS_SUBMITTED, argument.getValue().getDeploymentStatus());
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
	public void postAnApplicationInDBJSON() throws Exception {
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
		
		ApplicationRest applicationRest = new ApplicationRest();
		applicationRest.applicationDAO = applicationDAO;
		applicationRest.deploymentEventService = deploymentEventService;
		
		Response response = applicationRest.postApplicationJSON("automatic", null, threeTierWebAppOvfString);
		assertEquals(201, response.getStatus());
		
		String json = (String) response.getEntity();
		
		Application applicationResponse = ModelConverter.jsonApplicationToObject(json);
		
		// We verify the application was stored correctly
		assertEquals(1, applicationResponse.getId());
		assertEquals("/applications/threeTierWebApp", applicationResponse.getHref());
		assertEquals("threeTierWebApp", applicationResponse.getName());
		assertEquals(1, applicationResponse.getDeployments().size());
		assertEquals(threeTierWebAppOvfString, applicationResponse.getDeployments().get(0).getOvf());
		assertEquals(1, applicationResponse.getDeployments().get(0).getSchema());
		assertEquals(Dictionary.APPLICATION_STATUS_SUBMITTED, applicationResponse.getDeployments().get(0).getStatus());
		Pattern p = Pattern.compile("\\d\\d/\\d\\d/\\d\\d\\d\\d:\\d\\d:\\d\\d:\\d\\d \\+\\d\\d\\d\\d");
		Matcher m = p.matcher(applicationResponse.getDeployments().get(0).getStartDate());
		assertTrue(m.matches());
		
		// We verify the number of calls to the DAO
		verify(applicationDAO, times(2)).getByName("threeTierWebApp");
		verify(applicationDAO, times(1)).update(any(Application.class));
		
		//We verify that the event is fired
		ArgumentCaptor<DeploymentEvent> argument = ArgumentCaptor.forClass(DeploymentEvent.class);
		verify(deploymentEventService).fireDeploymentEvent(argument.capture());
		
		assertEquals(Dictionary.APPLICATION_STATUS_SUBMITTED, argument.getValue().getDeploymentStatus());
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
	public void postAnApplicationInDBNoAutonomicNegotiationTest() throws Exception {
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
		
		ApplicationRest applicationRest = new ApplicationRest();
		applicationRest.applicationDAO = applicationDAO;
		applicationRest.deploymentEventService = deploymentEventService;
		
		Response response = applicationRest.postApplication("manual", "2", threeTierWebAppOvfString);
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
		assertEquals(2, applicationResponse.getDeployments().get(0).getSchema());
		assertEquals(Dictionary.APPLICATION_STATUS_SUBMITTED, applicationResponse.getDeployments().get(0).getStatus());
		Pattern p = Pattern.compile("\\d\\d/\\d\\d/\\d\\d\\d\\d:\\d\\d:\\d\\d:\\d\\d \\+\\d\\d\\d\\d");
		Matcher m = p.matcher(applicationResponse.getDeployments().get(0).getStartDate());
		assertTrue(m.matches());
		
		// We verify the number of calls to the DAO
		verify(applicationDAO, times(2)).getByName("threeTierWebApp");
		verify(applicationDAO, times(1)).update(any(Application.class));
		
		//We verify that the event is fired
		ArgumentCaptor<DeploymentEvent> argument = ArgumentCaptor.forClass(DeploymentEvent.class);
		verify(deploymentEventService).fireDeploymentEvent(argument.capture());
		
		assertEquals(Dictionary.APPLICATION_STATUS_SUBMITTED, argument.getValue().getDeploymentStatus());
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
	public void postAnApplicationNotInDB() throws Exception {
		// We set a listener to get the sent message from the MessageQueue
		AmqpMessageReceiver receiver = new AmqpMessageReceiver(Configuration.amqpAddress, Configuration.amqpUsername, Configuration.amqpPassword,  "APPLICATION.>", true);
		AmqpListListener listener = new AmqpListListener();
		receiver.setMessageConsumer(listener);
		
		ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
		
		Application application = new Application();
		application.setId(1);
		application.setName("threeTierWebApp");
		
		Deployment deployment = new Deployment();
		deployment.setId(1);
		deployment.setStatus(Dictionary.APPLICATION_STATUS_SUBMITTED);
		application.addDeployment(deployment);
		
		// We put in order the different calls to the DB
		when(applicationDAO.getByName("threeTierWebApp")).thenReturn(null, application);
		when(applicationDAO.save(any(Application.class))).thenReturn(true);
		
		DeploymentEventService deploymentEventService = mock(DeploymentEventService.class);
		
		ApplicationRest applicationRest = new ApplicationRest();
		applicationRest.applicationDAO = applicationDAO;
		applicationRest.deploymentEventService = deploymentEventService;
		
		Response response = applicationRest.postApplication("automatic", null, threeTierWebAppOvfString);
		assertEquals(201, response.getStatus());
		
		String xml = (String) response.getEntity();
		
		JAXBContext jaxbContext = JAXBContext.newInstance(Application.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		Application applicationResponse = (Application) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		// We verify the application was stored correctly
		assertEquals(1, applicationResponse.getId());
		assertEquals("/applications/threeTierWebApp", applicationResponse.getHref());
		assertEquals("threeTierWebApp", applicationResponse.getName());
		
		// We verify the number of calls to the DAO
		verify(applicationDAO, times(2)).getByName("threeTierWebApp");
		verify(applicationDAO, times(1)).save(any(Application.class));
		
		ArgumentCaptor<DeploymentEvent> argument = ArgumentCaptor.forClass(DeploymentEvent.class);
		verify(deploymentEventService).fireDeploymentEvent(argument.capture());
		
		assertEquals(Dictionary.APPLICATION_STATUS_SUBMITTED, argument.getValue().getDeploymentStatus());
		assertEquals(true, argument.getValue().isAutomaticNegotiation());
	
		// We verify that the right messages were sent to the AMQP
		Thread.sleep(1000l);
		assertEquals(2, listener.getTextMessages().size());
		assertEquals("APPLICATION.threeTierWebApp.ADDED", listener.getTextMessages().get(0).getJMSDestination().toString());
		assertEquals("threeTierWebApp", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(0).getText()).getApplicationId());
		
		assertEquals("APPLICATION.threeTierWebApp.DEPLOYMENT.1.SUBMITTED", listener.getTextMessages().get(1).getJMSDestination().toString());
		assertEquals("threeTierWebApp", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(1).getText()).getApplicationId());
		assertEquals("1", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(1).getText()).getDeploymentId());
		assertEquals("SUBMITTED", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(1).getText()).getStatus());
		
		receiver.close();
	}
	
	
	@Test
	public void postInvalidOVFTest() {
		ApplicationRest applicationRest = new ApplicationRest();
		
		Response response = applicationRest.postApplication("automatic", null, "XXX");
		assertEquals(400, response.getStatus());
		
		String message = (String) response.getEntity();
		assertEquals("Invalid OVF", message);
	}
	
	@Test
	public void postInvalidPriceSchema() {
		ApplicationRest applicationRest = new ApplicationRest();
		
		Response response = applicationRest.postApplication("automatic", "a", "XXX");
		assertEquals(400, response.getStatus());
		
		String message = (String) response.getEntity();
		assertEquals("Invalid price schema format: a. Please enter an integer value", message);
	}
	
	@Test
	public void addDeploymentToApplicationTest() {
		ApplicationRest applicationRest = new ApplicationRest();
			
		String ovf = "ovf";
		
		Deployment deployment = applicationRest.createDeploymentToApplication(ovf);

		assertEquals("ovf", deployment.getOvf());
		assertEquals(Dictionary.APPLICATION_STATUS_SUBMITTED, deployment.getStatus());
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
