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
 */

public class VMTest {

	@Test
	public void pojoTest() {
		VM vm = new VM();
		vm.setId(1);
		vm.setHref("href");
		vm.setOvfId("ovfId");
		vm.setProviderId("provider-id");
		vm.setProviderVmId("provider-vm-id");
		vm.setStatus("XXX");
		vm.setIp("172.0.0.1");
		vm.setSlaAgreement("slaAggrementId");
		List<Image> images = new ArrayList<Image>();
		vm.setImages(images);
		List<Link> links = new ArrayList<Link>();
		vm.setLinks(links);
		
		assertEquals(1, vm.getId());
		assertEquals("href", vm.getHref());
		assertEquals("ovfId", vm.getOvfId());
		assertEquals("provider-id", vm.getProviderId());
		assertEquals("provider-vm-id", vm.getProviderVmId());
		assertEquals("XXX", vm.getStatus());
		assertEquals("172.0.0.1", vm.getIp());
		assertEquals("slaAggrementId", vm.getSlaAgreement());
		assertEquals(images, vm.getImages());
		assertEquals(links, vm.getLinks());
	}
	
	@Test
	public void addImageTest() {
		VM vm = new VM();
		
		assertEquals(null, vm.getImages());
		
		Image image = new Image();
		vm.addImage(image);
		assertEquals(1, vm.getImages().size());
		assertEquals(image, vm.getImages().get(0));
	}
	
	@Test
	public void addLinkTest() {
		VM vm = new VM();
		
		assertEquals(null, vm.getLinks());
		
		Link link = new Link();
		vm.addLink(link);
		assertEquals(1, vm.getLinks().size());
		assertEquals(link, vm.getLinks().get(0));
	}
}
