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
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.TextMessage;
import javax.naming.NamingException;

/**
 * This listens to the SLA manager as a source of events to monitor for
 * adaptation.
 *
 * @author Richard Kavanagh
 */
public class SlaManagerListener extends ActiveMQBase implements Runnable, EventListener {

    private final Destination queue;
    private static final String QUEUE_NAME = "";
    // Create a MessageConsumer from the Session to the Topic or Queue
    private final MessageConsumer consumer;
    private EventAssessor eventAssessor;

    public SlaManagerListener() throws JMSException, NamingException {
        super();
        queue = getMessageQueue(QUEUE_NAME);
        consumer = session.createConsumer(queue);
    }

    /**
     * This is the main run method of the SLA manager's action listener.
     */
    @Override
    public void run() {
        try {
            // Wait for a message
            Message message = consumer.receive(1000);

            if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                String text = textMessage.getText();
                textMessage.acknowledge();
                System.out.println("Received: " + text);
            } else {
                System.out.println("Received: " + message);
            }
            consumer.close();
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
    
}
