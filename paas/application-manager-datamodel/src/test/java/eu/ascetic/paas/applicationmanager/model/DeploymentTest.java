package eu.ascetic.paas.applicationmanager.model;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * Unit test to verify the correct functioning of this POJO
 * @author David Garcia Perez - Atos
 *
 */
public class DeploymentTest {

	@Test
	public void pojoTest() {
		Deployment deployment = new Deployment();
		deployment.setId(11);
		deployment.setHref("href");
		deployment.setPrice("provider-id");
		deployment.setStatus("STATUS");
		List<Link> links = new ArrayList<Link>();
		deployment.setLinks(links);
		List<VM> vms = new ArrayList<VM>();
		deployment.setVms(vms);
		
		assertEquals(11, deployment.getId());
		assertEquals("href", deployment.getHref());
		assertEquals("provider-id", deployment.getPrice());
		assertEquals("STATUS", deployment.getStatus());
		assertEquals(links, deployment.getLinks());
		assertEquals(vms, deployment.getVms());
	}
	
	@Test
	public void addLinkTest() {
		Deployment deployment = new Deployment();
		
		assertEquals(null, deployment.getLinks());
		
		Link link = new Link();
		deployment.addLink(link);
		assertEquals(link, deployment.getLinks().get(0));
	}
	
	@Test
	public void addVMTest() {
		Deployment deployment = new Deployment();
		
		assertEquals(null, deployment.getVms());
		
		VM vm = new VM();
		deployment.addVM(vm);
		
		assertEquals(1, deployment.getVms().size());
		assertEquals(vm, deployment.getVms().get(0));
	}
}
