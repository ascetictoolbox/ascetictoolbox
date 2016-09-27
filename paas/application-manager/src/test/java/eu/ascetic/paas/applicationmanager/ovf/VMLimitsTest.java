package eu.ascetic.paas.applicationmanager.ovf;

import static org.junit.Assert.assertEquals;

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
 * @author David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.garciaperez@atos.net 
 * 
 * Unit tests for the class VMLimits
 *
 */
public class VMLimitsTest {

	@Test
	public void pojoTest() {
		VMLimits vmLimits = new VMLimits();
		vmLimits.setLowerNumberOfVMs(1);
		vmLimits.setUpperNumberOfVMs(2);
		vmLimits.setMaxNumberCPUs(4);
		vmLimits.setMinNumberCPUs(3);
		
		assertEquals(1l, vmLimits.getLowerNumberOfVMs());
		assertEquals(2l, vmLimits.getUpperNumberOfVMs());
		assertEquals(3, vmLimits.getMinNumberCPUs());
		assertEquals(4, vmLimits.getMaxNumberCPUs());
	}
}
