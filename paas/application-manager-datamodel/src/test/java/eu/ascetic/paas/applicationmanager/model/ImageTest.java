package eu.ascetic.paas.applicationmanager.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
public class ImageTest {
	
	@Test
	public void pojo() {
		Image image = new Image();
		image.setHref("href");
		image.setId(11);
		image.setOvfId("ovf-id");
		image.setProviderImageId("provider-image-id");
		image.setOvfHref("ovf-href");
		image.setProviderId("provider-id");
		image.setDemo(true);
		
		assertEquals("href", image.getHref());
		assertEquals(11, image.getId());
		assertEquals("ovf-id", image.getOvfId());
		assertEquals("provider-image-id", image.getProviderImageId());
		assertEquals("ovf-href", image.getOvfHref());
		assertEquals("provider-id", image.getProviderId());
		assertTrue(image.isDemo());
	}
	
	@Test
	public void equalsTest() {
		Image image = new Image();
		image.setHref("href");
		image.setId(11);
		image.setOvfId("ovf-id");
		image.setProviderImageId("provider-image-id");
		image.setOvfHref("ovf-href");
		image.setProviderId("provider-id");
		image.setDemo(true);
		
		assertFalse(image.equals(null));
		assertFalse(image.equals(new Image()));
		assertTrue(image.equals(image));
		
		Image image1 = new Image();
		image1.setHref("href");
		image1.setId(11);
		image1.setOvfId("ovf-id");
		image1.setProviderImageId("provider-image-id");
		image1.setOvfHref("ovf-href");
		image1.setProviderId("provider-id");
		image1.setDemo(true);
		
		assertTrue(image.equals(image1));
		
		Image image2 = new Image();
		image2.setHref("href1");
		image2.setId(11);
		image2.setOvfId("ovf-id");
		image2.setProviderImageId("provider-image-id");
		image2.setOvfHref("ovf-href");
		image2.setProviderId("provider-id");
		image2.setDemo(true);
		
		assertFalse(image.equals(image2));
		
		Image image3 = new Image();
		image3.setHref("href1");
		image3.setId(111);
		image3.setOvfId("ovf-id");
		image3.setProviderImageId("provider-image-id");
		image3.setOvfHref("ovf-href");
		image3.setProviderId("provider-id");
		image3.setDemo(true);
		
		assertFalse(image.equals(image3));
		
		Image image4 = new Image();
		image4.setHref("href1");
		image4.setId(11);
		image4.setOvfId("ovf-id1");
		image4.setProviderImageId("provider-image-id");
		image4.setOvfHref("ovf-href");
		image4.setProviderId("provider-id");
		image4.setDemo(true);
		
		assertFalse(image.equals(image4));
		
		Image image5 = new Image();
		image5.setHref("href1");
		image5.setId(11);
		image5.setOvfId("ovf-id");
		image5.setProviderImageId("provider-image-id1");
		image5.setOvfHref("ovf-href");
		image5.setProviderId("provider-id");
		image5.setDemo(true);
		
		assertFalse(image.equals(image5));
		
		Image image6 = new Image();
		image6.setHref("href1");
		image6.setId(11);
		image6.setOvfId("ovf-id");
		image6.setProviderImageId("provider-image-id");
		image6.setOvfHref("ovf-href1");
		image6.setProviderId("provider-id");
		image6.setDemo(true);
		
		assertFalse(image.equals(image6));
		
		Image image7 = new Image();
		image7.setHref("href1");
		image7.setId(11);
		image7.setOvfId("ovf-id");
		image7.setProviderImageId("provider-image-id");
		image7.setOvfHref("ovf-href");
		image7.setProviderId("provider-id1");
		image7.setDemo(true);
		
		assertFalse(image.equals(image7));
		
		Image image8 = new Image();
		image8.setHref("href1");
		image8.setId(11);
		image8.setOvfId("ovf-id");
		image8.setProviderImageId("provider-image-id");
		image8.setOvfHref("ovf-href");
		image8.setProviderId("provider-id");
		image8.setDemo(false);
		
		assertFalse(image.equals(image8));
	}
}
