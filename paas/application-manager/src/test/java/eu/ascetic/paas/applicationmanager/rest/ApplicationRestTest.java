package eu.ascetic.paas.applicationmanager.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.Test;

import eu.ascetic.paas.applicationmanager.Dictionary;
import eu.ascetic.paas.applicationmanager.dao.ApplicationDAO;
import eu.ascetic.paas.applicationmanager.model.Application;
import eu.ascetic.paas.applicationmanager.model.Collection;
import eu.ascetic.paas.applicationmanager.model.Root;

/**
 * Set of unit tests that verify the correct work of the Application Manager Rest Interface
 * @author David Garcia Perez - Atos
 */
public class ApplicationRestTest {

	@Test
	public void getRootTest() throws JAXBException {
		ApplicationRest applicationRest = new ApplicationRest();
		
		Response response = applicationRest.getRoot();
		String timestamp = "" + System.currentTimeMillis();
		timestamp = timestamp.substring(0, timestamp.length() - 4);
		
		assertEquals(200, response.getStatus());
		
		String providersXML = (String) response.getEntity();
		
		JAXBContext jaxbContext = JAXBContext.newInstance(Root.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		Root root = (Root) jaxbUnmarshaller.unmarshal(new StringReader(providersXML));
		
		assertEquals("/", root.getHref());
		assertEquals("0.1-SNAPSHOT", root.getVersion());
		assertEquals(timestamp, root.getTimestamp().substring(0, root.getTimestamp().length() - 4));
		assertEquals(1, root.getLinks().size());
		assertEquals("applications", root.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, root.getLinks().get(0).getType());
		assertEquals("/applications", root.getLinks().get(0).getHref());
	}
	
	@Test
	public void getApplicationsTest() throws JAXBException {
		ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
		
		Application application1 = new Application();
		application1.setId(1);
		application1.setStatus(Dictionary.APPLICATION_STATUS_DEPLOYED);
		application1.setDeploymentPlanId("deployment-plan-1");
		Application application2 = new Application();
		application2.setId(2);
		application2.setStatus(Dictionary.APPLICATION_STATUS_ERROR);
		application2.setDeploymentPlanId("deployment-plan-2");
		
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
		assertEquals(Dictionary.APPLICATION_STATUS_DEPLOYED, collection.getItems().getApplications().get(0).getStatus());
		assertEquals("deployment-plan-1", collection.getItems().getApplications().get(0).getDeploymentPlanId());
		assertEquals(3, collection.getItems().getApplications().get(0).getLinks().size());
		assertEquals("/applications", collection.getItems().getApplications().get(0).getLinks().get(0).getHref());
		assertEquals("parent", collection.getItems().getApplications().get(0).getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, collection.getItems().getApplications().get(0).getLinks().get(0).getType());
		assertEquals("/applications/1", collection.getItems().getApplications().get(0).getLinks().get(1).getHref());
		assertEquals("self", collection.getItems().getApplications().get(0).getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, collection.getItems().getApplications().get(0).getLinks().get(1).getType());
		assertEquals("/applications/1/ovf", collection.getItems().getApplications().get(0).getLinks().get(2).getHref());
		assertEquals("OVF", collection.getItems().getApplications().get(0).getLinks().get(2).getRel());
		assertEquals(MediaType.APPLICATION_XML, collection.getItems().getApplications().get(0).getLinks().get(2).getType());
		//Application 2
		assertEquals(2, collection.getItems().getApplications().get(1).getId());
		assertEquals("/applications/2", collection.getItems().getApplications().get(1).getHref());
		assertEquals(Dictionary.APPLICATION_STATUS_ERROR, collection.getItems().getApplications().get(1).getStatus());
		assertEquals("deployment-plan-2", collection.getItems().getApplications().get(1).getDeploymentPlanId());
		assertEquals(3, collection.getItems().getApplications().get(0).getLinks().size());
		assertEquals("/applications", collection.getItems().getApplications().get(1).getLinks().get(0).getHref());
		assertEquals("parent", collection.getItems().getApplications().get(1).getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, collection.getItems().getApplications().get(1).getLinks().get(0).getType());
		assertEquals("/applications/2", collection.getItems().getApplications().get(1).getLinks().get(1).getHref());
		assertEquals("self", collection.getItems().getApplications().get(1).getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, collection.getItems().getApplications().get(1).getLinks().get(1).getType());
		assertEquals("/applications/2/ovf", collection.getItems().getApplications().get(1).getLinks().get(2).getHref());
		assertEquals("OVF", collection.getItems().getApplications().get(1).getLinks().get(2).getRel());
		assertEquals(MediaType.APPLICATION_XML, collection.getItems().getApplications().get(1).getLinks().get(2).getType());
		// Collection Links
		assertEquals("/applications", collection.getLinks().get(0).getHref());
		assertEquals("self", collection.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, collection.getLinks().get(0).getType());
		assertEquals("/", collection.getLinks().get(1).getHref());
		assertEquals("parent", collection.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, collection.getLinks().get(1).getType());
	}
}
