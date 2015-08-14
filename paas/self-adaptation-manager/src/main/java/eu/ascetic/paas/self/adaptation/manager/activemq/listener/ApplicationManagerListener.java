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

import eu.ascetic.paas.self.adaptation.manager.activemq.ActiveMQBase;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
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
 * This listens to the application monitor as a data source.
 *
 * @author Richard Kavanagh
 */
public class ApplicationManagerListener extends ActiveMQBase implements Runnable {

    private HashMap<String, MessageConsumer> consumers = new HashMap<>();
    private HashMap<String, Destination> destinations = new HashMap<>();
    private boolean running = true;
    private static final String CONFIG_FILE = "paas-self-adaptation-manager.properties";

    public ApplicationManagerListener() throws JMSException, NamingException {
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
        } catch (ConfigurationException ex) {
            Logger.getLogger(ApplicationManagerListener.class.getName()).log(Level.INFO, "Error loading the configuration of the PaaS Self adaptation manager", ex);
        }
    }

    /**
     * This creates an application manager listener that can have its
     * configuration information set.
     *
     * @param user The user name to use
     * @param password the password to use
     * @param url The factory used to lookup the message queue.
     * @throws JMSException
     * @throws NamingException
     */
    public ApplicationManagerListener(String user, String password, String url) throws JMSException, NamingException {
        super(user, password, url);
    }

    public String getBusiestVmTypeInApp(String applicationID) {
        String answer = "";
        //TODO get value from store
        return answer;
    }

    public String getLeastBusiestVmInApp(String applicationID) {
        String answer = "";
        //TODO get value from store
        return answer;
    }

    /**
     * This registers an application for listening.
     *
     * @param applicationId The application id to listen for
     */
    public void listenToApp(String applicationId) {
        try {
            if (consumers.containsKey("application-monitor.monitoring." + applicationId + ".measurement")) {
                return;
            }
            Destination destination = getMessageQueue("application-monitor.monitoring." + applicationId + ".measurement");
            destinations.put("application-monitor.monitoring." + applicationId + ".measurement", destination);
            consumers.put("application-monitor.monitoring." + applicationId + ".measurement", session.createConsumer(destination));
        } catch (NamingException ex) {
            Logger.getLogger(ApplicationManagerListener.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JMSException ex) {
            Logger.getLogger(ApplicationManagerListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This is the main run method of the SLA manager's action listener.
     */
    @Override
    public void run() {
        try {
            // Wait for a message
            while (running) {
                for (Map.Entry<String, MessageConsumer> entrySet : consumers.entrySet()) {
                    String appId = entrySet.getKey();
                    MessageConsumer consumer = entrySet.getValue();
                    Message message = consumer.receiveNoWait();

                    if (message != null && message instanceof TextMessage) {
                        TextMessage data = (TextMessage) message;
                        //TODO Parse Data
                        //have fields for latest values.
                    }
                }
            }
            for (MessageConsumer consumer : consumers.values()) {
                if (consumer != null) {
                    consumer.close();
                }
            }
            consumers.clear();
            destinations.clear();
            close();
        } catch (JMSException ex) {
            ex.printStackTrace();
        }
    }

}
