package eu.ascetic.paas.applicationmanager.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ApplicationSampleTest {

	@Test
	public void pojo() {
		ApplicationSample applicationSample = new ApplicationSample();
		applicationSample.setAppid("appid");
		applicationSample.setcValue(1.0);
		applicationSample.seteValue(2.0);
		applicationSample.setOrderID(5);
		applicationSample.setpValue(3.0);
		applicationSample.setTime(2l);
		applicationSample.setVmid("vmid");
		
		assertEquals("appid", applicationSample.getAppid());
		assertEquals(1.0, applicationSample.getcValue(), 0.0001);
		assertEquals(2.0, applicationSample.geteValue(), 0.0001);
		assertEquals(5, applicationSample.getOrderID());
		assertEquals(3.0, applicationSample.getpValue(), 0.0001);
		assertEquals(2l, applicationSample.getTime());
		assertEquals("vmid", applicationSample.getVmid());
	}
}
