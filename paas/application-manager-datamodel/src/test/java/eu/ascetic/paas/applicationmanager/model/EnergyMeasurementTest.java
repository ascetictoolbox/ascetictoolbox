package eu.ascetic.paas.applicationmanager.model;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class EnergyMeasurementTest {

	@Test
	public void testPojo() {
		EnergyMeasurement energyMeasurement = new EnergyMeasurement();
		energyMeasurement.setHref("href");
		energyMeasurement.setValue(22d);
		energyMeasurement.setDescription("Energy Estimation for VM");
		List<Link> links = new ArrayList<Link>();
		energyMeasurement.setLinks(links);
		
		assertEquals("href", energyMeasurement.getHref());
		assertEquals(22d, energyMeasurement.getValue(), 0.00001);
		assertEquals(links, energyMeasurement.getLinks());
		assertEquals("Energy Estimation for VM", energyMeasurement.getDescription());
	}
	
	@Test
	public void addLinkTest() {
		EnergyMeasurement energyMeasurement = new EnergyMeasurement();
		
		assertEquals(null, energyMeasurement.getLinks());
		
		Link link = new Link();
		energyMeasurement.addLink(link);
		assertEquals(link, energyMeasurement.getLinks().get(0));
	}
}
