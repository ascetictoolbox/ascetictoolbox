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
package eu.ascetic.paas.self.adaptation.manager.activemq.listener;

import com.google.gson.Gson;
import es.bsc.vmmclient.models.SelfAdaptationAction;
import eu.ascetic.paas.self.adaptation.manager.EventListener;
import eu.ascetic.paas.self.adaptation.manager.activemq.ActiveMQBase;
import eu.ascetic.paas.self.adaptation.manager.rules.EventAssessor;
import eu.ascetic.paas.self.adaptation.manager.rules.EventDataConverter;
import eu.ascetic.paas.self.adaptation.manager.rules.datatypes.EventData;
import eu.ascetic.paas.self.adaptation.manager.rules.datatypes.Response;
import eu.ascetic.paas.slam.pac.events.ViolationMessage;
import eu.ascetic.paas.slam.pac.events.ViolationMessageTranslator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.TextMessage;
import javax.naming.NamingException;

/**
 * The aim of this listener is to find changes to the environment that were
 * caused by the Self-adaptation manager in the IaaS layer.
 *
 * @author Richard Kavanagh
 */
public class EventHistoryListener extends ActiveMQBase implements Runnable, EventListener {

    private final Destination queue;
    private static final String QUEUE_NAME = "virtual-machine-manager.self-adaptation";
    // Create a MessageConsumer from the Session to the Topic or Queue
    private final MessageConsumer consumer;
    private EventAssessor eventAssessor;
    private boolean running = true;
    private Gson gson = new Gson();
    private ViolationMessageTranslator converter = new ViolationMessageTranslator();

    /**
     *
     * @throws JMSException
     * @throws NamingException
     */
    public EventHistoryListener() throws JMSException, NamingException {
        super();
        queue = getMessageQueue(QUEUE_NAME);
        consumer = session.createConsumer(queue);
    }

    /**
     *
     * @param user
     * @param password
     * @throws NamingException
     * @throws JMSException
     */
    public EventHistoryListener(String user, String password) throws NamingException, JMSException {
        super(user, password);
        queue = getMessageQueue(QUEUE_NAME);
        consumer = session.createConsumer(queue);
    }

    @Override
    public void run() {
        while (running) {
            try {
                // Wait for a message
                Message message = consumer.receive(1000);

                if (message instanceof TextMessage) {
                    TextMessage textMessage = (TextMessage) message;
                    textMessage.acknowledge();
                    SelfAdaptationAction adaptationAction = parseMessage(textMessage.getText());
                    String slaText = adaptationAction.getSlamMessage();
                    ViolationMessage violation = (ViolationMessage) converter.fromXML(slaText);
                    EventData cause = EventDataConverter.convertEventData(violation);
                    Response response = new Response(null, cause, Response.AdaptationType.REQUEST_VM_CONSOLIDATION);
                    response.setPerformed(adaptationAction.isSuccess());
                    if (adaptationAction.getDeploymentPlan().length >= 1) {
                        //TODO Fix this - This only passes reference to the first VM that was moved.
                        response.setVmId(adaptationAction.getDeploymentPlan()[0].getVmId());
                    }
                    eventAssessor.addRemoteAdaptationEvent(response);
                }
            } catch (JMSException ex) {
                ex.printStackTrace();
            }
        }
        if (consumer != null) {
            try {
                consumer.close();
            } catch (JMSException ex) {
                Logger.getLogger(EventHistoryListener.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        close();
    }

    /**
     * This parses a message from the VMM regarding adaptation events that it is
     * performing.
     *
     * @param message The message to parse.
     * @return The object realisation of the json message
     */
    public SelfAdaptationAction parseMessage(String message) {
        return gson.fromJson(message, SelfAdaptationAction.class);
    }

    /**
     * This parses a message from the VMM regarding adaptation events that it is
     * performing.
     *
     * @param message The object realisation of the json message
     * @return The message in a json format.
     */
    public String generateMessage(SelfAdaptationAction message) {
        return gson.toJson(message, message.getClass());
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
    }

}
