package eu.ascetic.paas.applicationmanager.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AgreementTest {

	@Test
	public void pojoTest() {
		Agreement agreement = new Agreement();
		agreement.setId(1);
		agreement.setDeploymentId("deployment-id");
		agreement.setHref("href");
		agreement.setPrice("222");
		agreement.setSlaAgreement("sla-agreement");
	
		assertEquals(1, agreement.getId());
		assertEquals("deployment-id", agreement.getDeploymentId());
		assertEquals("href", agreement.getHref());
		assertEquals("222", agreement.getPrice());
		assertEquals("sla-agreement", agreement.getSlaAgreement());
	}
	
	@Test
	public void addLinkTest() {
		Agreement agreement = new Agreement();
		
		assertEquals(null, agreement.getLinks());
		
		Link link = new Link();
		agreement.addLink(link);
		
		assertEquals(1, agreement.getLinks().size());
		assertEquals(link, agreement.getLinks().get(0));
	}
}
