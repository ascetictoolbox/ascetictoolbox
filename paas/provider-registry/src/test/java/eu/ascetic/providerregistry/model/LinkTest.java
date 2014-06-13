package eu.ascetic.providerregistry.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class LinkTest {

	@Test
	public void gettersAndSettersTest() {
		Link link = new Link();
		link.setHref("http://something.com");
		link.setRel("/");
		link.setType("application+xml");
		
		assertEquals("http://something.com", link.getHref());
		assertEquals("/", link.getRel());
		assertEquals("application+xml", link.getType());
	}
	
	@Test
	public void constructorTest() {
		Link link = new Link("/", "http://something.com", "application+xml");
		
		assertEquals("http://something.com", link.getHref());
		assertEquals("/", link.getRel());
		assertEquals("application+xml", link.getType());
	}
}
