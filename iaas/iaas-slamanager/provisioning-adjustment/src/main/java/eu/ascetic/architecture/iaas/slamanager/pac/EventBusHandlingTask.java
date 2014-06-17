/**
 * Copyright (c) 2008-2010, SLASOI
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of SLASOI nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SLASOI BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * @author         Beatriz Fuentes - fuentes@tid.es
 * @version        $Rev: 304 $
 * @lastrevision   $Date: 2010-12-05 14:45:45 +0100 (Sun, 05 Dec 2010) $
 * @filesource     $URL: https://sla-at-soi.svn.sourceforge.net/svnroot/sla-at-soi/platform/trunk/generic-slamanager/provisioning-adjustment/src/main/java/org/slasoi/gslam/pac/EventBusHandlingTask.java $
 */

/**
 * 
 */
package eu.ascetic.architecture.iaas.slamanager.pac;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.slasoi.common.messaging.MessagingException;
import org.slasoi.common.messaging.pubsub.Channel;
import org.slasoi.common.messaging.pubsub.MessageEvent;
import org.slasoi.common.messaging.pubsub.MessageListener;
import org.slasoi.common.messaging.pubsub.PubSubFactory;
import org.slasoi.common.messaging.pubsub.PubSubManager;
import org.slasoi.gslam.pac.Event;
import org.slasoi.gslam.pac.EventTranslationTask;
import org.slasoi.gslam.pac.EventTranslator;
import org.slasoi.gslam.pac.EventType;
import org.slasoi.gslam.pac.Identifier;
import org.slasoi.gslam.pac.SharedKnowledgePlane;
import org.slasoi.gslam.pac.Task;
import org.slasoi.gslam.pac.config.TaskConfiguration;
import org.slasoi.gslam.pac.events.Message;

import eu.ascetic.architecture.iaas.slamanager.pac.events.ViolationMessage;
import eu.ascetic.architecture.iaas.slamanager.pac.events.ViolationMessageTranslator;

/**
 * @author Beatriz Fuentes (TID)
 * 
 */
public class EventBusHandlingTask extends Task {
	private static final Logger logger = Logger.getLogger(EventBusHandlingTask.class.getName());

	private static final String BUS_PROPERTIES_FILE = "BUS_PROPERTIES_FILE";
	private static final String CHANNEL = "CHANNEL";

	public static String propertiesFile = null;
	private String[] channels = null;
	private PubSubManager pubSubManager = null;
	private String slamId;

	public EventBusHandlingTask(String propertiesFile, String channel) {
		this.propertiesFile = System.getenv("SLASOI_HOME") + propertiesFile;
		this.channels = new String[1];
		this.channels[0] = channel;

		logger.info("new EventBusHandlingTask to listen channel: " + channel);
		logger.info("Bus properties in file: " + this.propertiesFile);

		configureMessaging();
	}

	public EventBusHandlingTask(String propertiesFile, String[] channels) {
		this.propertiesFile = System.getenv("SLASOI_HOME") + propertiesFile;
		this.channels = new String[1];
		this.channels = channels;

		for (String channel : channels)
			logger.info("new EventBusHandlingTask to listen channel: " + channel);

		logger.info("Bus properties in file: " + this.propertiesFile);

		configureMessaging();
	}

	// default constructor to be used in automatic initialization
	public EventBusHandlingTask() {

	}

	public void configure(TaskConfiguration config) {
		logger.debug("EventBusHandlingTask::configure");
		// Sets the bus properties file
		propertiesFile = System.getenv("SLASOI_HOME") + config.getParameters().get(BUS_PROPERTIES_FILE);

		slamId = config.getSlamId();
		// Get channels (one or more)
		ArrayList<String> channelsList = new ArrayList<String>();
		int i = 1;
		String channel = config.getParameters().get(CHANNEL + i);
		logger.debug("Getting property " + CHANNEL + i + ": " + channel);
		while (channel != null) {
			channelsList.add(channel);
			i++;
			channel = config.getParameters().get(CHANNEL + i);
			logger.debug("Getting property " + CHANNEL + i + ": " + channel);
		}

		channels = new String[channelsList.size()];
		channels = channelsList.toArray(channels);

		configureMessaging();
	}

	private void configureMessaging() {

		// Create manager instance
		try {
			pubSubManager = PubSubFactory.createPubSubManager(propertiesFile);
		} catch (MessagingException e) {
			logger.error("PubSubManager could not be created" + e.getMessage());
			return;
		} catch (FileNotFoundException e) {
			logger.error("Properties file " + propertiesFile + " not found");
		} catch (IOException e) {
			logger.error("EventBusHandlingTask, IO error in configureMessaging");
			logger.error(e.getMessage());
		}

		logger.debug("EventBusHandlingTask::configureMessaging.PubSubManager created");

		// Add message listener
		try {
			pubSubManager.addMessageListener(new MonitoringEventListener(), channels);
		} catch (MessagingException e) {
			logger.error("MessageListener could not be added" + e.getMessage());
			return;
		}
		for (String channel : channels) {
			// Channel subscription
			try {
				if (!pubSubManager.isChannel(channel)) {
					pubSubManager.createChannel(new Channel(channel));
					logger.debug("Channel " + channel + " created");
				}
				pubSubManager.subscribe(channel);
				logger.debug("Channel subscription.");
			} catch (MessagingException e) {
				logger.error("Error subscribing to channel " + channel);
				logger.error(e.getMessage());
			}
		}

	}

	public void closeMessaging() throws MessagingException {

		logger.debug("EvenBusHandlingTask: closing Messaging");

		for (String channel : channels) {
			// Unsubscribe from the channel.
			pubSubManager.unsubscribe(channel);

			// Wait some time for message to arrive to the channel.
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// Delete the channel.
			if (pubSubManager.isChannel(channel))
				pubSubManager.deleteChannel(channel);

			// Close the connections.
			logger.info("Closing pubsubManager");
			pubSubManager.close();
		}

	}

	public void finalize() {
		try {
			closeMessaging();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void stop() {
		try {
			closeMessaging();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.stop();
	}

	/**
	 * Event listener
	 * 
	 */
	private class MonitoringEventListener implements MessageListener {

		private Properties properties = new Properties();

		public void processMessage(MessageEvent messageEvent) {
			logger.debug("EventBusHandlingTask:: Received message: " + messageEvent);
			String message = messageEvent.getMessage().getPayload();
			logger.info("Received message: ");
			logger.info(message);
			// Translate message to Java classes
			EventTranslationTask task = agent.getTranslationTask();
			try {
				properties.load(new FileInputStream(propertiesFile));
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if (task != null) {
				Message jMessage = null;

				EventType type = null;

				EventTranslator translator = new ViolationMessageTranslator();

				try {
					if (translator != null)
						jMessage = translator.fromXML(message);
				} catch (Exception e) {
					logger.info("Parsing exception: " + e.getMessage());
					e.printStackTrace();

					// Try with the monitoring message
					type = EventType.MonitoringEventMessage;
					translator = task.getEventTranslator(type);

					try {
						if (translator != null)
							jMessage = translator.fromXML(message);
					} catch (Exception ex) {
						logger.debug("Can not deserialize message");
					}
				}

				if (jMessage != null) {
					ViolationMessage vMessage = (ViolationMessage) jMessage;
					ViolationMessageTranslator violationMessageTranslator = new ViolationMessageTranslator();
					String typeString = vMessage.getAlert().getType();
					if (typeString.equals("violation")) {
						type = EventType.ViolationEvent;
					} else {
						type = EventType.WarningEvent;
					}

					Event event = new Event(type, Identifier.EventBus, jMessage);
					task.addEventType(type);
					logger.debug("Add event to knowledge base:");
					logger.debug(event);
					SharedKnowledgePlane.getInstance(slamId).getStatefulSession().insert(event);
					SharedKnowledgePlane.getInstance(slamId).getStatefulSession().insert(vMessage);
					SharedKnowledgePlane.getInstance(slamId).getStatefulSession().insert(violationMessageTranslator);
					SharedKnowledgePlane.getInstance(slamId).getStatefulSession().fireAllRules();
					agent.notifyEvent(event);
				}
			}
		}
	}

}
