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

import eu.ascetic.paas.self.adaptation.manager.activemq.actuator.ActionRequester;
import eu.ascetic.paas.self.adaptation.manager.activemq.listener.SlaManagerListener;
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
    ArrayList<EventListener> listeners = new ArrayList<>();
    ArrayList<ActuatorInvoker> actuators = new ArrayList<>();

    /**
     * This creates a new instance of the self-adaptation manager.
     * @throws JMSException
     * @throws NamingException 
     */
    public SelfAdaptationManager() throws JMSException, NamingException {
        listeners.add(new SlaManagerListener());
        actuators.add(new ActionRequester());
        //TODO load the actuator and listeners list in from file.
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
