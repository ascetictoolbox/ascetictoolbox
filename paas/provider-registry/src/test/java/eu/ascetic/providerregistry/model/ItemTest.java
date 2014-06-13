package eu.ascetic.providerregistry.model;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * Test the POJO Item funcitonality
 * @author David Garcia Perez - AtoS
 *
 */
public class ItemTest {
	
	@Test
	public void pojo() {
		Items items = new Items();
		items.setOffset(1);
		items.setTotal(2);
		
		List<Provider> providers = new ArrayList<Provider>();
		items.setProviders(providers);
		
		assertEquals(1, items.getOffset());
		assertEquals(2, items.getTotal());
		assertEquals(providers, items.getProviders());
	}
	
	@Test
	public void addExperiment() {
		Items items = new Items();
		assertEquals(null, items.getProviders());
		
		Provider provider = new Provider();
		items.addProvider(provider);
		
		assertEquals(1, items.getProviders().size());
		assertEquals(provider, items.getProviders().get(0));
	}
}
