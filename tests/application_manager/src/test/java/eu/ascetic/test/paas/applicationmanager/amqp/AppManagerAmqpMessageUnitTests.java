package eu.ascetic.test.paas.applicationmanager.amqp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

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
 * Test Representation of a mesage to see if it was sended...
 */
public class AppManagerAmqpMessageUnitTests {

	@Test
	public void pojo() {
		AppManagerAmqpMessage message = new AppManagerAmqpMessage();
		message.setApplicationId("appId");
		message.setDeploymentId("depId");
		message.setStatus("status");
		
		assertEquals("status", message.getStatus());
		assertEquals("depId", message.getDeploymentId());
		assertEquals("appId", message.getApplicationId());
	}
	
	@Test
	public void equalsTest() {
		AppManagerAmqpMessage message01 = new AppManagerAmqpMessage();
		message01.setApplicationId("appId");
		message01.setDeploymentId("depId");
		message01.setStatus("status");
		
		AppManagerAmqpMessage message02 = new AppManagerAmqpMessage();
		message02.setApplicationId("appId");
		message02.setDeploymentId("depId");
		message02.setStatus("status");
		
		AppManagerAmqpMessage message03= new AppManagerAmqpMessage();
		message03.setApplicationId("appId1");
		message03.setDeploymentId("depId");
		message03.setStatus("status");
		
		AppManagerAmqpMessage message04 = new AppManagerAmqpMessage();
		message04.setApplicationId("appId");
		message04.setDeploymentId("depId1");
		message04.setStatus("status");
		
		AppManagerAmqpMessage message05 = new AppManagerAmqpMessage();
		message05.setApplicationId("appId");
		message05.setDeploymentId("depId");
		message05.setStatus("status1");
		
		assertTrue(message01.equals(message01));
		assertFalse(message01.equals(null));
		assertTrue(message01.equals(message02));
		assertFalse(message01.equals(message03));
		assertFalse(message01.equals(message04));
		assertFalse(message01.equals(message05));
	}
}
