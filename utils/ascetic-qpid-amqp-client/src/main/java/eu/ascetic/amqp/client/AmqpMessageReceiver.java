package eu.ascetic.amqp.client;

import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;

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
 * This is a basic class that can subscribe to any topic or queue using a JNDI configuration file. 
 * Extend this class an just override the onMessage method. 
 */
public class AmqpMessageReceiver extends AmqpAbstract implements MessageListener {
	private static Logger logger = Logger.getLogger(AmqpMessageReceiver.class);
	private MessageConsumer messageConsumer;
	private TextMessage textMessage;
	
	/**
	 * Constructor of Message receiver
	 * @param user to connect to the AMQP Broker, if <code>null</code>, it is set to "guest"
	 * @param password to connect to the AMQP Broker, if <code>null</code>, it is set to "guest"
	 * @param queueOrTopic to subscribe, it should be the JNDI name of the jndi.properties file.
	 * @throws Exception
	 */
	public AmqpMessageReceiver(String user, String password, String queueOrTopic) throws Exception {
		super(user, password, queueOrTopic);

		messageConsumer = session.createConsumer(queue);
		messageConsumer.setMessageListener(this);
	}

	@Override
	public void onMessage(Message message) {
        try {
            logger.info("Received message with ID = " + message.getJMSMessageID() + " for the topic: " + message.getJMSDestination().toString());
            message.acknowledge();
            
            textMessage = (TextMessage) message;
              
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	/**
	 * @return returns the latest message recieved, it returns <code>null</code> if it did not recieved anything.
	 */
	public TextMessage getLastMessage() {
		return textMessage;
	}
}

