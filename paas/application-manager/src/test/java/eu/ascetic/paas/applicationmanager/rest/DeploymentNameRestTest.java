package eu.ascetic.paas.applicationmanager.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.StringReader;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.junit.Test;

import eu.ascetic.paas.applicationmanager.dao.DeploymentDAO;
import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.paas.applicationmanager.model.converter.ModelConverter;

public class DeploymentNameRestTest {

	@Test
	public void getDeploymentTest() throws Exception {
		Deployment deployment = new Deployment();
		deployment.setId(1);
		deployment.setOvf("ovf1");
		deployment.setPrice("price1");
		deployment.setStatus("Status1");
		deployment.setDeploymentName("XXXX");
		
		DeploymentDAO deploymentDAO = mock(DeploymentDAO.class);
		when(deploymentDAO.getDeployment("XXXX")).thenReturn(deployment);
		
		DeploymentNameRest deploymentRest = new DeploymentNameRest();
		deploymentRest.deploymentDAO = deploymentDAO;
		
		Response response = deploymentRest.getDeploymentXML("2", "XXXX");
		
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
		deployment.setDeploymentName("XXXX");
		
		DeploymentDAO deploymentDAO = mock(DeploymentDAO.class);
		when(deploymentDAO.getDeployment("XXXX")).thenReturn(deployment);
		
		DeploymentNameRest deploymentRest = new DeploymentNameRest();
		deploymentRest.deploymentDAO = deploymentDAO;
		
		Response response = deploymentRest.getDeploymentJSON("2", "XXXX");
		
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
}
