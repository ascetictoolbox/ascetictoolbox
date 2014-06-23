package eu.ascetic.paas.applicationmanager.model;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import eu.ascetic.paas.applicationmanager.Dictionary;

/**
 * Collection of unit test to verify that the POJO class representing an Application works as expected
 * @author David Garcia Perez - Atos
 */
public class ApplicationTest {

	@Test
	public void testPojo() {
		Application application = new Application();
		application.setDeploymentPlanId("deployment-plan-id");
		application.setHref("href");
		application.setId(1);
		List<Link> links = new ArrayList<Link>();
		application.setLinks(links);
		application.setStatus(Dictionary.APPLICATION_STATUS_DEPLOYED);
		
		assertEquals(Dictionary.APPLICATION_STATUS_DEPLOYED, application.getStatus());
		assertEquals(links, application.getLinks());
		assertEquals(1, application.getId());
		assertEquals("href", application.getHref());
		assertEquals("deployment-plan-id", application.getDeploymentPlanId());
	}
	
	@Test
	public void addLinkTest() {
		Application application = new Application();
		
		assertEquals(null, application.getLinks());
		
		Link link = new Link();
		application.addLink(link);
		assertEquals(link, application.getLinks().get(0));
	}
}
