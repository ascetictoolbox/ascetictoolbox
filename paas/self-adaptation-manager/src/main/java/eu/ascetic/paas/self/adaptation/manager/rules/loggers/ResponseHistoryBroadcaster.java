/**
 * Copyright 2016 University of Leeds
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
package eu.ascetic.paas.self.adaptation.manager.rules.loggers;

import eu.ascetic.paas.self.adaptation.manager.activemq.ActiveMQBase;
import eu.ascetic.paas.self.adaptation.manager.activemq.actuator.ActionRequester;
import eu.ascetic.paas.self.adaptation.manager.rules.datatypes.Response;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.NamingException;

/**
 * The aim of this class is to broadcast adaptation events as they are decided,
 * this allows other components to be made aware of these changes and in turn
 * adapt as they require (i.e. fosters collaboration). It is only intended as a
 * notification mechanism and is not intended to directly cause the desired
 * response (That is the role of an actuator).
 *
 * @author Richard Kavanagh
 */
public class ResponseHistoryBroadcaster extends ActiveMQBase implements Runnable {

    private final MessageProducer producer;
    private final Destination messageQueue;
    private final LinkedBlockingDeque<Response> queue = new LinkedBlockingDeque<>();
    private boolean stop = false;

    /**
     * This constructs a response history broadcaster.
     *
     * @param queueName The name of the queue to publish messages upon.
     * @throws JMSException
     * @throws NamingException
     */
    public ResponseHistoryBroadcaster(String queueName) throws JMSException, NamingException {
        connection = getConnection();
        // Create a Session
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        // Create the destination (Topic or Queue)
        messageQueue = getTopic(queueName);
        producer = getMessageProducer(session, messageQueue);
    }

    /**
     * This broadcasts responses by the PaaS SAM to ActiveMQ.
     *
     * @param response The response item to broadcast.
     */
    public void broadcastChange(Response response) {
        if (response != null) {
            queue.add(response);
        }
    }

    /**
     * This write a message to the Self-Adaptation manager's queue to indicate
     * that an adaptation action is going to occur.
     *
     * @param response The response that was decided upon.
     */
    private void broadcastChangeNow(Response response) {
        try {
            MapMessage responseMessage = session.createMapMessage();
            responseMessage.setString("EventTime", response.getTime() + "");
            responseMessage.setString("AppID", response.getApplicationId());
            responseMessage.setString("DeploymentID", response.getDeploymentId());
            responseMessage.setString("VMID", response.getVmId());
            responseMessage.setString("ActionType", response.getActionType().toString());
            responseMessage.setString("Details", response.getAdaptationDetails());
            responseMessage.setString("SLAUUID", response.getCause().getSlaUuid());
            responseMessage.setString("AgreementTerm", response.getCause().getAgreementTerm());
            responseMessage.setDouble("GuranteedValue", response.getCause().getGuranteedValue());
            responseMessage.setDouble("RawValue", response.getCause().getRawValue());
            responseMessage.setString("Operator", response.getCause().getGuranteeOperator().toString());
            producer.send(responseMessage, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
        } catch (JMSException ex) {
            Logger.getLogger(ResponseHistoryBroadcaster.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This is the main run method of the action requester. It waits and submits
     * actions to the self adaptation manager's actuators.
     */
    @Override
    public void run() {
        while (!stop || !queue.isEmpty()) {
            try {
                Response currentItem = queue.poll(30, TimeUnit.SECONDS);
                if (currentItem != null) {
                    ArrayList<Response> responses = new ArrayList<>();
                    responses.add(currentItem);
                    queue.drainTo(responses);
                    for (Response response : responses) {
                        broadcastChangeNow(response);
                    }
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(ActionRequester.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        close();
    }

    /**
     * This permanently stops this broadcaster. It will however broadcast all
     * queued work, before quitting.
     */
    public void stop() {
        this.stop = true;
    }

}
