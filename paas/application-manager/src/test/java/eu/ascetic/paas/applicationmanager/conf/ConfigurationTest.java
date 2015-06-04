package eu.ascetic.paas.applicationmanager.conf;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * 
 * Copyright 2015 ATOS SPAIN S.A. 
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
 * Test that the class that loads the configuration works as expected.
 */
public class ConfigurationTest {

	@Test
	public void loadConfigurationTest() {
		assertEquals("yes", Configuration.enableSLAM);
		assertEquals("http://111.222.333.444:8080/services/asceticNegotiation?wsdl", Configuration.slamURL);
		assertEquals("http://111.222.333.444:9000", Configuration.applicationMonitorUrl);
		assertEquals("/home/vmc2", Configuration.vmcontextualizerConfigurationFileDirectory);
		assertEquals("http://localhost2", Configuration.applicationManagerUrl);
		assertEquals("localhost2:5673", Configuration.amqpAddress);
		assertEquals("guest2", Configuration.amqpUsername);
		assertEquals("guest2", Configuration.amqpPassword);
	}
}
