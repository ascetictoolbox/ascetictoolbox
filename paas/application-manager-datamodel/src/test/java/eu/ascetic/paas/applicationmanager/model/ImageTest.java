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
public class ImageTest {
	
	@Test
	public void pojo() {
		Image image = new Image();
		image.setHref("href");
		image.setId(11);
		image.setOvfId("ovf-id");
		image.setProviderImageId("provider-image-id");
		
		assertEquals("href", image.getHref());
		assertEquals(11, image.getId());
		assertEquals("ovf-id", image.getOvfId());
		assertEquals("provider-image-id", image.getProviderImageId());
	}
}
