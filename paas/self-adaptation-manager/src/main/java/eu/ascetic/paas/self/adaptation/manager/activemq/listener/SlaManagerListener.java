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
package eu.ascetic.paas.self.adaptation.manager.activemq.listener;

import eu.ascetic.paas.self.adaptation.manager.EventListener;
import eu.ascetic.paas.self.adaptation.manager.activemq.ActiveMQBase;
import eu.ascetic.paas.self.adaptation.manager.rules.EventAssessor;
import eu.ascetic.paas.self.adaptation.manager.rules.EventDataConverter;
import eu.ascetic.paas.self.adaptation.manager.rules.datatypes.EventData;
import eu.ascetic.paas.slam.pac.events.ViolationMessage;
import eu.ascetic.paas.slam.pac.events.ViolationMessageTranslator;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.TextMessage;
import javax.naming.NamingException;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * This listens to the SLA manager as a source of events to monitor for
 * adaptation.
 *
 * @author Richard Kavanagh
 */
public class SlaManagerListener extends ActiveMQBase implements Runnable, EventListener {

    private final Destination queue;
    private static String queue_name = "slamanager";
    // Create a MessageConsumer from the Session to the Topic or Queue
    private final MessageConsumer consumer;
    private EventAssessor eventAssessor;
    private boolean running = true;
    private static final String CONFIG_FILE = "paas-self-adaptation-manager.properties";
    private ViolationMessageTranslator converter = new ViolationMessageTranslator();

    public SlaManagerListener() throws JMSException, NamingException {
        super();
        try {
            PropertiesConfiguration config;
            if (new File(CONFIG_FILE).exists()) {
                config = new PropertiesConfiguration(CONFIG_FILE);
            } else {
                config = new PropertiesConfiguration();
                config.setFile(new File(CONFIG_FILE));
            }
            config.setAutoSave(true); //This will save the configuration file back to disk. In case the defaults need setting.
            queue_name = config.getString("paas.self.adaptation.manager.sla.event.queue.name", queue_name);
            config.setProperty("paas.self.adaptation.manager.sla.event.queue.name", queue_name);
        } catch (ConfigurationException ex) {
            Logger.getLogger(SlaManagerListener.class.getName()).log(Level.INFO, "Error loading the configuration of the PaaS Self adaptation manager", ex);
        }
        queue = getMessageQueue(queue_name);
        consumer = session.createConsumer(queue);
    }

    /**
     * This creates an SLA manager listener that can have its configuration
     * information set.
     *
     * @param user The user name to use
     * @param password the password to use
     * @param url The factory used to lookup the message queue.
     * @param topicName The queue name to use
     * @throws JMSException
     * @throws NamingException
     */
    public SlaManagerListener(String user, String password, String url, String topicName) throws JMSException, NamingException {
        super(user, password, url);
        queue = getTopic(topicName);
        consumer = session.createConsumer(queue);
    }

    /**
     * This creates an SLA manager listener that can have its queue set.
     *
     * @param queueName The queue name to use
     * @throws JMSException
     * @throws NamingException
     */
    public SlaManagerListener(String queueName) throws JMSException, NamingException {
        super();
        queue = getMessageQueue(queueName);
        consumer = session.createConsumer(queue);
    }

    /**
     * This is the main run method of the SLA manager's action listener.
     */
    @Override
    public void run() {
        try {
            // Wait for a message
            while (running) {
                Message message = consumer.receive(1000);

                if (message instanceof TextMessage) {
                    TextMessage textMessage = (TextMessage) message;
                    ViolationMessage violation = (ViolationMessage) converter.fromXML(textMessage.getText());
                    EventData data = EventDataConverter.convertEventData(violation);
                    eventAssessor.assessEvent(data);
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(SlaManagerListener.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (consumer != null) {
                consumer.close();
            }
            close();
        } catch (JMSException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void setEventAssessor(EventAssessor assessor) {
        eventAssessor = assessor;
    }

    @Override
    public EventAssessor getEventAssessor() {
        return eventAssessor;
    }

    @Override
    public void stopListening() {
        running = false;
        close();
    }

}
