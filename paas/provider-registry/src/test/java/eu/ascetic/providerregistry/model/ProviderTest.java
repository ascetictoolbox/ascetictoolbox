package eu.ascetic.providerregistry.model;

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
 * @author David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * @email david.garciaperez@atos.net 
 * 
 * Unit test to verify all the methods and functions of the Provider.class
 */
public class ProviderTest {

	@Test
	public void pojoTest() {
		Provider provider = new Provider();
		provider.setHref("href");
		provider.setId(10);
		provider.setName("Name");
		provider.setVmmUrl("http...");
		List<Link> links = new ArrayList<Link>();
		provider.setLinks(links);
		
		assertEquals("href", provider.getHref());
		assertEquals(10l, provider.getId());
		assertEquals("Name", provider.getName());
		assertEquals("http...", provider.getVmmUrl());
		assertEquals(links, provider.getLinks());
	}
	
	@Test
	public void addLinkTest() {
		Provider provider = new Provider();
		
		assertEquals(null, provider.getLinks());
		
		Link link = new Link();
		provider.addLink(link);
		
		assertEquals(1, provider.getLinks().size());
		assertEquals(link, provider.getLinks().get(0));
	}
}
