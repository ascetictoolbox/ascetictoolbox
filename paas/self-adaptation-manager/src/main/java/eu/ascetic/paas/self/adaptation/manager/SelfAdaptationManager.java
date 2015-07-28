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

import eu.ascetic.paas.self.adaptation.manager.activemq.listener.SlaManagerListener;
import eu.ascetic.paas.self.adaptation.manager.rest.ActionRequester;
import eu.ascetic.paas.self.adaptation.manager.rules.EventAssessor;
import eu.ascetic.paas.self.adaptation.manager.rules.FuzzyEventAssessor;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;
import javax.naming.NamingException;

/**
 * This is the main backbone of the self adaptation manager.
 */
public class SelfAdaptationManager {

    /**
     * TODO populate the list of listeners and actuators. Once is this done,
     * attach the listeners to the decision logic that decides if an actuator
     * should fire or not.
     */
    private ArrayList<EventListener> listeners = new ArrayList<>();
    private ArrayList<ActuatorInvoker> actuators = new ArrayList<>();
    private EventAssessor eventAssessor = null;

    /**
     * This creates a new instance of the self-adaptation manager.
     * @throws JMSException
     * @throws NamingException 
     */
    public SelfAdaptationManager() throws JMSException, NamingException {
        try {
            eventAssessor = new FuzzyEventAssessor();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SelfAdaptationManager.class.getName()).log(Level.SEVERE, 
                    "The event assessor rule file was not found", ex);
        }
        EventListener listener = new SlaManagerListener();
        listeners.add(listener);
        actuators.add(new ActionRequester());
        eventAssessor.setActuators(actuators);
        eventAssessor.setListeners(listeners);
    }

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
