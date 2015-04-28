package eu.ascetic.amqp.client;

import javax.jms.DeliveryMode;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;

/**
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
 * Implements the logic for producing Amqp 1.0 messages to a Amqp compatible broker
 */
public class AmqpMessageProducer extends AmqpAbstract {
	private static Logger logger = Logger.getLogger(AmqpMessageProducer.class);
	protected MessageProducer messageProducer;
	
	/**
	 * Opens the connection and creates the MessageProducer object using the JNDI configuration	
	 * @param user username to connect to the broker, if <code>null</code> is set to "guest" by defualt.
	 * @param password password to connect to the broker, if <code>null</code> is set to "guest" by defualt.
	 * @param queueOrTopic queue or topic to which to send messages
	 * @throws Exception
	 */
	public AmqpMessageProducer(String user, String password, String queueOrTopic) throws Exception {
		super(user, password, queueOrTopic);
		
		messageProducer = session.createProducer(queue);
	}
	
	/**
	 * It sends a message to the Message Queue for an specific topic
	 * @param message text message to be sent.
	 * @throws Exception
	 */
	public void sendMessage(String message) throws Exception {
		logger.info("Sending message to queue/topic: " + queueOrTopic);
		logger.debug("Message: " + message);
		
		TextMessage testMessage = session.createTextMessage(message);
		messageProducer.send(testMessage, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
	}
}
