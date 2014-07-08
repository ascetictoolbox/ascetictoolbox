package eu.ascetic.paas.applicationmanager.model;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * Collection of unit test to verify that the POJO class representing an Application works as expected
 * @author David Garcia Perez - Atos
 */
public class ApplicationTest {

	@Test
	public void testPojo() {
		Application application = new Application();
		application.setHref("href");
		application.setId(1);
		application.setName("name");
		List<Link> links = new ArrayList<Link>();
		application.setLinks(links);
		List<Deployment> deployments = new ArrayList<Deployment>();
		application.setDeployments(deployments);
		
		assertEquals(links, application.getLinks());
		assertEquals(1, application.getId());
		assertEquals("href", application.getHref());
		assertEquals(deployments, application.getDeployments());
		assertEquals("name", application.getName());
	}
	
	@Test
	public void addLinkTest() {
		Application application = new Application();
		
		assertEquals(null, application.getLinks());
		
		Link link = new Link();
		application.addLink(link);
		assertEquals(link, application.getLinks().get(0));
	}
	
	@Test
	public void addDeploymentTest() {
		Application application = new Application();
		
		assertEquals(null, application.getDeployments());
		
		Deployment deployment = new Deployment();
		application.addDeployment(deployment);
		
		assertEquals(deployment, application.getDeployments().get(0));
	}
}
