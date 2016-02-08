package eu.ascetic.test.paas.applicationmanager.client;

import org.springframework.web.client.RestTemplate;

import eu.ascetic.paas.applicationmanager.model.Root;
import eu.ascetic.test.conf.Configuration;

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
 * REST Client to the Application Manager
 */
public class ApplicationManagerClient {

	/**
	 * @return returns the actual application manager version
	 */
	public static String getApplicationManagerVersion() {
		RestTemplate restTemplate = new RestTemplate();
		Root root = restTemplate.getForObject(Configuration.applicationManagerURL, Root.class);
		return root.getVersion();
	}
}
