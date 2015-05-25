package eu.ascetic.amqp.client;

import static org.junit.Assert.assertEquals;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

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
public class AmqpMessageRecieverTest extends AbstractTest {
	private static final String USER = "guest";
	private static final String PASSWORD = "guest";
	private AmqpExceptionListener amqpExceptionListener = new AmqpExceptionListener();

	@Test
	public void test() throws Exception {
		AmqpMessageReceiver messageReceiver = new AmqpMessageReceiver(USER, PASSWORD, "myTopicLookup");
		
		// The configuration for the Qpid InitialContextFactory has been supplied in
		// a jndi.properties file in the classpath, which results in it being picked
		// up automatically by the InitialContext constructor.
		Context context = new InitialContext();

		ConnectionFactory factory = (ConnectionFactory) context.lookup("myFactoryLookup");
		Destination queue = (Destination) context.lookup("myTopicLookup");

		Connection connection = factory.createConnection(USER, PASSWORD);
		connection.setExceptionListener(amqpExceptionListener);
		connection.start();

		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

		MessageProducer messageProducer = session.createProducer(queue);

		TextMessage message = session.createTextMessage("Hello world 1");
		messageProducer.send(message, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
		
		Thread.sleep(1000l);
		TextMessage recievedMessage = messageReceiver.getLastMessage();
		assertEquals("topic.pepito", recievedMessage.getJMSDestination().toString());
		assertEquals("Hello world 1", recievedMessage.getText());
		
		message = session.createTextMessage("Hello world!");
		messageProducer.send(message, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
		
		Thread.sleep(1000l);
		recievedMessage = messageReceiver.getLastMessage();
		assertEquals("topic.pepito", recievedMessage.getJMSDestination().toString());
		assertEquals("Hello world!", recievedMessage.getText());

		// We close all connections
		connection.close();
		messageReceiver.close();
	}
	
	@Test
	public void testSetAmqpExceptionListener() throws Exception {
		AmqpMessageReceiver reciever = new AmqpMessageReceiver(null, null, "myTopicLookup");
		
		AmqpExceptionListener exceptionListener = new AmqpExceptionListener();
		reciever.setAmqpExceptionListener(exceptionListener);
		
		assertEquals(exceptionListener, reciever.amqpExceptionListener);
		
		reciever.close();
	}
	
	@Test
	public void testSettingUserAndPassword() throws Exception {
		AmqpMessageReceiver reciever = new AmqpMessageReceiver(null, null, "myTopicLookup");
		
		assertEquals("guest", reciever.user);
		assertEquals("guest", reciever.password);
		
		reciever.close();
		
		reciever = new AmqpMessageReceiver("aaa", null, "myTopicLookup");
		
		assertEquals("aaa", reciever.user);
		assertEquals("guest", reciever.password);
		
		reciever.close();
		
		reciever = new AmqpMessageReceiver(null, "bbb", "myTopicLookup");
		
		assertEquals("guest", reciever.user);
		assertEquals("bbb", reciever.password);
		
		reciever.close();
		
		reciever = new AmqpMessageReceiver("aaa", "bbb", "myTopicLookup");
		
		assertEquals("aaa", reciever.user);
		assertEquals("bbb", reciever.password);
		
		reciever.close();
	}
}