package eu.ascetic.iaas.slamanager.pac.amqp;



import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.naming.NamingException;


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
public class AmqpMessageReceiver extends AmqpAbstract {
	private MessageConsumer messageConsumer;

	
	/**
	 * Constructor of Message receiver
	 * @param user to connect to the AMQP Broker, if <code>null</code>, it is set to "guest"
	 * @param password to connect to the AMQP Broker, if <code>null</code>, it is set to "guest"
	 * @param queueOrTopic to subscribe, it should be the JNDI name of the jndi.properties file.
	 * @throws Exception
	 */
	public AmqpMessageReceiver(String user, String password, String queueOrTopic) throws Exception {
		super(user, password, queueOrTopic);
	}
	
	/**
	 * Creates a topic or queue programatically, instaed of loading that information from the JNDI file
	 * @param url of the AMQP 1.0 broker, if it is <code>null</code> the system will use localhost:5672
	 * @param user to connect to the AMQP Broker, if <code>null</code>, it is set to "guest"
	 * @param password to connect to the AMQP Broker, if <code>null</code>, it is set to "guest"
	 * @param queueOrTopicName queue or topic name to subscribe to
	 * @param topic <code>true</code> if it is a topic, <code>false</code> if it iw a queue
	 * @throws Exception
	 */
	public AmqpMessageReceiver(String url, String user, String password, String queueOrTopicName, boolean topic) throws Exception {
		super(url, user, password, queueOrTopicName, topic);
	}

	public void setMessageConsumer(MessageListener messageListener) throws JMSException, NamingException {
		queue = (Destination) context.lookup(queueOrTopic);
		messageConsumer = session.createConsumer(queue);
		messageConsumer.setMessageListener(messageListener);
		
		startConnection();
	}
}

