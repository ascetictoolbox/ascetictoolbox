package eu.ascetic.amqp.client;

import static org.junit.Assert.assertEquals;


import javax.jms.TextMessage;

import org.junit.Test;

import eu.ascetic.amqp.AbstractTest;

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
 * Checks the correct work of the class: AmqpMessageRecierver
 */
public class AmqpMessageProducerTest extends AbstractTest {

	@Test
	public void sendMessageTest() throws Exception {
		AmqpMessageReceiver receiver = new AmqpMessageReceiver("guest", "guest", "myTopicLookup");
		AmqpMessageProducer producer = new AmqpMessageProducer("guest", "guest", "myTopicLookup");
		
		producer.sendMessage("testX");
		
		Thread.sleep(1000l);
		
		TextMessage message = receiver.getLastMessage();
		
		assertEquals("topic.pepito", message.getJMSDestination().toString());
		assertEquals("testX", message.getText());
		
		receiver.close();
		producer.close();
	}
}
