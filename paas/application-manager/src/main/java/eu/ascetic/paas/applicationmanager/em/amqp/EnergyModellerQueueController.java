package eu.ascetic.paas.applicationmanager.em.amqp;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.jms.JMSException;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import eu.ascetic.amqp.client.AmqpMessageReceiver;
import eu.ascetic.paas.applicationmanager.conf.Configuration;

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
 * Controller that it is going to listen to all AMQP messages from the
 * PaaS Energy Modeller and keep a record of the necessary messages
 * for other components.
 *
 */
@Service("EnergyModellerQueueService")
public class EnergyModellerQueueController {
	public static final String WATT = "WATT";
	public static final String WATTHOUR = "WATTHOUR";
	public static final String COUNT = "COUNT";
	public static final String SEC = "SEC";
	public static final String APP_DURATION = "APP_DURATION"; // Davide: the average duration of events per application as a whole 
	public static final String APP_COUNT = "APP_COUNT"; // Davide: is total per application including all its vms 
	public static final String MEASUREMENTS = "MEASUREMENTS";
	public static final String PREDICTIONS = "PREDICTIONS";
	protected static int MAX_ENTRIES_CACHE = 100;
	private static Logger logger = Logger.getLogger(EnergyModellerQueueController.class);
	private Map<String, EnergyModellerMessage> measurementMessages;
	private Map<String, EnergyModellerMessage> predictionsMessages;
	private EnergyModellerMessageListener emListenerMeasurements;
	private EnergyModellerMessageListener emListenerPredictions;
	private AmqpMessageReceiver receiverMeasurements;
	private AmqpMessageReceiver receiverPredictions;
	
	public EnergyModellerQueueController() {
		logger.info("Starting service EnergyModellerQueueController...........................................");
		// It is necessary to create two different queues to store the messages
		measurementMessages = EnergyModellerQueueController.createLRUMap(MAX_ENTRIES_CACHE);
		predictionsMessages = EnergyModellerQueueController.createLRUMap(MAX_ENTRIES_CACHE);
		
		// We get from the configuration the parameters to subscribe to the ActiveMQ message queue topics and
		// We create two listeners to them.
		try {
			receiverMeasurements = new AmqpMessageReceiver(Configuration.amqpAddress, Configuration.amqpUsername, Configuration.amqpPassword,  Configuration.emMeasurementsTopic, true);
			emListenerMeasurements = new EnergyModellerMessageListener(this, MEASUREMENTS);
			receiverMeasurements.setMessageConsumer(emListenerMeasurements);
			
			receiverPredictions = new AmqpMessageReceiver(Configuration.amqpAddress, Configuration.amqpUsername, Configuration.amqpPassword,  Configuration.emMeasurementsTopic, true);
			emListenerPredictions = new EnergyModellerMessageListener(this, PREDICTIONS);
			receiverPredictions.setMessageConsumer(emListenerPredictions);
			
		} catch (Exception e) {
			logger.info("Error trying to create connector to PaaS ActiveMQ for Energy Modeller messages");
			logger.info(e.getStackTrace());
		}
	}
	
	@Override 
	protected void finalize() throws JMSException {
		logger.debug("Clossing connections from EnergyModellerQueueController");
		receiverMeasurements.close();
		receiverPredictions.close();
	}

	/**
	 * It adds a message to one of the maps if it is from a valid type
	 * @param emMessage to be added
	 * @param type that needs to be added to a Map
	 */
	public void addMessage(EnergyModellerMessage emMessage, String type) {
		logger.info("Message recieved from PaaS Energy Modeller, type: " + type + " message: " + emMessage);
		if(type != null && type.equals(MEASUREMENTS)) {
			measurementMessages.put(generateKey(emMessage), emMessage);
		} else if (type.equals(PREDICTIONS)) {
			predictionsMessages.put(generateKey(emMessage), emMessage);
		}
 	}
	
	/** 
	 * @param key
	 * @return the measurement message for that key if available, null otherwise
	 */
	public EnergyModellerMessage getMeasurementMessage(String key) {
		return measurementMessages.get(key);
	}
	
	/**
	 * @param key
	 * @return the prediction message for that key if available, null otherwise
	 */
	public EnergyModellerMessage getPredictionMessage(String key) {
		return predictionsMessages.get(key);
	}
	
	/**
	 * It creates a HashMap that has a limit of "maxEntries", when that limit is reached
	 * the oldest entries (by order of adding them to the Map) are deleted.
	 * @param maxEntries maximum number of entries
	 * @return The fixed size LinkedHasMap.
	 */
	public static <K, V> Map<K, V> createLRUMap(final int maxEntries) {
	    return new LinkedHashMap<K, V>(maxEntries*10/7, 0.7f, true) {
			private static final long serialVersionUID = -3617640443322851203L;

			@Override
	        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
	            return size() > maxEntries;
	        }
	    };
	}
	
	/**
	 * It generates an unique key for an specific type of message
	 * @param emMessage
	 * @return the String with the key, null if the emMessage object is null.
	 */
	public static String generateKey(EnergyModellerMessage emMessage) {
		
		if(emMessage != null) {
			return generateKey(emMessage.getApplicationid(), emMessage.getEventid(), emMessage.getDeploymentid(), emMessage.getVms(), emMessage.getUnit());
		} else {
			return null;
		}
	}
	
	/**
	 * It generates an unique key for this specific parameters of the message
	 * @param applicationId
	 * @param eventId
	 * @param deploymentId
	 * @param vmsId
	 * @param unit
	 * @return the key to be used for looking for measurements
	 */
	public static String generateKey(String applicationId, String eventId, String deploymentId, List<String> vmsId, String unit) {
		String vms = "";
		
		if(vmsId != null) {
			for(String vmId : vmsId) {
				vms = vms + vmId;
			}
		}
		
		return applicationId + eventId + deploymentId + vms + unit;
	}
}
