package eu.ascetic.paas.applicationmanager.model;

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
 * @author: David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.garciaperez@atos.net 
 * 
 * This Unit test verifies the correct behaviour of the POJO Root
 * 
 */
public class RootTest {

	@Test
	public void testPojo() {
		Root root = new Root();
		root.setHref("/");
		root.setTimestamp("111");
		root.setVersion("0.1-SNAPSHOOT");
		List<Link> links = new ArrayList<Link>();
		root.setLinks(links);
		
		assertEquals("/", root.getHref());
		assertEquals("0.1-SNAPSHOOT", root.getVersion());
		assertEquals("111", root.getTimestamp());
		assertEquals(links, root.getLinks());
	}
	
	@Test
	public void addLinkTest() {
		Root root = new Root();
		
		assertEquals(null, root.getLinks());
		
		Link link = new Link();
		root.addLink(link);
		assertEquals(link, root.getLinks().get(0));
	}
}
