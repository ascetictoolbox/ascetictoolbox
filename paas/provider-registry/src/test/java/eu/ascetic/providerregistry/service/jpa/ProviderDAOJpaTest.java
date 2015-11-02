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
/**
 * 
 * Copyright 2014 ATOS SPAIN S.A. 
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * @email david.garciaperez@atos.net 
 * 
 * Unit test to verify all the methods and functions of the ProviderDAOJpa.class
 */
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
		provider.setVmmUrl("http://...");
		provider.setSlamUrl("http2...");
		
		boolean saved = providerDAO.save(provider);
		assertTrue(saved);
		
		List<Provider> providers = providerDAO.getAll();
		size = size + 1;
		assertEquals(size, providers.size());
		
		Provider providerFromDatabase = providers.get(size-1);
		assertEquals("name", providerFromDatabase.getName());
		assertEquals("http://...", providerFromDatabase.getVmmUrl());
	}
	
	@Test
	public void getById() {
		int size = providerDAO.getAll().size();
		
		Provider provider = new Provider();
		provider.setName("name");
		provider.setVmmUrl("http://...");
		provider.setSlamUrl("http2...");
		
		boolean saved = providerDAO.save(provider);
		assertTrue(saved);
			
		Provider providerFromDatabase = providerDAO.getAll().get(size);
		int id = providerFromDatabase.getId();
		providerFromDatabase = providerDAO.getById(id);
		assertEquals("name", providerFromDatabase.getName());
		assertEquals("http://...", providerFromDatabase.getVmmUrl());
		
		Provider nullProvider = providerDAO.getById(30000);
		assertEquals(null, nullProvider);
	}
	
	@Test
	public void delete() {
		int size = providerDAO.getAll().size();
		
		Provider provider = new Provider();
		provider.setName("name");
		provider.setVmmUrl("http://...");
		provider.setSlamUrl("http2...");
		
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
		provider.setVmmUrl("http://...");
		provider.setSlamUrl("http2...");
		
		boolean saved = providerDAO.save(provider);
		assertTrue(saved);
		
		Provider providerFromDatabase = providerDAO.getAll().get(size);
		int id = providerFromDatabase.getId();
		assertEquals("name", providerFromDatabase.getName());
		assertEquals("http://...", providerFromDatabase.getVmmUrl());
			
		providerFromDatabase.setName("name2");
		providerFromDatabase.setVmmUrl("http://...2");

		boolean updated = providerDAO.update(providerFromDatabase);
		assertTrue(updated);
		
		providerFromDatabase = providerDAO.getById(id);
		assertEquals("name2", providerFromDatabase.getName());
		assertEquals("http://...2", providerFromDatabase.getVmmUrl());
	}
}
