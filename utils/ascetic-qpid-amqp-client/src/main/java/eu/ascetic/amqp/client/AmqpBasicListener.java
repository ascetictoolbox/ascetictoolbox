package eu.ascetic.amqp.client;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;

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
 * Simple Amqp Listener to be used as example
 */
public class AmqpBasicListener implements MessageListener {
	private static Logger logger = Logger.getLogger(AmqpBasicListener.class);
	private String destination;
	private String message;

	@Override
	public void onMessage(Message message) {
		TextMessage textMessage = (TextMessage) message;
		
		try {
			destination= textMessage.getJMSDestination().toString();
			this.message = textMessage.getText();
		} catch(JMSException e) {
			logger.info("Error parsing message.");
			logger.info(e.getMessage());
			e.printStackTrace(System.out);
		}
	}

	public String getMessage() {
		return message;
	}
	
	public String getDestination() {
		return destination;
	}
}
