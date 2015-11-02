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
package eu.ascetic.utils.paassameventinjectiontool;

import eu.ascetic.paas.self.adaptation.manager.activemq.ActiveMQBase;
import eu.ascetic.paas.slam.pac.events.Value;
import eu.ascetic.paas.slam.pac.events.ViolationMessage;
import eu.ascetic.paas.slam.pac.events.ViolationMessageTranslator;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
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
    private int arrivalInterval = 1;
    private String appId = "sampleApp";
    private String deploymentId = "sampleDep";
    private String slaUuid = "sampleSlaUUID";

    public enum AgreementTerm {

        power_usage_per_app, energy_usage_per_app
    }

    public enum Operator {

        greater_than_or_equals,
        greater_than,
        equals,
        less_than,
        less_than_or_equals
    }

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
        return createViolationMessage(10.0, 11.0, "power_usage_per_app", "greater_than_or_equals");
    }

    /**
     * This creates and sends a text SLA violation message
     *
     * @param value
     * @param guranteedValue
     * @param agreementTerm
     * @param operator
     * @throws Exception
     */
    public void createAndSendViolationMessage(double value, double guranteedValue, String agreementTerm, String operator) throws Exception {
        String textMessage = createViolationMessage(value, guranteedValue, agreementTerm, operator);
        TextMessage message = session.createTextMessage(textMessage);
        producer.send(message);
    }

    /**
     * This creates a custom message to be sent to the PaaS Self-Adaptation
     * Manager
     *
     * @param value The value obtained
     * @param guranteedValue
     * @param agreementTerm
     * @param operator
     * @return
     * @throws Exception
     */
    public String createViolationMessage(double value, double guranteedValue, String agreementTerm, String operator) throws Exception {
        ViolationMessage violationMessage = new ViolationMessage(Calendar.getInstance(), appId, deploymentId);
        ViolationMessage.Alert alert = violationMessage.new Alert();
        alert.setType("violation");
        alert.setSlaUUID(slaUuid);
        Value v = new Value("free", value + "");
        violationMessage.setValue(v);
        alert.setSlaAgreementTerm(agreementTerm);
        ViolationMessage.Alert.SlaGuaranteedState sgs = alert.new SlaGuaranteedState();
        sgs.setGuaranteedId(agreementTerm);
        sgs.setGuaranteedValue(guranteedValue);
        sgs.setOperator(operator);
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
                Thread.sleep(TimeUnit.SECONDS.toMillis(arrivalInterval));
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

    /**
     * @return the arrivalInterval
     */
    public int getArrivalInterval() {
        return arrivalInterval;
    }

    /**
     * @param arrivalInterval the arrivalInterval to set
     */
    public void setArrivalInterval(int arrivalInterval) {
        this.arrivalInterval = arrivalInterval;
    }

    /**
     * @return the appId
     */
    public String getAppId() {
        return appId;
    }

    /**
     * @param appId the appId to set
     */
    public void setAppId(String appId) {
        this.appId = appId;
    }

    /**
     * @return the deploymentId
     */
    public String getDeploymentId() {
        return deploymentId;
    }

    /**
     * @param deploymentId the deploymentId to set
     */
    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    /**
     * @return the slaUuid
     */
    public String getSlaUuid() {
        return slaUuid;
    }

    /**
     * @param slaUuid the slaUuid to set
     */
    public void setSlaUuid(String slaUuid) {
        this.slaUuid = slaUuid;
    }

}
