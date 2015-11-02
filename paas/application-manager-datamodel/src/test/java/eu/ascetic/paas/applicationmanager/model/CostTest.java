package eu.ascetic.paas.applicationmanager.model;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * 
 * Copyright 2015 ATOS SPAIN S.A. 
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
 * 
 * Basic XML representation for any Cost
 * 
 */
public class CostTest {

	@Test
	public void pojo() {
		Cost cost = new Cost();
		cost.setCharges(1.0d);
		cost.setChargesDescription("a");
		cost.setEnergyDescription("b");
		cost.setEnergyValue(2.0d);
		cost.setPowerDescription("c");
		cost.setPowerValue(3.0d);
		List<Link> links = new ArrayList<Link>();
		cost.setLinks(links);
		
		assertEquals(1.0, cost.getCharges().doubleValue(), 0.001);	
		assertEquals("a", cost.getChargesDescription());
		assertEquals("b", cost.getEnergyDescription());
		assertEquals(2.0, cost.getEnergyValue().doubleValue(), 0.001);
		assertEquals("c", cost.getPowerDescription());
		assertEquals(3.0d, cost.getPowerValue().doubleValue(), 0.001);
		assertEquals(links, cost.getLinks());
	}
}
