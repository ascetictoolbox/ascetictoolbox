package eu.ascetic.test.paas.applicationmanager.amqp;

import org.apache.log4j.Logger;

import eu.ascetic.amqp.client.AmqpMessageReceiver;
import eu.ascetic.test.conf.Configuration;

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
 * Receiver for all AppManager messages
 */
public class AppManagerAmqpReceiver {
	private static Logger logger = Logger.getLogger(AppManagerAmqpReceiver.class);
	private String url;
	private String user;
	private String password;
	private String queueOrTopicName="APPLICATION.>";
	private AppManagerAmqpListListener listener;
	private AmqpMessageReceiver receiver;

	public AppManagerAmqpReceiver() {
		amqpUrlParser(Configuration.paasActiveMQUrl);
		
		try {
			receiver = new AmqpMessageReceiver(url, user, password, queueOrTopicName, true);
			listener = new AppManagerAmqpListListener();
			receiver.setMessageConsumer(listener);
			
		} catch(Exception e) {
			logger.info("Exception creating listener to PaaS queue");
			logger.info("Exception: " + e.getStackTrace());
		}
		
	}
	
	public void close() {
		try {
			receiver.close();
		} catch(Exception e) {
			logger.info("Exception closing connectuion to PaaS ActiveMQ");
			logger.info("Exception: " + e.getStackTrace());
		}
	}
	
	public boolean contains(AppManagerAmqpMessage message) {
		return listener.contains(message);
	}
	
	private void amqpUrlParser(String amqpUrl) {
		// amqp://guest:guest@iaas-vm-dev:5673
		
		String[] parts = amqpUrl.split("@");
		this.url = parts[1];
		
		parts = parts[0].split(":");
		this.password = parts[2];
		this.user = parts[1].substring(2);
	}
	
	public String getUrl() {
		return url;
	}
	
	public String getUser() {
		return user;
	}
	
	public String getPassword() {
		return password;
	}
}
