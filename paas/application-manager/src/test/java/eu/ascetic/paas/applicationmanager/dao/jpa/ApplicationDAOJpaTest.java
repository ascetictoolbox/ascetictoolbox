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
	public void update() {
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
}

