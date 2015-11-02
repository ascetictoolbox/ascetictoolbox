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

public class PowerMeasurementTest {

	@Test
	public void testPojo() {
		PowerMeasurement powerMeasurement = new PowerMeasurement();
		powerMeasurement.setHref("href");
		powerMeasurement.setValue(22d);
		powerMeasurement.setDescription("Energy Estimation for VM");
		List<Link> links = new ArrayList<Link>();
		powerMeasurement.setLinks(links);
		
		assertEquals("href", powerMeasurement.getHref());
		assertEquals(22d, powerMeasurement.getValue(), 0.00001);
		assertEquals(links, powerMeasurement.getLinks());
		assertEquals("Energy Estimation for VM", powerMeasurement.getDescription());
	}
	
	@Test
	public void addLinkTest() {
		PowerMeasurement powerMeasurement = new PowerMeasurement();
		
		assertEquals(null, powerMeasurement.getLinks());
		
		Link link = new Link();
		powerMeasurement.addLink(link);
		assertEquals(link, powerMeasurement.getLinks().get(0));
	}
}
