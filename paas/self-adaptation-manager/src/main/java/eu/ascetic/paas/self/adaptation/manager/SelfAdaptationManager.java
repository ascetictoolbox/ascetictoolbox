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
package eu.ascetic.paas.self.adaptation.manager;

import eu.ascetic.paas.self.adaptation.manager.activemq.listener.EventHistoryListener;
import eu.ascetic.paas.self.adaptation.manager.activemq.listener.SlaManagerListener;
import eu.ascetic.paas.self.adaptation.manager.rest.ActionRequester;
import eu.ascetic.paas.self.adaptation.manager.rules.AbstractEventAssessor;
import eu.ascetic.paas.self.adaptation.manager.rules.EventAssessor;
import eu.ascetic.paas.self.adaptation.manager.rules.FuzzyEventAssessor;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;
import javax.naming.NamingException;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * This is the main backbone of the self adaptation manager.
 */
public class SelfAdaptationManager {

    private ArrayList<EventListener> listeners = new ArrayList<>();
    private ActuatorInvoker actuator = null;
    private EventAssessor eventAssessor = null;
    private static final String CONFIG_FILE = "paas-self-adaptation-manager.properties";
    private static final String DEFAULT_EVENT_ASSESSOR_PACKAGE
            = "eu.ascetic.paas.self.adaptation.manager.rules";
    private String eventAssessorName = "FuzzyEventAssessor";

    /**
     * This creates a new instance of the self-adaptation manager.
     *
     * @throws JMSException An exception thrown in the event of an ActiveMQ
     * error.
     * @throws NamingException An exception thrown in the event of an ActiveMQ
     * error.
     */
    public SelfAdaptationManager() throws JMSException, NamingException {
        try {
            PropertiesConfiguration config;
            if (new File(CONFIG_FILE).exists()) {
                config = new PropertiesConfiguration(CONFIG_FILE);
            } else {
                config = new PropertiesConfiguration();
                config.setFile(new File(CONFIG_FILE));
            }
            config.setAutoSave(true); //This will save the configuration file back to disk. In case the defaults need setting.
            eventAssessorName = config.getString("paas.self.adaptation.manager.event.assessor", eventAssessorName);
            config.setProperty("paas.self.adaptation.manager.event.assessor", eventAssessorName);
        } catch (ConfigurationException ex) {
            Logger.getLogger(SelfAdaptationManager.class.getName()).log(Level.INFO, "Error loading the configuration of the PaaS Self adaptation manager", ex);
        }
        setEventAssessor(eventAssessorName);
        EventListener listener = new SlaManagerListener();
        EventListener iaasListener = new EventHistoryListener();
        listeners.add(listener);
        listeners.add(iaasListener);
        actuator = new ActionRequester();
        eventAssessor.setActuator(actuator);
        eventAssessor.setListeners(listeners);
    }

    /**
     * This allows the event assessor to be set. Event assessors are used to
     * decide the which form of adaptation to take.
     *
     * @param eventAssessorName The name of the algorithm to set
     */
    public final void setEventAssessor(String eventAssessorName) {
        try {
            try {
                if (!eventAssessorName.startsWith(DEFAULT_EVENT_ASSESSOR_PACKAGE)) {
                    eventAssessorName = DEFAULT_EVENT_ASSESSOR_PACKAGE + "." + eventAssessorName;
                }
                eventAssessor = (EventAssessor) (Class.forName(eventAssessorName).newInstance());
            } catch (ClassNotFoundException ex) {
                if (eventAssessor == null) {
                    eventAssessor = new FuzzyEventAssessor();
                }
                Logger.getLogger(AbstractEventAssessor.class.getName()).log(Level.WARNING, "The decision engine specified was not found");
            } catch (InstantiationException | IllegalAccessException ex) {
                if (eventAssessor == null) {
                    eventAssessor = new FuzzyEventAssessor();
                }
                Logger.getLogger(AbstractEventAssessor.class.getName()).log(Level.WARNING, "The setting of the decision engine did not work", ex);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SelfAdaptationManager.class.getName()).log(Level.SEVERE,
                    "The event assessor rule file was not found", ex);
            System.exit(-1);
        }
    }

    /**
     * This creates a new self-adaptation manager and is the main entry point
     * for the program.
     *
     * @param args The args are not used.
     */
    public static void main(String[] args) {
        try {
            new SelfAdaptationManager();
        } catch (JMSException ex) {
            Logger.getLogger(SelfAdaptationManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NamingException ex) {
            Logger.getLogger(SelfAdaptationManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
