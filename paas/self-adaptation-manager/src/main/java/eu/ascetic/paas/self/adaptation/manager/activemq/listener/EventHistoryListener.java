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
 * The aim of this listener is to find changes to the environment that were
 * caused by other actors in the overall environment, i.e. an application
 * auto-scaling.
 *
 * @author Richard Kavanagh
 */
public class EventHistoryListener extends ActiveMQBase implements Runnable, EventListener {

    private final Destination queue;
    private static final String QUEUE_NAME = "";
    // Create a MessageConsumer from the Session to the Topic or Queue
    private final MessageConsumer consumer;
    private EventAssessor eventAssessor;

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
            //TODO finish here
            eventAssessor.addRemoteAdaptationEvent(null);
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
