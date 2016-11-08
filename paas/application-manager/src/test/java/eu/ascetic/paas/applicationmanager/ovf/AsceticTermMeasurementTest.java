package eu.ascetic.paas.applicationmanager.ovf;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * 
 * Copyright 2016 ATOS SPAIN S.A. 
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
 * @author David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.garciaperez@atos.net 
 * 
 * Unit test for the class that represents the ASCETiC Term measurement inside an OVF to be converted to SLA
 *
 */
public class AsceticTermMeasurementTest {

	@Test
	public void pojoTest() {
		AsceticTermMeasurement termMeasurement = new AsceticTermMeasurement();
		termMeasurement.setAggregator("aggregator");
		termMeasurement.setBoundary(22.2);
		termMeasurement.setEvent("event");
		termMeasurement.setMetric("metric");
		termMeasurement.setParams(33);
		termMeasurement.setPeriod(111);
		termMeasurement.setComparator("LTE");
		
		assertEquals("aggregator", termMeasurement.getAggregator());
		assertEquals(22.2, termMeasurement.getBoundary().doubleValue(), 0.001);
		assertEquals("event", termMeasurement.getEvent());
		assertEquals("metric", termMeasurement.getMetric());
		assertEquals(33, termMeasurement.getParams().intValue());
		assertEquals(111, termMeasurement.getPeriod().intValue());
		assertEquals("LTE", termMeasurement.getComparator());
	}
}
