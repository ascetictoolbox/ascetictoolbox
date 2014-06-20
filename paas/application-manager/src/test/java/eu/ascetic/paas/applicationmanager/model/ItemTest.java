package eu.ascetic.paas.applicationmanager.model;

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
		
		List<Application> applications = new ArrayList<Application>();
		items.setApplications(applications);
		
		assertEquals(1, items.getOffset());
		assertEquals(2, items.getTotal());
		assertEquals(applications, items.getApplications());
	}
	
	@Test
	public void addApplication() {
		Items items = new Items();
		assertEquals(null, items.getApplications());
		
		Application application = new Application();
		items.addApplication(application);
		
		assertEquals(1, items.getApplications().size());
		assertEquals(application, items.getApplications().get(0));
	}
}
