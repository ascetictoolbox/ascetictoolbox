package eu.ascetic.providerregistry.model;

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
 * @email david.garciaperez@atos.net 
 * 
 * Test the POJO Link funcitonality.
 */
public class LinkTest {

	@Test
	public void gettersAndSettersTest() {
		Link link = new Link();
		link.setHref("http://something.com");
		link.setRel("/");
		link.setType("application+xml");
		
		assertEquals("http://something.com", link.getHref());
		assertEquals("/", link.getRel());
		assertEquals("application+xml", link.getType());
	}
	
	@Test
	public void constructorTest() {
		Link link = new Link("/", "http://something.com", "application+xml");
		
		assertEquals("http://something.com", link.getHref());
		assertEquals("/", link.getRel());
		assertEquals("application+xml", link.getType());
	}
}
