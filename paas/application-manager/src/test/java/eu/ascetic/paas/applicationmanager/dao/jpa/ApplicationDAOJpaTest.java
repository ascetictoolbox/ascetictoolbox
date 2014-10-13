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

import eu.ascetic.paas.applicationmanager.dao.ApplicationDAO;
import eu.ascetic.paas.applicationmanager.dao.DeploymentDAO;
import eu.ascetic.paas.applicationmanager.dao.ImageDAO;
import eu.ascetic.paas.applicationmanager.model.Application;
import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.paas.applicationmanager.model.Image;

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
 * @author: David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * @email david.garciaperez@atos.net 
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/application-manager-db-JPA-test-context.xml")
public class ApplicationDAOJpaTest extends AbstractTransactionalJUnit4SpringContextTests {
	@Autowired
	protected ApplicationDAO applicationDAO;
	@Autowired
	protected DeploymentDAO deploymentDAO;
	@Autowired
	protected ImageDAO imageDAO;
	
	@Test
	public void notNull() {
		if(applicationDAO == null || deploymentDAO == null || imageDAO == null) fail();
	}
	
	@Test
	public void saveGetAll() {
		int size = applicationDAO.getAll().size();
		
		Application application = new Application();
		application.setName("name");
		
		boolean saved = applicationDAO.save(application);
		assertTrue(saved);
		
		List<Application> applications = applicationDAO.getAll();
		size = size + 1;
		assertEquals(size, applications.size());
		
		Application applicationFromDatabase = applications.get(size-1);
		assertEquals("name", applicationFromDatabase.getName());
	}
	
	@Test
	public void getById() {
		int size = applicationDAO.getAll().size();
		
		Application application = new Application();
		application.setName("name");
		
		boolean saved = applicationDAO.save(application);
		assertTrue(saved);
			
		Application applicationFromDatabase = applicationDAO.getAll().get(size);
		int id = applicationFromDatabase.getId();
		applicationFromDatabase = applicationDAO.getById(id);
		assertEquals("name", applicationFromDatabase.getName());
		
		Application nullApplication = applicationDAO.getById(30000);
		assertEquals(null, nullApplication);
	}
	
	@Test
	public void delete() {
		int size = applicationDAO.getAll().size();
		
		Application application = new Application();
		application.setName("name");
		
		boolean saved = applicationDAO.save(application);
		assertTrue(saved);
			
		Application applicationFromDatabase = applicationDAO.getAll().get(size);
		int id = applicationFromDatabase.getId();
		
		boolean deleted = applicationDAO.delete(applicationFromDatabase);
		assertTrue(deleted);
		
		deleted = applicationDAO.delete(applicationFromDatabase);
		assertTrue(!deleted);
		
		applicationFromDatabase = applicationDAO.getById(id);
		assertEquals(null, applicationFromDatabase);
	}
	
	@Test
	public void updateTest() {
		int size = applicationDAO.getAll().size();
		
		Application application = new Application();
		application.setName("name");
		
		boolean saved = applicationDAO.save(application);
		assertTrue(saved);
		
		Application applicationFromDatabase = applicationDAO.getAll().get(size);
		int id = applicationFromDatabase.getId();
		assertEquals("name", applicationFromDatabase.getName());
		
		applicationFromDatabase.setName("name2");

		boolean updated = applicationDAO.update(applicationFromDatabase);
		assertTrue(updated);
		
		applicationFromDatabase = applicationDAO.getById(id);
		assertEquals("name2", applicationFromDatabase.getName());
	}
	
	@Test
	public void getByNameTest() {
		Application application1 = new Application();
		application1.setName("name1");
		Application application2 = new Application();
		application2.setName("name2");
		
		boolean saved = applicationDAO.save(application1);
		assertTrue(saved);
		saved = applicationDAO.save(application2);
		assertTrue(saved);
		
		Application applicationFromDatabase = applicationDAO.getByName("name1");
		assertEquals("name1", applicationFromDatabase.getName());
		
		applicationFromDatabase = applicationDAO.getByName("xxx");
		assertEquals(null, applicationFromDatabase);
	}

		
	@Test
	public void cascadeTest() {
		int size = applicationDAO.getAll().size();
		
		Application application = new Application();
		application.setName("name");

		Deployment deployment1 = new Deployment();
		deployment1.setStatus("RUNNING");
		deployment1.setPrice("expensive");
		
		Deployment deployment2 = new Deployment();
		deployment2.setStatus("RUNNING");
		deployment2.setPrice("expensive");
		
		application.addDeployment(deployment1);
		application.addDeployment(deployment2);

		boolean saved = applicationDAO.save(application);
		assertTrue(saved);
		
		Application applicationFromDatabase = applicationDAO.getAll().get(size);
		int id = applicationFromDatabase.getId();
		applicationFromDatabase = applicationDAO.getById(id);
		assertEquals("name", applicationFromDatabase.getName());
		
		assertEquals(2, application.getDeployments().size());
		assertEquals("RUNNING", application.getDeployments().get(1).getStatus());
		
		boolean deleted = applicationDAO.delete(applicationFromDatabase);
		assertTrue(deleted);

		
		List<Deployment> deployments = deploymentDAO.getAll();
		assertEquals(0, deployments.size());
	}
	
	@Test
	public void imageCascadeTest() {
		int size = applicationDAO.getAll().size();
		int sizeImages = imageDAO.getAll().size();
		
		Application application = new Application();
		application.setName("name");
		
		Image image1 = new Image();
		image1.setOvfId("ovf-id1");
		Image image2 = new Image();
		image2.setOvfId("ovf-id2");
		
		application.addImage(image1);
		application.addImage(image2);
		
		boolean saved = applicationDAO.save(application);
		assertTrue(saved);
		
		int sizeImagesNew = imageDAO.getAll().size();
		assertEquals(sizeImages + 2, sizeImagesNew);
		
		Application applicationFromDatabase = applicationDAO.getAll().get(size);
		int id = applicationFromDatabase.getId();
		applicationFromDatabase = applicationDAO.getById(id);
		assertEquals("name", applicationFromDatabase.getName());
		
		assertEquals(2, applicationFromDatabase.getImages().size());
		assertEquals("ovf-id1", applicationFromDatabase.getImages().get(0).getOvfId());
		
		boolean deleted = applicationDAO.delete(applicationFromDatabase);
		assertTrue(deleted);
		
		sizeImagesNew = imageDAO.getAll().size();
		assertEquals(sizeImages, sizeImagesNew);
	}
}

