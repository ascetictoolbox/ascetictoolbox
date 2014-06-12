/**
 * Copyright 2012 Hewlett-Packard Development Company, L.P.                
 *                                                                          
 * Licensed under the Apache License, Version 2.0 (the "License");         
 * you may not use this file except in compliance with the License.        
 * You may obtain a copy of the License at                                 
 *                                                                          
 *     http://www.apache.org/licenses/LICENSE-2.0                          
 *                                                                          
 * Unless required by applicable law or agreed to in writing, software     
 * distributed under the License is distributed on an "AS IS" BASIS,       
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and     
 * limitations under the License.                                          
 */

package eu.ascetic.architecture.iaas.pac.events;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.slasoi.common.messaging.Setting;
import org.slasoi.common.messaging.Settings;
import org.slasoi.gslam.core.context.SLAManagerContext.SLAManagerContextException;
import org.slasoi.gslam.core.negotiation.SLARegistry.InvalidUUIDException;
import org.slasoi.gslam.pac.events.Message;
import org.slasoi.slamodel.primitives.STND;
import org.slasoi.slamodel.primitives.UUID;
import org.slasoi.slamodel.sla.SLA;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

import eu.ascetic.architecture.iaas.pac.EventBusHandlingTask;
import eu.ascetic.architecture.iaas.pac.ProvisioningAdjustmentImpl;
import eu.ascetic.architecture.iaas.pac.events.ViolationMessage.Alert;
import eu.ascetic.architecture.iaas.pac.events.ViolationMessage.Alert.SlaGuaranteedState;

public class EventViolationFederationForwardingHandler extends EventHandler {

	private static final Logger logger = Logger.getLogger(EventViolationFederationForwardingHandler.class.getName());

	private String propertiesFile = null;

	public EventViolationFederationForwardingHandler() {
		super();
	}

	public EventViolationFederationForwardingHandler(Message m) {
		super(m);
	}

	@Override
	public void process() {
		logger.debug("INSIDE PROCESS METHOD!");
		this.propertiesFile = EventBusHandlingTask.propertiesFile;
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(propertiesFile));
		} catch (FileNotFoundException e1) {
			logger.debug(e1.getMessage());
		} catch (IOException e1) {
			logger.debug(e1.getMessage());
		}
		ViolationMessage violationMessage = (ViolationMessage) message;
		String typeString = ((ViolationMessage) violationMessage).getAlert().getType();
		String slaId = ((ViolationMessage) violationMessage).getAlert().getSlaUUID();
		UUID uuid = new UUID(slaId);
		UUID[] uuids = new UUID[1];
		uuids[0] = uuid;
		SLA[] slas = null;
		SLA sla = null;
		try {
			slas = ProvisioningAdjustmentImpl.getSLAManagerContext().getSLARegistry().getIQuery().getSLA(uuids);
		} catch (InvalidUUIDException e) {
			logger.debug(e.getMessage());
		} catch (SLAManagerContextException e) {
			logger.debug(e.getMessage());
		}
		if (slas.length != 0) {
			sla = slas[0];
			logger.debug("SLA loaded from SLARegistry:");
			// logger.debug(sla);
			ViolationMessageTranslator violationMessageTranslator = new ViolationMessageTranslator();
			ViolationMessage violationMessageFederation = null;
			if (typeString.equals("violation")) {
				// if forward configuration is disabled, skip this
				// part
				if (properties.getProperty("enable_forward_violation_message").equals("true")) {
					violationMessageFederation = new ViolationMessage(violationMessage.getTime(), violationMessage.getCeeId(), violationMessage.getVsName(), violationMessage.getVmName(),
							violationMessage.getSid());
					violationMessageFederation.setValue(violationMessage.getValue());
					Alert alert = violationMessage.getAlert();
					ViolationMessage.Alert aFederation = violationMessageFederation.new Alert();
					String federationSlaUUID = getFederationSlaId(sla);
					if (federationSlaUUID != null)
						aFederation.setSlaUUID(federationSlaUUID);
					String providerUUID = getProviderUUID(sla);
					if (providerUUID != null) {
						ViolationMessage.Alert.Provider provider = aFederation.new Provider();
						provider.setProviderUUID(providerUUID);
						provider.setSlaUUID(slaId);
						aFederation.setProvider(provider);
					}
					aFederation.setSlaAgreementTerm(alert.getSlaAgreementTerm());
					aFederation.setType(alert.getType());
					SlaGuaranteedState slaG = alert.getSlaGuaranteedState();
					ViolationMessage.Alert.SlaGuaranteedState s = aFederation.new SlaGuaranteedState();
					s.setGuaranteedId(slaG.getGuaranteedId());
					s.setGuaranteedValue(slaG.getGuaranteedValue());
					s.setOperator(slaG.getOperator());
					aFederation.setSlaGuaranteedState(s);
					violationMessageFederation.setAlert(aFederation);

					// forward message to federation channel
					forwardMessage(violationMessageTranslator.toXML(violationMessageFederation));

					System.out.println("Violation Message fowarded:");
					// System.out.println(violationMessage);
				}
			}
		} else {
			logger.debug("Sla not loaded!!!");
		}
	}

	private String getProviderUUID(SLA sla) {
		String providerUUID = sla.getPropertyValue(new STND("ProviderUUid"));
		return providerUUID;
	}

	private String getFederationSlaId(SLA sla) {
		String federationSlaId = sla.getPropertyValue(new STND("FederationSlaId"));
		return federationSlaId;
	}

	public void forwardMessage(String mess) {
		try {
			Settings settings = Settings.createSettings(propertiesFile);
			ConnectionFactory factory = new ConnectionFactory();
			if (settings.getSetting(Setting.amqp_host) != null)
				factory.setHost(settings.getSetting(Setting.amqp_host));
			if (settings.getSetting(Setting.amqp_username) != null)
				factory.setUsername(settings.getSetting(Setting.amqp_username));
			if (settings.getSetting(Setting.amqp_password) != null)
				factory.setPassword(settings.getSetting(Setting.amqp_password));
			if (settings.getSetting(Setting.amqp_virtualhost) != null)
				factory.setVirtualHost(settings.getSetting(Setting.amqp_virtualhost));

			Connection connection = factory.newConnection();
			com.rabbitmq.client.Channel channel = connection.createChannel();

			if (settings.getSetting(Setting.amqp_federation_forwarding_channel) != null) {
				channel.basicPublish(settings.getSetting(Setting.amqp_federation_forwarding_channel), "#", MessageProperties.PERSISTENT_TEXT_PLAIN, mess.getBytes());
				System.out.println(" [x] Sent '" + mess + "'");
			}
			channel.close();
			connection.close();
		} catch (FileNotFoundException e) {
			logger.debug(e.getMessage());
		} catch (IOException e) {
			logger.debug(e.getMessage());
		}
	}

}
