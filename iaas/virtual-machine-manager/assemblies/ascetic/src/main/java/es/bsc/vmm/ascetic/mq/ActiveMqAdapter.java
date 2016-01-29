/**
 Copyright (C) 2013-2014  Barcelona Supercomputing Center

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package es.bsc.vmm.ascetic.mq;

import es.bsc.demiurge.core.configuration.Config;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.jms.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ActiveMqAdapter {

    private final ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
            Config.INSTANCE.getConfiguration().getString("activeMqUrl","tcp://localhost:61616")
    );

    private final Logger log = LogManager.getLogger(ActiveMqAdapter.class);

    /**
     * Publishes a message in the queue with the topic and the message specified
     *
     * @param topic the topic
     * @param message the message
     */
    public void publishMessage(String topic, String message) {
        Connection connection = null;
        Session session = null;
        try {
            // Create a Connection
            connection = connectionFactory.createConnection();
            connection.start();

            // Create a Session
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // Create the destination
            Destination destination = session.createTopic(topic);

            // Create a MessageProducer from the Session to the Topic
            MessageProducer producer = session.createProducer(destination);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

            producer.send(session.createTextMessage(message));

            session.close();
            connection.close();
        } catch (Exception e) {
			LogManager.getLogger(ActiveMqAdapter.class).warn("[VMM] Could not send topic " + topic + " to the message queue");
		} finally {
            try {
                session.close();
                connection.close();
            } catch(Exception e) {
                log.warn("Can't close connection: " + e.getMessage());
            }
        }
    }

	private Map<String, Connection> openConnections = new HashMap<>();
	private Map<String, Session> openSessions = new HashMap<>();

	public void listenToQueue(String queueName, MessageListener listener) throws JMSException {
		log.debug("Listening for messages to queue: " + queueName);
		QueueConnection connection = null;
		QueueSession session = null;
			connection = connectionFactory.createQueueConnection();
			session = connection.createQueueSession(true, Session.AUTO_ACKNOWLEDGE);
			Queue q = session.createQueue(queueName);
			MessageConsumer consumer = session.createConsumer(q);
			consumer.setMessageListener(listener);
			connection.start();
			openConnections.put(queueName, connection);
			openSessions.put(queueName,session);
	}

	public void closeQueue(String queueName) {
		try {
			log.debug("Closing queue " + queueName);
			Connection connection = openConnections.remove(queueName);
			Session session = openSessions.remove(queueName);
			connection.stop();
			connection.close();
			session.close();
		} catch(Exception e) {
			log.warn("Can't close connection: " + e.getMessage());
		}
	}

	public void closeAllQueues() {
		Set<String> cn = new HashSet<>(openConnections.keySet());
		for(String queue : cn) {
			closeQueue(queue);
		}
	}
}
