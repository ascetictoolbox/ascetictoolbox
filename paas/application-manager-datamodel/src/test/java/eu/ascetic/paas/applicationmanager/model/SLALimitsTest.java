package eu.ascetic.paas.applicationmanager.model;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

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
 * Unit test for POJO Object that represents the SLA Limits for a Deployment
 *
 */
public class SLALimitsTest {

	@Test
	public void pojoTest() {
		SLALimits slaLimits = new SLALimits();
		slaLimits.setCost("cost");
		slaLimits.setCostUnit("costUnit");
		slaLimits.setEnergy("energy");
		slaLimits.setEnergyUnit("energyUnit");
		slaLimits.setPower("power");
		slaLimits.setPowerUnit("powerUnit");
		List<SLAVMLimits> limits = new ArrayList<SLAVMLimits>();
		slaLimits.setVmLimits(limits);
		
		assertEquals("cost", slaLimits.getCost());
		assertEquals("costUnit", slaLimits.getCostUnit());
		assertEquals("energy", slaLimits.getEnergy());
		assertEquals("energyUnit", slaLimits.getEnergyUnit());
		assertEquals("power", slaLimits.getPower());
		assertEquals("powerUnit", slaLimits.getPowerUnit());
		assertEquals(limits, slaLimits.getVmLimits());
	}
}
