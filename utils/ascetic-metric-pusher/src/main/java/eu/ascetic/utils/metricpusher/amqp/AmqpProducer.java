package eu.ascetic.utils.metricpusher.amqp;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import eu.ascetic.amqp.client.AmqpMessageProducer;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.Item;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.json.JsonCurrentItem;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.utils.Dictionary;
import eu.ascetic.utils.metricpusher.conf.Configuration;

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
	private static final Gson gson = new GsonBuilder().create();

	/**
	 * It sends a JSON message to the message queue to an specific topic. It reads the configuration from
	 * the Configuration class.
	 * @param topic to where the message it is going to be sent i.e.: application.111.deployment.222
	 * @param message JSON object representation of the message to be sent
	 */
	protected static void sendMessage(String topic, String message) {
		
		if(Configuration.enableAMQP != null && Configuration.enableAMQP.equals("yes")) {

			try {
				AmqpMessageProducer producer = new AmqpMessageProducer(Configuration.amqpAddress, Configuration.amqpUsername, Configuration.amqpPassword, topic, true);
				producer.sendMessage(message);
				producer.close();

				logger.info("Sending message to: " + topic);
				logger.info("Message sent: \n" + message);

			} catch(Exception e) {
				logger.info("Error trying to send message to the Message Queue for the topic: " + topic);
				logger.info("Message: " + message);
				logger.info(e.getStackTrace());
			}
		}
	}
	public static void sendCurrentValueForItemInHostMessage(String hostId, String item, double value, String units, 
			long lastClock) {
		
		JsonCurrentItem jsonMessage = new JsonCurrentItem(hostId);
		jsonMessage.setName(item);
		jsonMessage.setValue(value);
		jsonMessage.setUnits(units);
		jsonMessage.setTimestamp(lastClock);
		
		//Remove the _host from hostId string which is allocated at the final of the String
		String[] aux = hostId.split(Configuration.hostFilterBegins);
		String vmId = aux[0];
		
		//publish the message
		String mpMessage = gson.toJson(jsonMessage);
		
		//AmqpProducer.sendMessage("vm." + vmId + ".item." + item, mpMessage);
		System.out.println("Sending message to: " + "vm." + vmId + ".item." + item);
		System.out.println("Message sent: \n" + mpMessage);
	}

}
