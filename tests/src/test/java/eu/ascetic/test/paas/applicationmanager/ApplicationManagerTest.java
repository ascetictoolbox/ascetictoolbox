package eu.ascetic.test.paas.applicationmanager;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import eu.ascetic.test.conf.Configuration;
import eu.ascetic.test.paas.applicationmanager.client.ApplicationManagerClient;

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
 *  
 *  It contains all the Integration/System Tests for the Application Manager
 */

public class ApplicationManagerTest {

	/*
	 * If verifies if the Application Manager is active
	 * It performs a GET over the /application-manager path
	 */
	@Test
	public void isAppManagerRunning() {
		String version = ApplicationManagerClient.getApplicationManagerVersion();
		assertEquals("It is impossible to connect to the Application Manager in this address: " + Configuration.applicationManagerURL, "0.1-SNAPSHOT", version);
	}
}
