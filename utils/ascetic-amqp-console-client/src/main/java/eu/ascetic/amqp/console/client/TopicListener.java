package eu.ascetic.amqp.console.client;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.jms.MessageListener;

/**
 * 
 * Copyright 2015 ATOS SPAIN S.A. 
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.garciaperez@atos.net
 * 
 * It subscribes to a topic
 */
public class TopicListener implements MessageListener {

	@Override
    public void onMessage(Message message) {
        try {
        	TextMessage textMessage = (TextMessage) message;
        	System.out.println("   ");
        	System.out.println("####################################");
            System.out.println("   Message received for destination: " + textMessage.getJMSDestination());
            System.out.println("   Message:");
            System.out.println("   ");
            System.out.println(textMessage.getText());
            System.out.println("   ");
        } catch (JMSException e) {
            System.err.println("Caught exception receiving message: " + e);
        }
    }
}
