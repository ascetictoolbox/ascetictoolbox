package eu.ascetic.paas.applicationmanager.vmmanager.datamodel;

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
 * 
 * Set of unit tests that verify the correct work of the Application Manager Rest Interface
 *
 */

public class ImageToUploadTest {

	@Test
	public void testEquals() {
		ImageToUpload imageToUpload = new ImageToUpload("xxx", "yyy");
		
		assertFalse(imageToUpload.equals(new String()));
		assertTrue(imageToUpload.equals(imageToUpload));
		assertTrue(imageToUpload.equals(new ImageToUpload("xxx", "yyy")));
		assertFalse(imageToUpload.equals(new ImageToUpload("xxx", "url")));
		assertFalse(imageToUpload.equals(new ImageToUpload("name", "yyy")));
		assertFalse(imageToUpload.equals(new ImageToUpload(null, null)));
		assertFalse(imageToUpload.equals(null));
	}
}
