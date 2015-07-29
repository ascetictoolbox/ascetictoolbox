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
package eu.ascetic.paas.self.adaptation.manager.activemq.actuator;

import eu.ascetic.paas.applicationmanager.model.VM;
import eu.ascetic.paas.self.adaptation.manager.ActuatorInvoker;
import eu.ascetic.paas.self.adaptation.manager.activemq.ActiveMQBase;
import eu.ascetic.paas.self.adaptation.manager.rules.datatypes.Response;
import java.util.List;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.NamingException;

/**
 * This requests adaptation actions to be performed in order to perform
 * adaptation.
 *
 * @author Richard Kavanagh
 */
public class ActionRequester extends ActiveMQBase implements Runnable, ActuatorInvoker {

    private final MessageProducer producer;
    private final Destination queue;
    private static final String QUEUE_NAME = "";
    //Rank adaptation?? i.e. 1, consolidate, 2, scale, 3 redeploy ??

    /**
     * This creates a new action requester.
     * @throws JMSException
     * @throws NamingException 
     */
    public ActionRequester() throws JMSException, NamingException {
        connection = getConnection();
        // Create a Session
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        // Create the destination (Topic or Queue)
//        queue = session.createQueue(QUEUE_NAME);
        queue = getMessageQueue(QUEUE_NAME);
        producer = getMessageProducer(session, queue);
    }

    /**
     * This sends a message to the topic of the message queue of the actuator 
     * that performs work on behalf of the self-adaptation manager.
     *
     * @param message text message to be sent.
     * @throws JMSException
     */
    public void sendMessage(String message) throws JMSException {
        TextMessage testMessage = session.createTextMessage(message);
        producer.send(testMessage, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
    }

    /**
     * This is the main run method of the action requester. It waits and submits
     * actions to the self adaptation manager's actuators.
     */
    @Override
    public void run() {
        try {
            // Create a messages
            String text = "Hello world! From: " + Thread.currentThread().getName() + " : " + this.hashCode();
            sendMessage(text);
            // Clean up
            close();
        } catch (JMSException ex) {
            System.out.println("Caught: " + ex);
            ex.printStackTrace();
        }
    }

    @Override
    public List<String> getVmTypesAvailableToAdd(String applicationId, String deploymentId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<String> getVmTypesAvailableToRemove(String applicationId, String deploymentId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public List<Integer> getVmIdsAvailableToRemove(String applicationId, String deploymentId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }    

    @Override
    public int getVMsOfGivenType(List<VM> vms, String type) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addVM(String applicationId, String deploymentId, String ovfId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteVM(String application, String deployment, String vmID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void actuate(Response response) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
