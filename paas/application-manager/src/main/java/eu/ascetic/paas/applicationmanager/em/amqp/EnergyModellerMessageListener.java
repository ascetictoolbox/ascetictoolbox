package eu.ascetic.paas.applicationmanager.em.amqp;

import java.io.StringReader;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.eclipse.persistence.jaxb.UnmarshallerProperties;

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
 * Object that it is going to subscribe to the messages produced by the PaaS EnergyModeller
 *
 */
public class EnergyModellerMessageListener implements MessageListener {
	private static Logger logger = Logger.getLogger(EnergyModellerMessageListener.class);
	private EnergyModellerQueueController controller;
	private String type;
	
	public EnergyModellerMessageListener(EnergyModellerQueueController controller, String type) {
		this.controller = controller;
		this.type = type;
	}

	@Override
	public void onMessage(Message message) {
		TextMessage textMessage = (TextMessage) message;
		
		try {
			JAXBContext jc = org.eclipse.persistence.jaxb.JAXBContextFactory.createContext(new Class[] {EnergyModellerMessage.class}, null);
        
			// Create the Unmarshaller Object using the JAXB Context
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			unmarshaller.setProperty(UnmarshallerProperties.MEDIA_TYPE, "application/json");
			unmarshaller.setProperty(UnmarshallerProperties.JSON_INCLUDE_ROOT, false);
        
			StreamSource jsonSource = new StreamSource(new StringReader(textMessage.getText()));  
			EnergyModellerMessage emMessage = unmarshaller.unmarshal(jsonSource, EnergyModellerMessage.class).getValue();
			
			controller.addMessage(emMessage, type);
			
		} catch(JAXBException exception) {
			logger.info("Error parsing message from Energy Modeller");
			logger.info(exception.getStackTrace());
		} catch(JMSException exception) {
			logger.info("Error reading the queue from PaaS Energy Modeller");
			logger.info(exception.getStackTrace());
		}
	}
}
