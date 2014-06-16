package eu.ascetic.providerregistry.model;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * Unit test to verify all the methods and functions of the Provider.class
 * @author David Garcia Perez - Atos
 */
public class ProviderTest {

	@Test
	public void pojoTest() {
		Provider provider = new Provider();
		provider.setHref("href");
		provider.setId(10);
		provider.setName("Name");
		provider.setEndpoint("http...");
		List<Link> links = new ArrayList<Link>();
		provider.setLinks(links);
		
		assertEquals("href", provider.getHref());
		assertEquals(10l, provider.getId());
		assertEquals("Name", provider.getName());
		assertEquals("http...", provider.getEndpoint());
		assertEquals(links, provider.getLinks());
	}
	
	@Test
	public void addLinkTest() {
		Provider provider = new Provider();
		
		assertEquals(null, provider.getLinks());
		
		Link link = new Link();
		provider.addLink(link);
		
		assertEquals(1, provider.getLinks().size());
		assertEquals(link, provider.getLinks().get(0));
	}
}
