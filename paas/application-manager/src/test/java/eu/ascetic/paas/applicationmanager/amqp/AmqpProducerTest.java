package eu.ascetic.paas.applicationmanager.amqp;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import eu.ascetic.amqp.client.AmqpBasicListener;
import eu.ascetic.amqp.client.AmqpMessageReceiver;
import eu.ascetic.paas.applicationmanager.amqp.model.ApplicationManagerMessage;
import eu.ascetic.paas.applicationmanager.conf.Configuration;
import eu.ascetic.paas.applicationmanager.model.converter.ModelConverter;

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
 * This class test the correct behaviour of the AmqpProducer class
 *
 */
public class AmqpProducerTest extends AbstractTest {

	@Test
	public void sendMessageTest() throws Exception {
		// We manually modify the configuration object to be able to use personally for this test
		Configuration.amqpAddress = "localhost:7672";
		Configuration.amqpUsername = "guest";
		Configuration.amqpPassword = "guest";
		
		// We create the message to be sent
		ApplicationManagerMessage messageToBeSent = new ApplicationManagerMessage();
		messageToBeSent.setApplicationId("111");
		messageToBeSent.setDeploymentId("222");
		messageToBeSent.setStatus("SOMETHING");
		
		// We set a listener to get the sent message from the MessageQueue
		AmqpMessageReceiver receiver = new AmqpMessageReceiver(Configuration.amqpAddress, Configuration.amqpUsername, Configuration.amqpPassword,  "application.111.deployment.222", true);
		AmqpBasicListener listener = new AmqpBasicListener();
		receiver.setMessageConsumer(listener);
		
		// We send the message with our AmqpProducer
		AmqpProducer.sendMessage("application.111.deployment.222", messageToBeSent);
		
		// We wait one second just in case
		Thread.sleep(1000l);
		
		// We verify the results
		assertEquals("application.111.deployment.222", listener.getDestination());
		
		// We parse the received message and verify its values
		ApplicationManagerMessage amMessageReceived = ModelConverter.jsonToApplicationManagerMessage(listener.getMessage());
		assertEquals("111", amMessageReceived.getApplicationId());
		assertEquals("222", amMessageReceived.getDeploymentId());
		assertEquals("SOMETHING", amMessageReceived.getStatus());
		
		receiver.close();
	}
}
