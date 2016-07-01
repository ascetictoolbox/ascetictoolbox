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

import eu.ascetic.paas.applicationmanager.dao.ApplicationDAO;
import eu.ascetic.paas.applicationmanager.dao.DeploymentDAO;
import eu.ascetic.paas.applicationmanager.dao.VMDAO;
import eu.ascetic.paas.applicationmanager.model.Application;
import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.paas.applicationmanager.model.Image;
import eu.ascetic.paas.applicationmanager.model.VM;

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
 * e-mail david.garciaperez@atos.net 
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/application-manager-db-JPA-test-context.xml")
public class DeploymentDAOJpaTest extends AbstractTransactionalJUnit4SpringContextTests {
	@Autowired
	protected DeploymentDAO deploymentDAO;
	@Autowired
	protected ApplicationDAO applicationDAO;
	@Autowired 
	protected VMDAO vmDAO;
	
	@Test
	public void notNull() {
		if(deploymentDAO == null || vmDAO == null || applicationDAO == null) fail();
	}
	
	@Test
	public void saveGetAll() {
		int size = deploymentDAO.getAll().size();
		
		Deployment deployment = new Deployment();
		deployment.setStatus("RUNNIG");
		deployment.setPrice("expensive");
		//deployment.setSlaAgreement("sla agreement");
		
		boolean saved = deploymentDAO.save(deployment);
		assertTrue(saved);
		
		List<Deployment> deployments = deploymentDAO.getAll();
		size = size + 1;
		assertEquals(size, deployments.size());
		
		Deployment deploymentFromDatabase = deployments.get(size-1);
		assertEquals("RUNNIG", deploymentFromDatabase.getStatus());
		assertEquals("expensive", deploymentFromDatabase.getPrice());
		//assertEquals("sla agreement", deploymentFromDatabase.getSlaAgreement());
	}
	
	@Test
	public void getById() {
		int size = deploymentDAO.getAll().size();
		
		Deployment deployment = new Deployment();
		deployment.setStatus("RUNNIG");
		deployment.setPrice("expensive");
		
		boolean saved = deploymentDAO.save(deployment);
		assertTrue(saved);
			
		Deployment deploymentFromDatabase = deploymentDAO.getAll().get(size);
		int id = deploymentFromDatabase.getId();
		deploymentFromDatabase = deploymentDAO.getById(id);
		assertEquals("RUNNIG", deploymentFromDatabase.getStatus());
		assertEquals("expensive", deploymentFromDatabase.getPrice());
		
		Deployment nullDeployment = deploymentDAO.getById(30000);
		assertEquals(null, nullDeployment);
	}
	
	@Test
	public void delete() {
		int size = deploymentDAO.getAll().size();
		
		Deployment deployment = new Deployment();
		deployment.setStatus("RUNNIG");
		deployment.setPrice("expensive");
		
		boolean saved = deploymentDAO.save(deployment);
		assertTrue(saved);
			
		Deployment deploymentFromDatabase = deploymentDAO.getAll().get(size);
		int id = deploymentFromDatabase.getId();
		
		boolean deleted = deploymentDAO.delete(deploymentFromDatabase);
		assertTrue(deleted);
		
		deleted = deploymentDAO.delete(deploymentFromDatabase);
		assertTrue(!deleted);
		
		deploymentFromDatabase = deploymentDAO.getById(id);
		assertEquals(null, deploymentFromDatabase);
	}
	
	@Test
	public void update() {
		int size = deploymentDAO.getAll().size();
		
		Deployment deployment = new Deployment();
		deployment.setStatus("RUNNIG");
		deployment.setPrice("expensive");
		
		boolean saved = deploymentDAO.save(deployment);
		assertTrue(saved);
		
		Deployment deploymentFromDatabase = deploymentDAO.getAll().get(size);
		int id = deploymentFromDatabase.getId();
		assertEquals("RUNNIG", deploymentFromDatabase.getStatus());
		
		deploymentFromDatabase.setStatus("STOPPED");

		boolean updated = deploymentDAO.update(deploymentFromDatabase);
		assertTrue(updated);
		
		deploymentFromDatabase = deploymentDAO.getById(id);
		assertEquals("STOPPED", deploymentFromDatabase.getStatus());
	}
	
	@Test
	public void cascade() {
		int size = deploymentDAO.getAll().size();
		
		Deployment deployment = new Deployment();
		deployment.setStatus("RUNNING");
		deployment.setPrice("expensive");
		
		VM vm1 = new VM();
		vm1.setIp("127.0.0.1");
		vm1.setOvfId("ovf-id");
		vm1.setProviderId("provider-id");
		vm1.setProviderVmId("provider-vm-id");
		vm1.setSlaAgreement("sla-agreement");
		vm1.setStatus("RUNNING");
		
		VM vm2 = new VM();
		vm2.setIp("127.0.0.1");
		vm2.setOvfId("ovf-id");
		vm2.setProviderId("provider-id");
		vm2.setProviderVmId("provider-vm-id");
		vm2.setSlaAgreement("sla-agreement");
		vm2.setStatus("RUNNING");
		
		deployment.addVM(vm1);
		deployment.addVM(vm2);

		boolean saved = deploymentDAO.save(deployment);
		assertTrue(saved);
		
		List<VM> vms = vmDAO.getAll();
		assertEquals(2, vms.size());
		
		Deployment deploymentFromDatabase = deploymentDAO.getAll().get(size);
		assertEquals("RUNNING", deploymentFromDatabase.getStatus());
		
		assertEquals(2, deployment.getVms().size());
		assertEquals("RUNNING", deployment.getVms().get(1).getStatus());
		
		boolean deleted = deploymentDAO.delete(deploymentFromDatabase);
		assertTrue(deleted);

		
		vms = vmDAO.getAll();
		assertEquals(0, vms.size());
	}
	
	@Test
	public void getDeploymentsForApplicationWithStatus() {
		Application application = new Application();
		application.setName("name");

		Deployment deployment1 = new Deployment();
		deployment1.setStatus("RUNNING");
		deployment1.setPrice("expensive");
				
		Deployment deployment2 = new Deployment();
		deployment2.setStatus("DELETED");
		deployment2.setPrice("expensive");
		
		Deployment deployment3 = new Deployment();
		deployment3.setStatus("RUNNING");
		deployment3.setPrice("expensive");
		
		application.addDeployment(deployment1);
		application.addDeployment(deployment2);
		application.addDeployment(deployment3);
		
		boolean saved = applicationDAO.save(application);
		assertTrue(saved);
		
		List<Deployment> deploymentsRUNNING = deploymentDAO.getDeploymentsForApplicationWithStatus(application, "RUNNING");
		assertEquals(2, deploymentsRUNNING.size());
		
		List<Deployment> deploymentsDELETED = deploymentDAO.getDeploymentsForApplicationWithStatus(application, "DELETED");
		assertEquals(1, deploymentsDELETED.size());
		
		List<Deployment> deploymentsXXX = deploymentDAO.getDeploymentsForApplicationWithStatus(application, "XXX");
		assertEquals(0, deploymentsXXX.size());
	}
	
	@Test
	public void getDeplomentByDeploymentName() {

		Deployment deployment1 = new Deployment();
		deployment1.setStatus("RUNNING");
		deployment1.setDeploymentName("aaaa");
		deployment1.setPrice("expensive");
				
		Deployment deployment2 = new Deployment();
		deployment2.setStatus("DELETED");
		deployment2.setDeploymentName("bbb");
		deployment2.setPrice("expensive");
		
		Deployment deployment3 = new Deployment();
		deployment3.setStatus("RUNNING");
		deployment3.setPrice("expensive");
		
		deploymentDAO.save(deployment1);
		deploymentDAO.save(deployment2);
		deploymentDAO.save(deployment3);
		
		Deployment deploymentFromDB = deploymentDAO.getDeployment("aaaa");
		assertEquals("RUNNING", deploymentFromDB.getStatus());
		deploymentFromDB = deploymentDAO.getDeployment("bbb");
		assertEquals("DELETED", deploymentFromDB.getStatus());
		deploymentFromDB = deploymentDAO.getDeployment("ccc");
		assertNull(deploymentFromDB);
	}
}

