/**
 * Copyright 2015 University of Leeds
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package eu.ascetic.paas.self.adaptation.manager.activemq;

import eu.ascetic.amqp.client.AmqpExceptionListener;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * This is a set of helper procedures for creating either producers or consumers
 * using the Apache Active MQ messaging system.
 *
 * @author Richard Kavanagh
 */
public abstract class ActiveMQBase {

    protected String user = "guest";
    protected String password = "guest";
    protected AmqpExceptionListener amqpExceptionListener = new AmqpExceptionListener();
    protected Connection connection;
    protected Session session;
    protected Context context;

    /**
     * This is the constructor for the ActiveMQBase class. It establishes the
     * connection and the session for derived classes. It uses the default
     * guest:guest username and password.
     * @throws JMSException
     * @throws NamingException 
     */
    public ActiveMQBase() throws JMSException, NamingException {
        initialise();
    }
    
    /**
     * This is the constructor for the ActiveMQBase class. It establishes the
     * connection and the session for derived classes.
     * @param user The username to connect with
     * @param password The password to connect with
     * @throws javax.naming.NamingException
     * @throws javax.jms.JMSException
     */
    public ActiveMQBase(String user, String password) throws NamingException, JMSException {
        if (user != null) {
            this.user = user;
        }
        if (password != null) {
            this.password = password;
        }
        initialise();
    }
    
    /**
     * This is the generic code code from the ActiveMQBase's constructors.
     * @throws NamingException
     * @throws JMSException 
     */
    private void initialise() throws NamingException, JMSException {
        context = new InitialContext();

        ConnectionFactory factory = (ConnectionFactory) context.lookup("myFactoryLookup");

        connection = factory.createConnection(this.user, this.password);
        connection.setExceptionListener(amqpExceptionListener);
        connection.start();

        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    /**
     * This changes the default exception listener.
     *
     * @param amqpExceptionListener
     */
    public void setAmqpExceptionListener(AmqpExceptionListener amqpExceptionListener) {
        this.amqpExceptionListener = amqpExceptionListener;
    }

    protected Connection getConnection() throws JMSException, NamingException {
        // Create a ConnectionFactory
        ConnectionFactory factory = (ConnectionFactory) context.lookup("myFactoryLookup");
        // Create a Connection
        connection = factory.createConnection();
        connection.start();
        return connection;
    }

    /**
     * This gets a message producer for a given session's queue.
     * @param session The session to get the message producer for
     * @param queue The queue to create the message producer for
     * @return The message producer which is ready to send messages.
     * @throws JMSException 
     */
    protected MessageProducer getMessageProducer(Session session, Destination queue) throws JMSException {
        // Create a MessageProducer from the Session to the Topic or Queue
        MessageProducer producer = session.createProducer(queue);
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        return producer;
    }

    /**
     * This gets a queue from the current context
     * @param queue The name of the queue to get
     * @return The queue for messages to be sent to
     * @throws NamingException 
     */
    protected Destination getMessageQueue(String queue) throws NamingException {
        context = new InitialContext();
        return (Destination) context.lookup(queue);
    }

    /**
     * Closes the connection to the message queue backbone.
     *
     * @throws JMSException
     */
    public void close() throws JMSException {
        session.close();
        connection.close();
    }

}
