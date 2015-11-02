package eu.ascetic.paas.applicationmanager.datamodel.convert;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import eu.ascetic.paas.applicationmanager.model.Application;
import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.paas.applicationmanager.model.Image;
import eu.ascetic.paas.applicationmanager.model.Link;

public class ApplicationConverterTest {

	@Test
	public void applicationWithoutDeploymentsTest() {
		Application application = new Application();
		
		application.setDeployments(new ArrayList<Deployment>());
		application.setHref("href");
		application.setId(22);
		List<Image> images = new ArrayList<Image>();
		application.setImages(images);
		List<Link> links = new ArrayList<Link>();
		application.setLinks(links);
		application.setName("name");
		
		Application applicationReturned = ApplicationConverter.withoutDeployments(application);
		
		assertEquals("name", applicationReturned.getName());
		assertEquals(null, applicationReturned.getDeployments());
		assertEquals(images, applicationReturned.getImages());
		assertEquals(22, applicationReturned.getId());
		assertEquals("href", applicationReturned.getHref());
		assertEquals(links, applicationReturned.getLinks());
		
		applicationReturned = ApplicationConverter.withoutDeployments(null);
		assertEquals(null, applicationReturned);
	}
}
