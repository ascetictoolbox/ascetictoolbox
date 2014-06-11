package eu.ascetic.monitoring.api.datamodel;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * The Class HostTest.
 */
public class HostTest {
	
	/**
	 * Pojo test.
	 */
	@Test
	public void pojoTest() {
		Host host = new Host();
		host.setHost("myHost");
		host.setAvailable("1");
		host.setHostid("1");
		
		assertEquals("myHost", host.getHost());
		assertEquals("1", host.getAvailable());
		assertEquals("1", host.getHostid());
	}

	
}
