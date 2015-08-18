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
import eu.ascetic.paas.slam.pac.events.Value;
import eu.ascetic.paas.slam.pac.events.ViolationMessage;
import eu.ascetic.paas.slam.pac.events.ViolationMessageTranslator;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.NamingException;

/**
 * The aim of this class is to generate messages so that the SLA manager event
 * listener can listen for them.
 *
 * @author Richard Kavanagh
 */
public class SLAManagerMessageGenerator extends ActiveMQBase implements Runnable {

    private final MessageProducer producer;
    private final Destination messageQueue;
    private final static String queueName = "paas-slam.monitoring.f28d4719-5f98-4c87-9365-6be602da9a4a.DavidgpTestApp.violationNotified";
    private int messageCount = 10;

    /**
     * This creates a new action requester.
     *
     * @throws JMSException
     * @throws NamingException
     */
    public SLAManagerMessageGenerator() throws JMSException, NamingException {
        connection = getConnection();
        // Create a Session
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        // Create the destination (Topic or Queue)
        messageQueue = getMessageQueue(queueName);
        producer = getMessageProducer(session, messageQueue);
    }

    /**
     * This creates an SLA manager listener that can have its configuration
     * information set.
     *
     * @param user The user name to use
     * @param password the password to use
     * @param url The factory used to lookup the message queue.
     * @param queueName The queue name to use
     * @throws JMSException
     * @throws NamingException
     */
    public SLAManagerMessageGenerator(String user, String password, String url, String queueName) throws JMSException, NamingException {
        super(user, password, url);
        connection = getConnection();
        // Create a Session
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        // Create the destination (Topic or Queue)
        messageQueue = getMessageQueue(queueName);
        producer = getMessageProducer(session, messageQueue);
    }

    /**
     * Creates the default violation message.
     *
     * @return
     * @throws Exception
     */
    public String createViolationMessage() throws Exception {
        return createViolationMessage("sampleApp", "sampleDep", "sampleSlaUUID", 10.0, 11.0);
    }

    /**
     * This creates and sends a text SLA violation message
     * @param appId
     * @param deploymentId
     * @param slaUuid
     * @param value
     * @param guranteedValue
     * @throws Exception 
     */
    public void createAndSendViolationMessage(String appId, String deploymentId, String slaUuid, double value, double guranteedValue) throws Exception {
        String textMessage = createViolationMessage(appId, deploymentId, slaUuid, value, guranteedValue);
        TextMessage message = session.createTextMessage(textMessage);
        producer.send(message);
    }

    /**
     * This creates a custom message to be sent to the PaaS Self-Adaptation
     * Manager
     *
     * @param appId The application ID
     * @param deploymentId The deployment ID
     * @param slaUuid the SLA UUID
     * @param value The value obtained
     * @param guranteedValue
     * @return
     * @throws Exception
     */
    public String createViolationMessage(String appId, String deploymentId, String slaUuid, double value, double guranteedValue) throws Exception {
        ViolationMessage violationMessage = new ViolationMessage(Calendar.getInstance(), appId, deploymentId);
        ViolationMessage.Alert alert = violationMessage.new Alert();
        alert.setType("violation");
        alert.setSlaUUID(slaUuid);
        Value v = new Value("free", value + "");
        violationMessage.setValue(v);
        alert.setSlaAgreementTerm("power_usage_per_app");
        ViolationMessage.Alert.SlaGuaranteedState sgs = alert.new SlaGuaranteedState();
        sgs.setGuaranteedId("power_usage_per_app");
        sgs.setGuaranteedValue(guranteedValue);
        sgs.setOperator("greater_than_or_equals");
        alert.setSlaGuaranteedState(sgs);
        violationMessage.setAlert(alert);
        ViolationMessageTranslator vmt = new ViolationMessageTranslator();
        return vmt.toXML(violationMessage);
    }

    @Override
    public void run() {
        for (int i = 0; i < messageCount; i++) {
            try {
                TextMessage message = session.createTextMessage(createViolationMessage());
                producer.send(message);
            } catch (JMSException ex) {
                Logger.getLogger(SLAManagerMessageGenerator.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(SLAManagerMessageGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(SLAManagerMessageGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        try {
            producer.close();
            close();
        } catch (JMSException ex) {
            Logger.getLogger(SLAManagerMessageGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @return the messageCount
     */
    public int getMessageCount() {
        return messageCount;
    }

    /**
     * @param messageCount the messageCount to set
     */
    public void setMessageCount(int messageCount) {
        this.messageCount = messageCount;
    }

}
