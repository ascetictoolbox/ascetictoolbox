package eu.ascetic.paas.applicationmanager.model;

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
 * @author: David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.garciaperez@atos.net 
 * 
 * Unit test for the class SLAInfoTerm
 * 
 */
public class SLAInfoTermTest {
	
	@Test
	public void pojo() {
		SLAInfoTerm slaInfoTerm = new SLAInfoTerm();
		slaInfoTerm.setComparator("comparator");
		slaInfoTerm.setMetricUnit("metricUnit");
		slaInfoTerm.setSlaTerm("slaTemr");
		slaInfoTerm.setSlaType("slaType");
		slaInfoTerm.setValue("value");
		
		assertEquals("value", slaInfoTerm.getValue());
		assertEquals("slaType", slaInfoTerm.getSlaType());
		assertEquals("slaTemr", slaInfoTerm.getSlaTerm());
		assertEquals("metricUnit", slaInfoTerm.getMetricUnit());
		assertEquals("comparator", slaInfoTerm.getComparator());
	}

}
