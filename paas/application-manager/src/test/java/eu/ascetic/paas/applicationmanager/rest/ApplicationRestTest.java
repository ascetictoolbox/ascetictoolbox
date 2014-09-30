package eu.ascetic.paas.applicationmanager.rest;

import static org.junit.Assert.assertEquals;
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

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.Before;
import org.junit.Test;

import eu.ascetic.paas.applicationmanager.dao.ApplicationDAO;
import eu.ascetic.paas.applicationmanager.model.Application;
import eu.ascetic.paas.applicationmanager.model.Collection;
import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.paas.applicationmanager.model.Dictionary;

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
 * Set of unit tests that verify the correct work of the Application Manager Rest Interface
 *
 */

public class ApplicationRestTest {
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
		
		when(applicationDAO.getAll()).thenReturn(applications);
		
		ApplicationRest applicationRest = new ApplicationRest();
		applicationRest.applicationDAO = applicationDAO;
		
		Response response = applicationRest.getApplications();
		
		assertEquals(200, response.getStatus());
		
		String xml = (String) response.getEntity();
		
		JAXBContext jaxbContext = JAXBContext.newInstance(Collection.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		Collection collection = (Collection) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		//Collection
		assertEquals("/applications", collection.getHref());
		assertEquals(0, collection.getItems().getOffset());
		assertEquals(2, collection.getItems().getTotal());
		assertEquals(2, collection.getItems().getApplications().size());
		//Application 1
		assertEquals(1, collection.getItems().getApplications().get(0).getId());
		assertEquals("/applications/name-1", collection.getItems().getApplications().get(0).getHref());
		assertEquals(3, collection.getItems().getApplications().get(0).getLinks().size());
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
		assertEquals(3, collection.getItems().getApplications().get(0).getLinks().size());
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
	public void getApplicationTest() throws JAXBException {
		ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
		
		Application application = new Application();
		application.setId(1);
		application.setName("name");
		
		when(applicationDAO.getByName("1")).thenReturn(application);
		
		ApplicationRest applicationRest = new ApplicationRest();
		applicationRest.applicationDAO = applicationDAO;
		
		Response response = applicationRest.getApplication("1");
		
		assertEquals(200, response.getStatus());
		
		String xml = (String) response.getEntity();
		
		JAXBContext jaxbContext = JAXBContext.newInstance(Application.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		Application applicationResponse = (Application) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		assertEquals(1, applicationResponse.getId());
		assertEquals("/applications/name", applicationResponse.getHref());
		assertEquals(3, applicationResponse.getLinks().size());
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
	public void postAnApplicationInDB() throws JAXBException {
		ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
		
		Application application = new Application();
		application.setId(1);
		application.setName("threeTierWebApp");
		
		// We put in order the different calls to the DB
		when(applicationDAO.getByName("threeTierWebApp")).thenReturn(application, application);
		when(applicationDAO.update(any(Application.class))).thenReturn(true);
		
		ApplicationRest applicationRest = new ApplicationRest();
		applicationRest.applicationDAO = applicationDAO;
		
		Response response = applicationRest.postApplication(threeTierWebAppOvfString);
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
		assertEquals(Dictionary.APPLICATION_STATUS_NEGOTIATIED, applicationResponse.getDeployments().get(0).getStatus());
		
		// We verify the number of calls to the DAO
		verify(applicationDAO, times(2)).getByName("threeTierWebApp");
		verify(applicationDAO, times(1)).update(any(Application.class));
	}
	
	@Test
	public void postAnApplicationNotInDB() throws JAXBException {
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
		
		ApplicationRest applicationRest = new ApplicationRest();
		applicationRest.applicationDAO = applicationDAO;
		
		Response response = applicationRest.postApplication(threeTierWebAppOvfString);
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
	}
	
	
	@Test
	public void postInvalidOVFTest() {
		ApplicationRest applicationRest = new ApplicationRest();
		
		Response response = applicationRest.postApplication("XXX");
		assertEquals(400, response.getStatus());
		
		String message = (String) response.getEntity();
		assertEquals("Invalid OVF", message);
	}
	
	@Test
	public void addDeploymentToApplicationTest() {
		ApplicationRest applicationRest = new ApplicationRest();
			
		String ovf = "ovf";
		
		Deployment deployment = applicationRest.createDeploymentToApplication(ovf);

		assertEquals("ovf", deployment.getOvf());
		assertEquals(Dictionary.APPLICATION_STATUS_NEGOTIATIED, deployment.getStatus());
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
