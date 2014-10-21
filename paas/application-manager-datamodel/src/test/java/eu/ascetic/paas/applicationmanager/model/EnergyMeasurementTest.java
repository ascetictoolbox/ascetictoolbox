package eu.ascetic.paas.applicationmanager.model;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * 
 * Copyright 2014 ATOS SPAIN S.A. 
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author: David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.garciaperez@atos.net 
 */

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
