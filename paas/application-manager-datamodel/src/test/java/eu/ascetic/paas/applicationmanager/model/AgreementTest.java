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
 * @email david.garciaperez@atos.net 
 */

public class AgreementTest {

	@Test
	public void pojoTest() {
		Agreement agreement = new Agreement();
		agreement.setId(1);
		agreement.setDeploymentId("deployment-id");
		agreement.setHref("href");
		agreement.setPrice("222");
		agreement.setSlaAgreement("sla-agreement");
	
		assertEquals(1, agreement.getId());
		assertEquals("deployment-id", agreement.getDeploymentId());
		assertEquals("href", agreement.getHref());
		assertEquals("222", agreement.getPrice());
		assertEquals("sla-agreement", agreement.getSlaAgreement());
	}
	
	@Test
	public void addLinkTest() {
		Agreement agreement = new Agreement();
		
		assertEquals(null, agreement.getLinks());
		
		Link link = new Link();
		agreement.addLink(link);
		
		assertEquals(1, agreement.getLinks().size());
		assertEquals(link, agreement.getLinks().get(0));
	}
}
