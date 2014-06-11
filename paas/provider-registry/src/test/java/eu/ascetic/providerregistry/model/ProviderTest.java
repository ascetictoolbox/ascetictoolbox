package eu.ascetic.providerregistry.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit test to verify all the methods and functions of the Provider.class
 * @author David Garcia Perez - Atos
 */
public class ProviderTest {

	@Test
	public void pojoTest() {
		Provider provider = new Provider();
		provider.setId(10);
		provider.setName("Name");
		provider.setEndpoint("http...");
		
		assertEquals(10l, provider.getId());
		assertEquals("Name", provider.getName());
		assertEquals("http...", provider.getEndpoint());
	}
}
