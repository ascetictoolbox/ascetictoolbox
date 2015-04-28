package eu.ascetic.amqp.client;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;

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
 * Implements common logic for both the AmpqReciever and AmpqProducer
 */
public abstract class AmqpAbstract {
	protected String user = "guest";
	protected String password = "guest";
	protected AmqpExceptionListener amqpExceptionListener = new AmqpExceptionListener();
	protected Connection connection;
	protected Session session;
	protected Destination queue;
	protected String queueOrTopic;
	
	public AmqpAbstract(String user, String password, String queueOrTopic) throws Exception {
		this.queueOrTopic = queueOrTopic;
		
		if(user != null) {
			this.user = user;
		} 
		
		if(password != null) {
			this.password = password;
		}
		
		Context context = new InitialContext();

		ConnectionFactory factory = (ConnectionFactory) context.lookup("myFactoryLookup");
		queue = (Destination) context.lookup(queueOrTopic);

	    connection = factory.createConnection(this.user, this.password);
		connection.setExceptionListener(amqpExceptionListener);
		connection.start();

		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
	}
	
	/**
	 * Closes the connection to the Broker
	 * @throws JMSException
	 */
    public void close() throws JMSException {
        connection.close();
    }
	
    /**
     * Changes the default exception listener...
     * @param amqpExceptionListener
     */
    public void setAmqpExceptionListener(AmqpExceptionListener amqpExceptionListener) {
    	this.amqpExceptionListener = amqpExceptionListener;
    }
}
