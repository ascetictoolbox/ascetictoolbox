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
import java.io.File;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.apache.activemq.advisory.DestinationSource;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * This is a set of helper procedures for creating either producers or consumers
 * using the Apache Active MQ messaging system.
 *
 * @author Richard Kavanagh
 */
public abstract class ActiveMQBase {

    protected String user = "guest";
    protected String password = "guest";
    protected String factoryLookupName = "myFactoryLookup";
    protected AmqpExceptionListener amqpExceptionListener = new AmqpExceptionListener();
    protected Connection connection;
    protected Session session;
    protected boolean useURL = true;
    /**
     * Example urls: 
     * Y2: Stable: 192.168.3.222:5673 
     * Y2: Testing: 192.168.3.16:5673 
     * Y1: 10.4.0.16:5672
     */
    protected String url = "192.168.3.222:5673";
    private static final String CONFIG_FILE = "paas-self-adaptation-manager.properties";

    /**
     * This is the constructor for the ActiveMQBase class. It establishes the
     * connection and the session for derived classes. It uses the default
     * guest:guest username and password.
     *
     * @throws JMSException An exception thrown in the event of an ActiveMQ
     * error.
     * @throws NamingException An exception thrown in the event of an ActiveMQ
     * error.
     */
    public ActiveMQBase() throws JMSException, NamingException {
        try {
            PropertiesConfiguration config;
            if (new File(CONFIG_FILE).exists()) {
                config = new PropertiesConfiguration(CONFIG_FILE);
            } else {
                config = new PropertiesConfiguration();
                config.setFile(new File(CONFIG_FILE));
            }
            config.setAutoSave(true); //This will save the configuration file back to disk. In case the defaults need setting.
            user = config.getString("paas.self.adaptation.manager.activemq.username", user);
            config.setProperty("paas.self.adaptation.manager.activemq.username", user);
            password = config.getString("paas.self.adaptation.manager.activemq.password", password);
            config.setProperty("paas.self.adaptation.manager.activemq.password", password);
            useURL = config.getBoolean("paas.self.adaptation.manager.activemq.use.url", useURL);
            config.setProperty("paas.self.adaptation.manager.activemq.use.url", useURL);
            url = config.getString("paas.self.adaptation.manager.activemq.url", url);
            config.setProperty("paas.self.adaptation.manager.activemq.url", url);
            if (useURL == false) {
                factoryLookupName = config.getString("paas.self.adaptation.manager.activemq.factory.name", factoryLookupName);
                config.setProperty("paas.self.adaptation.manager.activemq.factory.name", factoryLookupName);
                initialise();
            } else {
                initialise(url);
            }
        } catch (ConfigurationException ex) {
            Logger.getLogger(ActiveMQBase.class.getName()).log(Level.INFO, "Error loading the configuration of the PaaS Self adaptation manager", ex);
        }

    }

    /**
     * This is the constructor for the ActiveMQBase class. It establishes the
     * connection and the session for derived classes. It uses the specified
     * username password and factory lookup name.
     *
     * @param user The user name to use
     * @param password the password to use
     * @param url The url used to connect to ActiveMQ
     * @throws JMSException An exception thrown in the event of an ActiveMQ
     * error.
     * @throws NamingException An exception thrown in the event of an ActiveMQ
     * error.
     */
    public ActiveMQBase(String user, String password, String url) throws JMSException, NamingException {
        this.user = user;
        this.password = password;
        this.url = url;
        initialise(url);
    }

    /**
     * This is the constructor for the ActiveMQBase class. It establishes the
     * connection and the session for derived classes.
     *
     * @param user The username to connect with
     * @param password The password to connect with
     * @throws JMSException An exception thrown in the event of an ActiveMQ
     * error.
     * @throws NamingException An exception thrown in the event of an ActiveMQ
     * error.
     */
    public ActiveMQBase(String user, String password) throws NamingException, JMSException {
        if (user != null) {
            this.user = user;
        }
        if (password != null) {
            this.password = password;
        }
        if (useURL) {
            initialise(url);
        } else {
            initialise();
        }
    }

    /**
     * This initialises the ActiveMQ base.
     *
     * @param url The url that points to the Apache Active MQ server.
     * @throws JMSException An exception thrown in the event of an ActiveMQ
     * error.
     * @throws NamingException An exception thrown in the event of an ActiveMQ
     * error.
     */
    private void initialise(String url) throws NamingException, JMSException {

        String initialContextFactory = "org.apache.qpid.jms.jndi.JmsInitialContextFactory";
        String connectionJNDIName = UUID.randomUUID().toString();
        String connectionURL = "amqp://" + this.user + ":" + this.password + "@" + url;

        // Set the properties ...
        Properties properties = new Properties();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, initialContextFactory);
        properties.put("connectionfactory." + connectionJNDIName, connectionURL);

        // Now we have the context already configured... 
        // Create the initial context
        Context context = new InitialContext(properties);

        ConnectionFactory factory = (ConnectionFactory) context.lookup(connectionJNDIName);

        connection = factory.createConnection(this.user, this.password);
        connection.setExceptionListener(amqpExceptionListener);
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        connection.start();
    }

    /**
     * This is the generic code code from the ActiveMQBase's constructors, this
     * initialises the ActiveMQ base based upon a lookup discovery mechanism.
     *
     * @throws JMSException An exception thrown in the event of an ActiveMQ
     * error.
     * @throws NamingException An exception thrown in the event of an ActiveMQ
     * error.
     */
    private void initialise() throws NamingException, JMSException {
        Context context = new InitialContext();

        ConnectionFactory factory = (ConnectionFactory) context.lookup(factoryLookupName);

        connection = factory.createConnection(user, password);
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

    /**
     * This returns the current connection for Active MQ
     *
     * @return This returns a new connection.
     * @throws JMSException An exception thrown in the event of an ActiveMQ
     * error.
     * @throws NamingException An exception thrown in the event of an ActiveMQ
     * error.
     */
    protected Connection getConnection() throws JMSException, NamingException {
        return connection;
    }

    /**
     * This gets a message producer for a given session's queue.
     *
     * @param session The session to get the message producer for
     * @param queue The queue to create the message producer for
     * @return The message producer which is ready to send messages.
     * @throws JMSException An exception thrown in the event of an ActiveMQ
     * error.
     */
    protected MessageProducer getMessageProducer(Session session, Destination queue) throws JMSException {
        // Create a MessageProducer from the Session to the Topic or Queue
        MessageProducer producer = session.createProducer(queue);
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        return producer;
    }

    /**
     * This gets a queue from the current context
     *
     * @param queue The name of the queue to get
     * @return The queue for messages to be sent to
     * @throws JMSException An exception thrown in the event of an ActiveMQ
     * error.
     * @throws NamingException An exception thrown in the event of an ActiveMQ
     * error.
     */
    protected Destination getMessageQueue(String queue) throws NamingException, JMSException {
        return (Destination) session.createTopic(queue);
    }

    /**
     * This gets a topic from the current context
     *
     * @param topic The name of the topic to get
     * @return The topic for messages to be sent to
     * @throws JMSException An exception thrown in the event of an ActiveMQ
     * error.
     * @throws NamingException An exception thrown in the event of an ActiveMQ
     * error.
     */
    protected Destination getTopic(String topic) throws NamingException, JMSException {
        return (Destination) session.createTopic(topic);
    }

    /**
     * This lists the available queues on the server. Queues have a single
     * message that will be received by exactly one consumer. If there are no
     * consumers available at the time the message is sent it will be kept until
     * a consumer is available to process the message.
     *
     * @return The list of queues on the server.
     */
    public Set<ActiveMQQueue> getQueues() {
        DestinationSource source;
        try {
            source = new DestinationSource(connection);
            return source.getQueues();
        } catch (JMSException ex) {
            Logger.getLogger(ActiveMQBase.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * This lists the available queues on the server.
     *
     * Topics operates with a publish and subscribe mechanism. When you publish
     * a message is sent to all subscribers. Only subscribers who had an active
     * subscription at the time the broker receives a message will get a copy.
     *
     * @return The list of topics on the server.
     */
    public Set<ActiveMQTopic> getTopics() {
        DestinationSource source;
        try {
            source = new DestinationSource(connection);
            return source.getTopics();
        } catch (JMSException ex) {
            Logger.getLogger(ActiveMQBase.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * This method prints to standard out a list of all queues and topics.
     */
    public void printQueueAndTopicInformation() {
        Set<ActiveMQQueue> queues = getQueues();
        for (ActiveMQQueue queue1 : queues) {
            try {
                System.out.println("Queue Name: " + queue1.getQueueName());
                System.out.println("Queue Physical: " + queue1.getPhysicalName());
            } catch (JMSException ex) {
                Logger.getLogger(ActiveMQBase.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        Set<ActiveMQTopic> topics = getTopics();
        for (ActiveMQTopic topics1 : topics) {
            try {
                System.out.println("Topic Name: " + topics1.getTopicName());
                System.out.println("Topic Physical: " + topics1.getPhysicalName());
            } catch (JMSException ex) {
                Logger.getLogger(ActiveMQBase.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Closes the connection to the message queue backbone.
     */
    public void close() {
        if (session != null) {
            try {
                session.close();
            } catch (JMSException ex) {
                Logger.getLogger(ActiveMQBase.class.getName()).log(Level.SEVERE, null, ex);
            }
            session = null;
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (JMSException ex) {
                Logger.getLogger(ActiveMQBase.class.getName()).log(Level.SEVERE, null, ex);
            }
            connection = null;
        }
    }

}
