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
import eu.ascetic.paas.applicationmanager.model.Application;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/application-manager-db-JPA-test-context.xml")
public class ApplicationDAOJpaTest extends AbstractTransactionalJUnit4SpringContextTests {
	@Autowired
	protected ApplicationDAO applicationDAO;
	
	@Test
	public void notNull() {
		if(applicationDAO == null) fail();
	}
	
	@Test
	public void saveGetAll() {
		int size = applicationDAO.getAll().size();
		
		Application application = new Application();
		application.setDeploymentPlanId("deployment-plan");
		application.setStatus("RUNNING");
		
		boolean saved = applicationDAO.save(application);
		assertTrue(saved);
		
		List<Application> applications = applicationDAO.getAll();
		size = size + 1;
		assertEquals(size, applications.size());
		
		Application applicationFromDatabase = applications.get(size-1);
		assertEquals("RUNNING", applicationFromDatabase.getStatus());
		assertEquals("deployment-plan", applicationFromDatabase.getDeploymentPlanId());
	}
	
	@Test
	public void getById() {
		int size = applicationDAO.getAll().size();
		
		Application application = new Application();
		application.setDeploymentPlanId("deployment-plan");
		application.setStatus("RUNNING");
		
		boolean saved = applicationDAO.save(application);
		assertTrue(saved);
			
		Application applicationFromDatabase = applicationDAO.getAll().get(size);
		int id = applicationFromDatabase.getId();
		applicationFromDatabase = applicationDAO.getById(id);
		assertEquals("RUNNING", applicationFromDatabase.getStatus());
		assertEquals("deployment-plan", applicationFromDatabase.getDeploymentPlanId());
		
		Application nullApplication = applicationDAO.getById(30000);
		assertEquals(null, nullApplication);
	}
	
	@Test
	public void delete() {
		int size = applicationDAO.getAll().size();
		
		Application application = new Application();
		application.setDeploymentPlanId("deployment-plan");
		application.setStatus("RUNNING");
		
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
	public void update() {
		int size = applicationDAO.getAll().size();
		
		Application application = new Application();
		application.setDeploymentPlanId("deployment-plan");
		application.setStatus("RUNNING");
		
		boolean saved = applicationDAO.save(application);
		assertTrue(saved);
		
		Application applicationFromDatabase = applicationDAO.getAll().get(size);
		int id = applicationFromDatabase.getId();
		assertEquals("RUNNING", applicationFromDatabase.getStatus());
		assertEquals("deployment-plan", applicationFromDatabase.getDeploymentPlanId());
			
		applicationFromDatabase.setDeploymentPlanId("deployment-plan2");
		applicationFromDatabase.setStatus("STOPPED");

		boolean updated = applicationDAO.update(applicationFromDatabase);
		assertTrue(updated);
		
		applicationFromDatabase = applicationDAO.getById(id);
		assertEquals("STOPPED", applicationFromDatabase.getStatus());
		assertEquals("deployment-plan2", applicationFromDatabase.getDeploymentPlanId());
	}
}

