package eu.ascetic.paas.applicationmanager.spreader;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;

import eu.ascetic.amqp.client.AmqpMessageReceiver;
import eu.ascetic.paas.applicationmanager.spreader.model.Converter;
import eu.ascetic.paas.applicationmanager.spreader.model.IaaSMessage;

/**
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
 * Message Listener to each one of the ActiveMQ IaaS messages
 */
public class MetricsListener implements MessageListener {
	private static Logger logger = Logger.getLogger(MetricsListener.class);
	protected String host;
	protected String user;
	protected String password;
	private String topic;
	
	public MetricsListener(String amqpUrl, String topic) {
		this.topic = topic;
		
		if(amqpUrl != null && topic != null) {
			// We parse the URL
			amqpUrlParser(amqpUrl);
			
			try {
				// We setup the listener
				AmqpMessageReceiver receiver = new AmqpMessageReceiver(host, user, password,  topic, true);
				receiver.setMessageConsumer(this);
				
			} catch(Exception ex) {
				logger.info("Impossible to connect to AMQP server: " + amqpUrl + " , to this topic: " + topic);
				logger.info(ex.getMessage());
			}
		}
	}
	
	protected void amqpUrlParser(String amqpUrl) {
		// amqp://guest:guest@iaas-vm-dev:5673
		
		String[] parts = amqpUrl.split("@");
		host = parts[1];
		
		parts = parts[0].split(":");
		password = parts[2];
		user = parts[1].substring(2);
	}
	
	@Override
    public void onMessage(Message message) {
        try {
        	TextMessage textMessage = (TextMessage) message;
        	logger.debug("Message received for destionation: " + textMessage.getJMSDestination());
        	IaaSMessage im = Converter.iaasMessageFromJSONToObject(textMessage.getText());
        	
        	
        } catch (JMSException e) {
            logger.info("Error receiving message from: " + host + " to the topic: " + topic);
            logger.info(e.getMessage());
        }
	}
}
