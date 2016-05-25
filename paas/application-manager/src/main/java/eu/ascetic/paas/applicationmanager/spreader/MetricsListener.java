package eu.ascetic.paas.applicationmanager.spreader;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import eu.ascetic.amqp.client.AmqpMessageReceiver;
import eu.ascetic.paas.applicationmanager.amqp.AmqpProducer;
import eu.ascetic.paas.applicationmanager.amqp.model.ApplicationManagerMessage;
import eu.ascetic.paas.applicationmanager.amqp.model.VM;
import eu.ascetic.paas.applicationmanager.dao.VMDAO;
import eu.ascetic.paas.applicationmanager.model.converter.ModelConverter;
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
	private String providerId;
	private AmqpMessageReceiver receiver;
	@Autowired
	protected VMDAO vmDAO;
	
	public MetricsListener(String amqpUrl, String topic, String providerId) {
		this.topic = topic;
		this.providerId = providerId;
		
		if(amqpUrl != null && topic != null) {
			// We parse the URL
			amqpUrlParser(amqpUrl);
			
			try {
				// We setup the listener
				// Topic has to have this format: vm.*.item.<metric_name>
				receiver = new AmqpMessageReceiver(host, user, password,  topic, true);
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
        	String destination = textMessage.getJMSDestination().toString();
        	logger.debug("Message received for destionation: " + destination);
        	String text = (String) textMessage.getText();
        	logger.debug("Text Message: " + text);
        	
        	IaaSMessage im = Converter.iaasMessageFromJSONToObject(text);
        	
        	logger.debug("DESTINATION: " + destination);
        	String vmID = destination.split("\\.")[1];
        	
        	eu.ascetic.paas.applicationmanager.model.VM vmFromDB = vmDAO.getVMWithProviderVMId(vmID, providerId);
        	
        	if(vmFromDB != null) {
        		String topicToSend = "APPLICATION." + vmFromDB.getDeployment().getApplication().getName() +
        				".DEPLOYMENT." + vmFromDB.getDeployment().getId() +
        				".VM." + vmFromDB.getId() +
        				".METRIC." + im.getName();

        		VM vm = new VM();
        		vm.setIaasVmId(vmID);
        		vm.setMetricName(im.getName());
        		vm.setUnits(im.getUnits());
        		vm.setValue(im.getValue());
        		vm.setTimestamp(im.getTimestamp());
        		vm.setVmId("" + vmFromDB.getId());
        		vm.setProviderId(providerId);

        		logger.debug("creó el nuevo mensaje");

        		ApplicationManagerMessage amM = new ApplicationManagerMessage();
        		amM.setApplicationId(vmFromDB.getDeployment().getApplication().getName());
        		amM.setDeploymentId("" + vmFromDB.getDeployment().getId());
        		amM.addVM(vm);


        		logger.debug("Message to be sent: " + ModelConverter.applicationManagerMessageToJSON(amM));

        		// We send the created message //TODO change this topic... 
        		AmqpProducer.sendMessage(topicToSend, amM);
        	}
        	
        } catch (JMSException e) {
            logger.info("Error receiving message from: " + host + " to the topic: " + topic);
            logger.info(e.getMessage());
        }
	}
	
	public void close() throws JMSException {
		receiver.close();
	}
}
