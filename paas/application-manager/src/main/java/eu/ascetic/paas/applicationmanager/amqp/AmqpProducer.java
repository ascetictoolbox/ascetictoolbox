package eu.ascetic.paas.applicationmanager.amqp;

import org.apache.log4j.Logger;

import eu.ascetic.amqp.client.AmqpMessageProducer;
import eu.ascetic.paas.applicationmanager.amqp.model.ApplicationManagerMessage;
import eu.ascetic.paas.applicationmanager.conf.Configuration;
import eu.ascetic.paas.applicationmanager.model.converter.ModelConverter;

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
 * This class is the responsible of sending messages to the AMQP from the Application Manager
 *
 */
public class AmqpProducer {
	private static Logger logger = Logger.getLogger(AmqpProducer.class);

	/**
	 * It sends a JSON message to the message queue to an specific topic. It reads the configuration from
	 * the Configuration class.
	 * @param topic to where the message it is going to be sent i.e.: application.111.deployment.222
	 * @param message JSON object representation of the message to be sent
	 */
	public static void sendMessage(String topic, ApplicationManagerMessage message) {
		// First we convert the actual message from Object to JSON String
		String messageString = ModelConverter.applicationManagerMessageToJSON(message);
		
		try {
			AmqpMessageProducer producer = new AmqpMessageProducer(Configuration.amqpAddress, Configuration.amqpUsername, Configuration.amqpPassword, topic, true);
			producer.sendMessage(messageString);
			producer.close();
			
			logger.info("Sending message to: " + topic);
			logger.info("Message sent: \n" + messageString);
			
		} catch(Exception e) {
			logger.info("Error trying to send message to the Message Queue for the topic: " + topic);
			logger.info("Message: " + messageString);
			logger.info(e.getStackTrace());
		}
	}
}
