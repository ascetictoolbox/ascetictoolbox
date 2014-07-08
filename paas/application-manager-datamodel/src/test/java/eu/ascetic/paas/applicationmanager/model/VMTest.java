package eu.ascetic.paas.applicationmanager.model;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class VMTest {

	@Test
	public void pojoTest() {
		VM vm = new VM();
		vm.setId(1);
		vm.setHref("href");
		vm.setOvfId("ovfId");
		vm.setProviderId("provider-id");
		vm.setProviderVmId("provider-vm-id");
		vm.setStatus("XXX");
		vm.setIp("172.0.0.1");
		vm.setSlaAgreement("slaAggrementId");
		List<Link> links = new ArrayList<Link>();
		vm.setLinks(links);
		
		assertEquals(1, vm.getId());
		assertEquals("href", vm.getHref());
		assertEquals("ovfId", vm.getOvfId());
		assertEquals("provider-id", vm.getProviderId());
		assertEquals("provider-vm-id", vm.getProviderVmId());
		assertEquals("XXX", vm.getStatus());
		assertEquals("172.0.0.1", vm.getIp());
		assertEquals("slaAggrementId", vm.getSlaAgreement());
		assertEquals(links, vm.getLinks());
	}
	
	@Test
	public void addLinkTest() {
		VM vm = new VM();
		
		assertEquals(null, vm.getLinks());
		
		Link link = new Link();
		vm.addLink(link);
		assertEquals(link, vm.getLinks().get(0));
	}
}
