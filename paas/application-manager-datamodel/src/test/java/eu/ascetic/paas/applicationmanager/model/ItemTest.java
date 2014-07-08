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
	public void pojoTest() {
		Items items = new Items();
		items.setOffset(1);
		items.setTotal(2);
		
		List<Application> applications = new ArrayList<Application>();
		items.setApplications(applications);
		
		List<Deployment> deployments = new ArrayList<Deployment>();
		items.setDeployments(deployments);
		
		List<VM> vms = new ArrayList<VM>();
		items.setVms(vms);
		
		List<Agreement> agreements = new ArrayList<Agreement>();
		items.setAgreements(agreements);
		
		assertEquals(1, items.getOffset());
		assertEquals(2, items.getTotal());
		assertEquals(applications, items.getApplications());
		assertEquals(deployments, items.getDeployments());
		assertEquals(vms, items.getVms());
		assertEquals(agreements, items.getAgreements());
	}
	
	@Test
	public void addApplicationTest() {
		Items items = new Items();
		assertEquals(null, items.getApplications());
		
		Application application = new Application();
		items.addApplication(application);
		
		assertEquals(1, items.getApplications().size());
		assertEquals(application, items.getApplications().get(0));
	}
	
	@Test
	public void addDeploymentTest() {
		Items items = new Items();
		assertEquals(null, items.getDeployments());
		
		Deployment deployment = new Deployment();
		items.addDeployment(deployment);
		
		assertEquals(1, items.getDeployments().size());
		assertEquals(deployment, items.getDeployments().get(0));
	}
	
	@Test
	public void addVmsTest() {
		Items items = new Items();
		assertEquals(null, items.getVms());
		
		VM vm = new VM();
		items.addVm(vm);
		
		assertEquals(1, items.getVms().size());
		assertEquals(vm, items.getVms().get(0));
	}
	
	@Test
	public void addAgreementTest() {
		Items items = new Items();
		
		assertEquals(null, items.getAgreements());
		
		Agreement agreement = new Agreement();
		items.addAgreement(agreement);
		
		assertEquals(1, items.getAgreements().size());
		assertEquals(agreement, items.getAgreements().get(0));
	}
}
