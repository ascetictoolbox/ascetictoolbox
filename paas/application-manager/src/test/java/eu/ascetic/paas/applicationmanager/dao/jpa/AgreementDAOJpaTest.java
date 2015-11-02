package eu.ascetic.paas.applicationmanager.dao.jpa;

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

import eu.ascetic.paas.applicationmanager.dao.AgreementDAO;
import eu.ascetic.paas.applicationmanager.model.Agreement;

/**
 * 
 * Copyright 2015 ATOS SPAIN S.A. 
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
 * @author David Garcia Perez. Ato#s Research and Innovation, Atos SPAIN SA
 * e-mail david.garciaperez@atos.net 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/application-manager-db-JPA-test-context.xml")
public class AgreementDAOJpaTest extends AbstractTransactionalJUnit4SpringContextTests {
	@Autowired
	protected AgreementDAO agreementDAO;
	
	@Test
	public void notNull() {
		if(agreementDAO == null) fail();
	}
	
	@Test
	public void saveGetAll() {
		int size = agreementDAO.getAll().size();
		
		Agreement agreement = new Agreement();
		agreement.setPrice("111");
		agreement.setProviderId("provider-id");
		agreement.setSlaAgreement("sla-agreement");
		agreement.setSlaAgreementId("sla-agreement-id");
		
		boolean saved = agreementDAO.save(agreement);
		assertTrue(saved);
		
		List<Agreement> agreements = agreementDAO.getAll();
		size = size + 1;
		assertEquals(size, agreements.size());
		
		Agreement agreementFromDatabase = agreements.get(size-1);
		assertEquals("111", agreementFromDatabase.getPrice());
		assertEquals("provider-id", agreementFromDatabase.getProviderId());
		assertEquals("sla-agreement", agreementFromDatabase.getSlaAgreement());
		assertEquals("sla-agreement-id", agreementFromDatabase.getSlaAgreementId());
	}
	
	@Test
	public void getById() {
		int size = agreementDAO.getAll().size();
		
		Agreement agreement = new Agreement();
		agreement.setPrice("111");
		agreement.setProviderId("provider-id");
		agreement.setSlaAgreement("sla-agreement");
		agreement.setSlaAgreementId("sla-agreement-id");
		
		boolean saved = agreementDAO.save(agreement);
		assertTrue(saved);
			
		Agreement agreementFromDatabase = agreementDAO.getAll().get(size);
		int id = agreementFromDatabase.getId();
		agreementFromDatabase = agreementDAO.getById(id);
		assertEquals("111", agreementFromDatabase.getPrice());
		assertEquals("provider-id", agreementFromDatabase.getProviderId());
		assertEquals("sla-agreement", agreementFromDatabase.getSlaAgreement());
		assertEquals("sla-agreement-id", agreementFromDatabase.getSlaAgreementId());
		
		Agreement nullAgreement = agreementDAO.getById(30000);
		assertEquals(null, nullAgreement);
	}
	
	@Test
	public void delete() {
		int size = agreementDAO.getAll().size();
		
		Agreement agreement = new Agreement();
		agreement.setPrice("111");
		agreement.setProviderId("provider-id");
		agreement.setSlaAgreement("sla-agreement");
		agreement.setSlaAgreementId("sla-agreement-id");
		
		boolean saved = agreementDAO.save(agreement);
		assertTrue(saved);
		
		Agreement agreementFromDatabase = agreementDAO.getAll().get(size);
		int id = agreementFromDatabase.getId();
		
		boolean deleted = agreementDAO.delete(agreementFromDatabase);
		assertTrue(deleted);
		
		deleted = agreementDAO.delete(agreementFromDatabase);
		assertTrue(!deleted);
		
		agreementFromDatabase = agreementDAO.getById(id);
		assertEquals(null, agreementFromDatabase);
	}
	
	@Test
	public void update() {
		int size = agreementDAO.getAll().size();
		
		Agreement agreement = new Agreement();
		agreement.setPrice("111");
		agreement.setProviderId("provider-id");
		agreement.setSlaAgreement("sla-agreement");
		agreement.setSlaAgreementId("sla-agreement-id");
		
		boolean saved = agreementDAO.save(agreement);
		assertTrue(saved);
		
		Agreement agreementFromDatabase = agreementDAO.getAll().get(size);
		int id = agreementFromDatabase.getId();
		assertEquals("sla-agreement-id", agreementFromDatabase.getSlaAgreementId());
		
		agreementFromDatabase.setSlaAgreementId("sla-agre");

		boolean updated = agreementDAO.update(agreementFromDatabase);
		assertTrue(updated);
		
		agreementFromDatabase = agreementDAO.getById(id);
		assertEquals("sla-agre", agreementFromDatabase.getSlaAgreementId());
	}
}
