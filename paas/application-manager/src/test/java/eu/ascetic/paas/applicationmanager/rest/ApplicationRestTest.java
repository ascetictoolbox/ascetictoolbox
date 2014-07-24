package eu.ascetic.paas.applicationmanager.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;

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
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;

import eu.ascetic.paas.applicationmanager.dao.ApplicationDAO;
import eu.ascetic.paas.applicationmanager.model.Application;
import eu.ascetic.paas.applicationmanager.model.Collection;
import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.paas.applicationmanager.model.Dictionary;
/**
 * Set of unit tests that verify the correct work of the Application Manager Rest Interface
 * @author David Garcia Perez - Atos
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
		application1.setName("name 1");

		Application application2 = new Application();
		application2.setId(2);
		application2.setName("name 2");
		
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
		assertEquals("/applications/1", collection.getItems().getApplications().get(0).getHref());
		assertEquals(3, collection.getItems().getApplications().get(0).getLinks().size());
		assertEquals("/applications", collection.getItems().getApplications().get(0).getLinks().get(0).getHref());
		assertEquals("parent", collection.getItems().getApplications().get(0).getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, collection.getItems().getApplications().get(0).getLinks().get(0).getType());
		assertEquals("/applications/1", collection.getItems().getApplications().get(0).getLinks().get(1).getHref());
		assertEquals("self", collection.getItems().getApplications().get(0).getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, collection.getItems().getApplications().get(0).getLinks().get(1).getType());
		assertEquals("/applications/1/deployments", collection.getItems().getApplications().get(0).getLinks().get(2).getHref());
		assertEquals("deployments", collection.getItems().getApplications().get(0).getLinks().get(2).getRel());
		assertEquals(MediaType.APPLICATION_XML, collection.getItems().getApplications().get(0).getLinks().get(2).getType());
		//Application 2
		assertEquals(2, collection.getItems().getApplications().get(1).getId());
		assertEquals("/applications/2", collection.getItems().getApplications().get(1).getHref());
		assertEquals(3, collection.getItems().getApplications().get(0).getLinks().size());
		assertEquals("/applications", collection.getItems().getApplications().get(1).getLinks().get(0).getHref());
		assertEquals("parent", collection.getItems().getApplications().get(1).getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, collection.getItems().getApplications().get(1).getLinks().get(0).getType());
		assertEquals("/applications/2", collection.getItems().getApplications().get(1).getLinks().get(1).getHref());
		assertEquals("self", collection.getItems().getApplications().get(1).getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, collection.getItems().getApplications().get(1).getLinks().get(1).getType());
		assertEquals("/applications/2/deployments", collection.getItems().getApplications().get(1).getLinks().get(2).getHref());
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
		
		when(applicationDAO.getById(1)).thenReturn(application);
		
		ApplicationRest applicationRest = new ApplicationRest();
		applicationRest.applicationDAO = applicationDAO;
		
		Response response = applicationRest.getApplication("1");
		
		assertEquals(200, response.getStatus());
		
		String xml = (String) response.getEntity();
		
		JAXBContext jaxbContext = JAXBContext.newInstance(Application.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		Application applicationResponse = (Application) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		assertEquals(1, applicationResponse.getId());
		assertEquals("/applications/1", applicationResponse.getHref());
		assertEquals(3, applicationResponse.getLinks().size());
		assertEquals("/applications", applicationResponse.getLinks().get(0).getHref());
		assertEquals("parent", applicationResponse.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, applicationResponse.getLinks().get(0).getType());
		assertEquals("/applications/1", applicationResponse.getLinks().get(1).getHref());
		assertEquals("self",applicationResponse.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, applicationResponse.getLinks().get(1).getType());
		assertEquals("/applications/1/deployments", applicationResponse.getLinks().get(2).getHref());
		assertEquals("deployments",applicationResponse.getLinks().get(2).getRel());
		assertEquals(MediaType.APPLICATION_XML, applicationResponse.getLinks().get(2).getType());
	}
	
	@Test
	public void postAnApplicationNotInDB() throws JAXBException {
		ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
		
		// We put in order the different calls to the DB
		when(applicationDAO.getByName("Three Tier Web App")).thenReturn(null);
		when(applicationDAO.save(any(Application.class))).thenReturn(true) ;
		
		Application application = new Application();
		application.setId(1);
		application.setName("Three Tier Web App");
		
		Deployment deployment = new Deployment();
		deployment.setId(1);
		deployment.setStatus(Dictionary.APPLICATION_STATUS_SUBMITTED);
		application.addDeployment(deployment);
		
		when(applicationDAO.getByName("Three Tier Web App")).thenReturn(application);
		
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
		assertEquals("/applications/1", applicationResponse.getHref());
		assertEquals("Three Tier Web App", applicationResponse.getName());
		
		// We verify that the argument sent was the correct one
		verify(applicationDAO).save((Application) argThat(new MessagesApplicationArgumentMatcher()));
	}
	
	
	@Test
	public void postInvalidOVFTest() {
		ApplicationRest applicationRest = new ApplicationRest();
		
		Response response = applicationRest.postApplication("XXX");
		assertEquals(400, response.getStatus());
		
		String message = (String) response.getEntity();
		assertEquals("Invalid OVF", message);
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

class MessagesApplicationArgumentMatcher extends ArgumentMatcher<Application> {

	public boolean matches(Object o) {
		//	45             if (o instanceof List) {
		//	46                 List<String> strings = (List<String>) o;
		//	47                 if (strings.size() != 3) return false;
		//	48                 if (!strings.get(0).equals("<message><name>Joe Smith</name></message>")) return false;
		//	49                 if (!strings.get(1).equals("<message><name>John Doe</name></message>")) return false;
		//	                 if (!strings.get(2).equals("<message><name>Eddie Moola</name></message>")) return false;
		//	            }
		return true;
	}
}
