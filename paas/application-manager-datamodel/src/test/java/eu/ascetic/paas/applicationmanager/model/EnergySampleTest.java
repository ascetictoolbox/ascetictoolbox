package eu.ascetic.paas.applicationmanager.model;

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
 * @author: David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.garciaperez@atos.net 
 */
public class EnergySampleTest {

	@Test
	public void pojo() {
		EnergySample energySample = new EnergySample();
		energySample.setCvalue(1.0);
		energySample.setEvalue(2.0);
		energySample.setPvalue(3.0);
		energySample.setTimestampBeging(1l);
		energySample.setTimestampEnd(2l);
		energySample.setVmid("vmid");
		
		assertEquals("vmid", energySample.getVmid());
		assertEquals(1.0, energySample.getCvalue(), 0.00001);
		assertEquals(2.0, energySample.getEvalue(), 0.00001);
		assertEquals(3.0, energySample.getPvalue(), 0.00001);
		assertEquals(1l, energySample.getTimestampBeging());
		assertEquals(2l, energySample.getTimestampEnd());
	}
}
