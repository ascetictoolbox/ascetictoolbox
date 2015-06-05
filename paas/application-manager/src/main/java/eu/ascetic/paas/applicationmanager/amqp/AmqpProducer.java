package eu.ascetic.paas.applicationmanager.amqp;

import org.apache.log4j.Logger;

import eu.ascetic.amqp.client.AmqpMessageProducer;
import eu.ascetic.paas.applicationmanager.amqp.model.ApplicationManagerMessage;
import eu.ascetic.paas.applicationmanager.conf.Configuration;
import eu.ascetic.paas.applicationmanager.model.Application;
import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.paas.applicationmanager.model.Dictionary;
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
	
	public static final String APPLLICATION_PATH = "APPLICATION";
	public static final String DEPLOYMENT_PATH = "DEPLOYMENT";

	/**
	 * It sends a JSON message to the message queue to an specific topic. It reads the configuration from
	 * the Configuration class.
	 * @param topic to where the message it is going to be sent i.e.: application.111.deployment.222
	 * @param message JSON object representation of the message to be sent
	 */
	protected static void sendMessage(String topic, ApplicationManagerMessage message) {
		
		if(Configuration.enableAMQP.equals("yes")) {
		
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
	
	/**
	 * Sends a message for when a new application it is added to the DB
	 * @param application Application that has just been added to the DB.
	 */
	public static void sendNewApplicationMessage(Application application) {
		// We create the message to be sent
		ApplicationManagerMessage amMessage = new ApplicationManagerMessage();
		amMessage.setApplicationId(application.getName());
		
		AmqpProducer.sendMessage(APPLLICATION_PATH + "." + application.getName() + "." + "ADDED", amMessage);
	}
	
	/**
	 * Sends the message that a new deployment has been submitted
	 * @param application from which to format the message
	 */
	public static void sendDeploymentSubmittedMessage(Application application) {
		ApplicationManagerMessage amMessage = MessageCreator.fromApplication(application);
		
		AmqpProducer.sendMessage(APPLLICATION_PATH + "." + application.getName() + "." 
		                         + DEPLOYMENT_PATH + "." + application.getDeployments().get(0).getId() + "." 
				                 + Dictionary.APPLICATION_STATUS_SUBMITTED, 
								 amMessage);
	}
	
	/**
	 * Sends the message that an application enters the NEGOTIATING state
	 * @param applicationName
	 * @param deployment
	 */
	public static void sendDeploymentNegotiatingMessage(String applicationName, Deployment deployment) {
		ApplicationManagerMessage amMessage = MessageCreator.fromDeployment(applicationName, deployment);
		
		AmqpProducer.sendMessage(APPLLICATION_PATH + "." + applicationName + "." 
                                 + DEPLOYMENT_PATH + "." + deployment.getId() + "." 
                                 + Dictionary.APPLICATION_STATUS_NEGOTIATING, 
                                 amMessage);
	}
	
	/**
	 * Sends the message that an application exits the NEGOTIATING state
	 * @param applicationName
	 * @param deployment
	 */
	public static void sendDeploymentNegotiatedMessage(String applicationName, Deployment deployment) {
		ApplicationManagerMessage amMessage = MessageCreator.fromDeployment(applicationName, deployment);
		
		AmqpProducer.sendMessage(APPLLICATION_PATH + "." + applicationName + "." 
                                 + DEPLOYMENT_PATH + "." + deployment.getId() + "." 
                                 + Dictionary.APPLICATION_STATUS_NEGOTIATIED, 
                                 amMessage);
	}
	
	/**
	 * Sends the message that an application enters the NEGOTIATING state
	 * @param applicationName
	 * @param deployment
	 */
	public static void sendDeploymentContextualizingMessage(String applicationName, Deployment deployment) {
		ApplicationManagerMessage amMessage = MessageCreator.fromDeployment(applicationName, deployment);
		
		AmqpProducer.sendMessage(APPLLICATION_PATH + "." + applicationName + "." 
                                 + DEPLOYMENT_PATH + "." + deployment.getId() + "." 
                                 + Dictionary.APPLICATION_STATUS_CONTEXTUALIZING, 
                                 amMessage);
	}
	
	/**
	 * Sends the message that an application enters the NEGOTIATING state
	 * @param applicationName
	 * @param deployment
	 */
	public static void sendDeploymentContextualizedMessage(String applicationName, Deployment deployment) {
		ApplicationManagerMessage amMessage = MessageCreator.fromDeployment(applicationName, deployment);
		
		AmqpProducer.sendMessage(APPLLICATION_PATH + "." + applicationName + "." 
                                 + DEPLOYMENT_PATH + "." + deployment.getId() + "." 
                                 + Dictionary.APPLICATION_STATUS_CONTEXTUALIZED, 
                                 amMessage);
	}
	
	/**
	 * Sends the message that an application enters the DEPLOYING state
	 * @param applicationName
	 * @param deployment
	 */
	public static void sendDeploymentDeployingMessage(String applicationName, Deployment deployment) {
		ApplicationManagerMessage amMessage = MessageCreator.fromDeployment(applicationName, deployment);
		
		AmqpProducer.sendMessage(APPLLICATION_PATH + "." + applicationName + "." 
                                 + DEPLOYMENT_PATH + "." + deployment.getId() + "." 
                                 + Dictionary.APPLICATION_STATUS_DEPLOYING, 
                                 amMessage);
	}
	
	/**
	 * Sends the message that an application exits the NEGOTIATING state
	 * @param applicationName
	 * @param deployment
	 */
	public static void sendDeploymentDeployedMessage(String applicationName, Deployment deployment) {
		ApplicationManagerMessage amMessage = MessageCreator.fromDeployment(applicationName, deployment);
		
		AmqpProducer.sendMessage(APPLLICATION_PATH + "." + applicationName + "." 
                                 + DEPLOYMENT_PATH + "." + deployment.getId() + "." 
                                 + Dictionary.APPLICATION_STATUS_DEPLOYED, 
                                 amMessage);
	}
}
