package eu.ascetic.paas.applicationmanager.dao.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
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
import eu.ascetic.paas.applicationmanager.dao.DeploymentDAO;
import eu.ascetic.paas.applicationmanager.model.Agreement;
import eu.ascetic.paas.applicationmanager.model.Deployment;

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
	@Autowired
	protected DeploymentDAO deploymentDAO;
	
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
	
	@Test
	public void getAcceptedAgreement() {
		// Normal case we have a deployment with a series of agreements, one accepted
		Deployment deployment1 = new Deployment();
		deployment1.setStatus("XXXX1");
		
		Agreement agreement1 = new Agreement();
		agreement1.setPrice("111");
		agreement1.setProviderId("provider-id");
		agreement1.setSlaAgreement("sla-agreement");
		agreement1.setSlaAgreementId("sla-agreement-id");
		agreement1.setDeployment(deployment1);
		agreement1.setAccepted(false);
		deployment1.addAgreement(agreement1);
		
		Agreement agreement2 = new Agreement();
		agreement2.setPrice("121");
		agreement2.setProviderId("provider-id2");
		agreement2.setSlaAgreement("sla-agreement2");
		agreement2.setSlaAgreementId("sla-agreement-id2");
		agreement2.setDeployment(deployment1);
		agreement2.setAccepted(true);
		deployment1.addAgreement(agreement2);
		
		Agreement agreement3 = new Agreement();
		agreement3.setPrice("131");
		agreement3.setProviderId("provider-id3");
		agreement3.setSlaAgreement("sla-agreement3");
		agreement3.setSlaAgreementId("sla-agreement-id3");
		agreement3.setDeployment(deployment1);
		agreement3.setAccepted(false);
		deployment1.addAgreement(agreement3);
		
		Agreement agreement4 = new Agreement();
		agreement4.setPrice("431");
		agreement4.setProviderId("provider-id4");
		agreement4.setSlaAgreement("sla-agreement4");
		agreement4.setSlaAgreementId("sla-agreement-id4");
		agreement4.setAccepted(false);
		
		deploymentDAO.save(deployment1);
		agreementDAO.save(agreement1);
		agreementDAO.save(agreement2);
		agreementDAO.save(agreement3);
		agreementDAO.save(agreement4);
		
		Agreement agreementFromDB = agreementDAO.getAcceptedAgreement(deployment1);
		assertEquals(agreementFromDB, agreement2);
		
		// We check in the case the deployment has no agreements that we get null
		Deployment deployment2 = new Deployment();
		deployment2.setStatus("STATUS2");
		
		deploymentDAO.save(deployment2);
		
		agreementFromDB = agreementDAO.getAcceptedAgreement(deployment2);
		assertNull(agreementFromDB);
		
		Agreement agreement5 = new Agreement();
		agreement5.setPrice("531");
		agreement5.setProviderId("provider-id5");
		agreement5.setSlaAgreement("sla-agreement5");
		agreement5.setSlaAgreementId("sla-agreement-id5");
		agreement5.setAccepted(false);
		agreement5.setDeployment(deployment2);
		deployment2.addAgreement(agreement5);
		
		deployment2.addAgreement(agreement4);
		agreement4.setDeployment(deployment2);
		
		deploymentDAO.update(deployment2);
		agreementDAO.update(agreement4);
		agreementDAO.save(agreement5);
		
		agreementFromDB = agreementDAO.getAcceptedAgreement(deployment2);
		assertNull(agreementFromDB);
	}
}
