package eu.ascetic.providerregistry.rest;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eu.ascetic.providerregistry.model.Provider;
import eu.ascetic.providerregistry.service.ProviderDAO;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/provider-registry-db-JPA-test-context.xml")
public class ProviderAPITest extends AbstractTransactionalJUnit4SpringContextTests {
	@Autowired
	protected ProviderDAO providerDAO;
	
	@Before
	public void setUp() {
		Provider provider1 = new Provider();
		provider1.setName("Provider 1");
		provider1.setEndpoint("http://provider1.ascetic.com");
		
		Provider provider2 = new Provider();
		provider2.setName("Provider 2");
		provider2.setEndpoint("http://provider2.ascetic.com");
		
		boolean saved = providerDAO.save(provider1);
		assertTrue(saved);
		saved = providerDAO.save(provider2);
		assertTrue(saved);
	}
	
	@Test
	public void getExperimentsTest() {
		ProviderAPI providerAPI = new ProviderAPI();
		providerAPI.providerDAO = providerDAO;
		
		
	}
}
