package eu.ascetic.paas.applicationmanager.slam.sla.model;

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
 * unit test for  the InterfaceDeclr entry inside an SLA Template
 */
public class InterfaceDeclrTest {

	@Test
	public void pojoTest() {
		InterfaceDeclr interfaceDeclr = new InterfaceDeclr();
		Properties properties = new Properties();
		interfaceDeclr.setProperties(properties);
		interfaceDeclr.setId("id");
		interfaceDeclr.setProviderRef("ref");
		Interface intf = new Interface();
		interfaceDeclr.setIntf(intf);
		EndPoint endpoint = new EndPoint();
		interfaceDeclr.setEndPoint(endpoint);
		
		assertEquals(endpoint, interfaceDeclr.getEndPoint());
		assertEquals(intf, interfaceDeclr.getIntf());
		assertEquals("id", interfaceDeclr.getId());
		assertEquals("ref", interfaceDeclr.getProviderRef());
		assertEquals(properties, interfaceDeclr.getProperties());
	}
}
