package eu.ascetic.test.paas.applicationmanager.amqp;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;

import com.google.common.collect.EvictingQueue;

import eu.ascetic.paas.applicationmanager.amqp.model.ApplicationManagerMessage;
import eu.ascetic.paas.applicationmanager.model.converter.ModelConverter;
import eu.ascetic.test.conf.Configuration;

/**
 * 
 * Copyright 2016 ATOS SPAIN S.A. 
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
 *  
 * List listener, it stores a determined collection of messages in a queue list.
 */
public class AppManagerAmqpListListener implements MessageListener {
	private static Logger logger = Logger.getLogger(AppManagerAmqpListListener.class);
	protected EvictingQueue<AppManagerAmqpMessage> messageQueue;
	
	public AppManagerAmqpListListener() {
		logger.info("Creating a message listener with a max size of: " + Configuration.queueSize);
		messageQueue = EvictingQueue.create(Configuration.queueSize);
	}

	@Override
	public void onMessage(Message message) {
		TextMessage textMessage = (TextMessage) message;
		
		try {
			String destination = textMessage.getJMSDestination().toString();
			
			if(destination.startsWith("APPLICATION.")) {
				AppManagerAmqpMessage newMessage = new AppManagerAmqpMessage();
				ApplicationManagerMessage appMessage = ModelConverter.jsonToApplicationManagerMessage(textMessage.getText());
				
				newMessage.setApplicationId(appMessage.getApplicationId());
				newMessage.setDeploymentId(appMessage.getDeploymentId());
				newMessage.setStatus(appMessage.getStatus());
				
				messageQueue.add(newMessage);
			}
			
		} catch(JMSException e) {
			logger.info("Error parsing message.");
			logger.info(e.getMessage());
			e.printStackTrace(System.out);
		}
	}
	
	public boolean contains(AppManagerAmqpMessage message) {
		return messageQueue.contains(message);
	}
}
