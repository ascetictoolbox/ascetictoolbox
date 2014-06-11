package eu.ascetic.providerregistry.service.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

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
public class ProviderDAOJpaTest extends AbstractTransactionalJUnit4SpringContextTests {
	@Autowired
	protected ProviderDAO providerDAO;
	
	@Test
	public void notNull() {
		if(providerDAO == null) fail();
	}
	
	@Test
	public void saveGetAll() {
		int size = providerDAO.getAll().size();
		
		Provider provider = new Provider();
		provider.setName("name");
		provider.setEndpoint("http://...");
		
		boolean saved = providerDAO.save(provider);
		assertTrue(saved);
		
		List<Provider> providers = providerDAO.getAll();
		size = size + 1;
		assertEquals(size, providers.size());
		
		Provider providerFromDatabase = providers.get(size-1);
		assertEquals("name", providerFromDatabase.getName());
		assertEquals("http://...", providerFromDatabase.getEndpoint());
	}
	
	@Test
	public void getById() {
		int size = providerDAO.getAll().size();
		
		Provider provider = new Provider();
		provider.setName("name");
		provider.setEndpoint("http://...");
		
		boolean saved = providerDAO.save(provider);
		assertTrue(saved);
			
		Provider providerFromDatabase = providerDAO.getAll().get(size);
		int id = providerFromDatabase.getId();
		providerFromDatabase = providerDAO.getById(id);
		assertEquals("name", providerFromDatabase.getName());
		assertEquals("http://...", providerFromDatabase.getEndpoint());
		
		Provider nullProvider = providerDAO.getById(30000);
		assertEquals(null, nullProvider);
	}
	
	@Test
	public void delete() {
		int size = providerDAO.getAll().size();
		
		Provider provider = new Provider();
		provider.setName("name");
		provider.setEndpoint("http://...");
		
		boolean saved = providerDAO.save(provider);
		assertTrue(saved);
			
		Provider providerFromDatabase = providerDAO.getAll().get(size);
		int id = providerFromDatabase.getId();
		
		boolean deleted = providerDAO.delete(providerFromDatabase);
		assertTrue(deleted);
		
		deleted = providerDAO.delete(providerFromDatabase);
		assertTrue(!deleted);
		
		providerFromDatabase = providerDAO.getById(id);
		assertEquals(null, providerFromDatabase);
	}
	
	@Test
	public void update() {
		int size = providerDAO.getAll().size();
		
		Provider provider = new Provider();
		provider.setName("name");
		provider.setEndpoint("http://...");
		
		boolean saved = providerDAO.save(provider);
		assertTrue(saved);
		
		Provider providerFromDatabase = providerDAO.getAll().get(size);
		int id = providerFromDatabase.getId();
		assertEquals("name", providerFromDatabase.getName());
		assertEquals("http://...", providerFromDatabase.getEndpoint());
			
		providerFromDatabase.setName("name2");
		providerFromDatabase.setEndpoint("http://...2");

		boolean updated = providerDAO.update(providerFromDatabase);
		assertTrue(updated);
		
		providerFromDatabase = providerDAO.getById(id);
		assertEquals("name2", providerFromDatabase.getName());
		assertEquals("http://...2", providerFromDatabase.getEndpoint());
	}
}
