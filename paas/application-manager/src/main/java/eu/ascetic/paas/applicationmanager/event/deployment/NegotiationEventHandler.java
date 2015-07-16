package eu.ascetic.paas.applicationmanager.event.deployment;

import java.sql.Timestamp;

import org.apache.log4j.Logger;
import org.custommonkey.xmlunit.XMLUnit;
import org.slasoi.gslam.syntaxconverter.SLASOITemplateRenderer;
import org.slasoi.slamodel.sla.SLATemplate;
import org.springframework.beans.factory.annotation.Autowired;

import eu.ascetic.paas.applicationmanager.amqp.AmqpProducer;
import eu.ascetic.paas.applicationmanager.conf.Configuration;
import eu.ascetic.paas.applicationmanager.dao.DeploymentDAO;
import eu.ascetic.paas.applicationmanager.event.DeploymentEvent;
import eu.ascetic.paas.applicationmanager.model.Agreement;
import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.paas.applicationmanager.model.Dictionary;
import eu.ascetic.paas.applicationmanager.ovf.OVFUtils;
import eu.ascetic.paas.applicationmanager.slam.NegotiationWsClient;
import eu.ascetic.paas.applicationmanager.slam.SLATemplateCreator;
import eu.ascetic.paas.applicationmanager.slam.translator.SlaTranslator;
import eu.ascetic.paas.applicationmanager.slam.translator.SlaTranslatorImplNoOsgi;
import eu.ascetic.utils.ovf.api.OvfDefinition;
import reactor.event.Event;
import reactor.spring.annotation.Consumer;
import reactor.spring.annotation.Selector;

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
 * This POJO reacts to a deployment in NEGOTIATION event.
 * It contacts the SLA Manager. Negotiates the event and moves the state to NEGOTIATIED
 */
@Consumer
public class NegotiationEventHandler {
	private static Logger logger = Logger.getLogger(NegotiationEventHandler.class);
	@Autowired
	protected DeploymentDAO deploymentDAO;
	@Autowired
	protected DeploymentEventService deploymentEventService;
	
	// TODO this method it is not multiprovider... 
	@Selector(value="topic.deployment.status", reactor="@rootReactor")
	public void negotiationProcess(Event<DeploymentEvent> event) throws Exception {
		DeploymentEvent deploymentEvent = event.getData();

		if(deploymentEvent.getDeploymentStatus().equals(Dictionary.APPLICATION_STATUS_NEGOTIATION)) {
			logger.info(" Moving deployment id: " + deploymentEvent.getDeploymentId()  + " to " + Dictionary.APPLICATION_STATUS_NEGOTIATING + " state");
			
			// We need first to read the deployment from the DB:
			Deployment deployment = deploymentDAO.getById(deploymentEvent.getDeploymentId());
						
			deployment.setStatus(Dictionary.APPLICATION_STATUS_NEGOTIATING);
			// We save the changes to the DB
			deploymentDAO.update(deployment);
			
			// We sent the message that the negottiating state starts:
			AmqpProducer.sendDeploymentNegotiatingMessage(deploymentEvent.getApplicationName(), deployment);
			
			if(Configuration.enableSLAM.equals("yes")) {
				// Configuring the XML previously... 
				XMLUnit.setIgnoreWhitespace(true);
				XMLUnit.setIgnoreComments(true);
				XMLUnit.setIgnoreAttributeOrder(true);
				XMLUnit.setNormalizeWhitespace(true);

				// First we create the SLA template from the OVF
				OvfDefinition ovfDefinition = OVFUtils.getOvfDefinition(deployment.getOvf());
				SLATemplate slaTemplate = SLATemplateCreator.generateSLATemplate(ovfDefinition, Configuration.applicationManagerUrl + 
																							    "/application-manager/applications/" + 
																							    deploymentEvent.getApplicationName() + 
																							    "/deployments/" + 
																							    deployment.getId() + 
																							    "/ovf");
				logger.info("Initial SLA Template document: " + slaTemplate);

				// We create a client to the SLAM
				NegotiationWsClient client = new NegotiationWsClient();
				SlaTranslator slaTranslator = new SlaTranslatorImplNoOsgi();
				client.setSlaTranslator(slaTranslator);

				// Then we initiate the Negotiation
				logger.debug("Sending initiateNegotiation SOAP request...");
				String negId = client.initiateNegotiation(Configuration.slamURL, slaTemplate);

				logger.info("  Negotiation ID: " + negId);

				// After the negotiation it is initiated, we get and negotiation ID, we use it to start the actual negotiation process
				logger.debug("Sending negotiate SOAP request...");
				logger.debug("Negotiation ID: " + negId);
				// The generation again of the template it is the only difference between my unit test and the failure I'm seeing
				slaTemplate = SLATemplateCreator.generateSLATemplate(ovfDefinition, Configuration.applicationManagerUrl + 
					    															"/application-manager/applications/" + 
					    															deploymentEvent.getApplicationName() + 
					    															"/deployments/" + 
					    															deployment.getId() + 
					    															"/ovf");
				
				SLATemplate[] slats = client.negotiate(Configuration.slamURL, slaTemplate, negId);

				//					SLASOITemplateRenderer rend = new SLASOITemplateRenderer();
				//					String xmlRetSlat = rend.renderSLATemplate(slats[0]); // TODO I need to retrieve a list of negotiations
				//					logger.debug("SLA Template:");
				//					logger.debug(xmlRetSlat);
				
				logger.info("Deployment: " + deployment + " id: " + deployment.getId()); 
				logger.info(" agreements: " + deployment.getAgreements());
				
				// New Y2 - We store all the templates in the database
				storeTemplatesInDB(slats, negId, deployment);
			}
			
			System.out.println("#### <- 3");
			deployment.setStatus(Dictionary.APPLICATION_STATUS_NEGOTIATIED);
			System.out.println("#### <- 4");
			deploymentEvent.setDeploymentStatus(deployment.getStatus());
			
			System.out.println("#### <- 5");
			logger.info("about to save to the db");
			
			// We save the changes to the DB
			deploymentDAO.update(deployment);
			
			// We sent the message that the negottiated state starts:
			AmqpProducer.sendDeploymentNegotiatedMessage(deploymentEvent.getApplicationName(), deployment);
			
			//We notify that the deployment has been modified
			deploymentEventService.fireDeploymentEvent(deploymentEvent);
		}
	}
	
	//TODO add the price estimation here and multiprovider
	protected void storeTemplatesInDB(SLATemplate[] slats, String negotiationId, Deployment deployment) throws Exception {
		logger.info("SLATS: " + slats);
		logger.info("Deployment: " + deployment + " agreements: " + deployment.getAgreements());
	
		
		if(slats != null) {
			
			logger.info("slats length: " + slats.length);
			logger.info("Time: " +  Long.parseLong(Configuration.slaAgreementExpirationTime));

			// Calculating when the agreement it is going to expire
			// TODO this needs to be converted into multiprovider
			long  minutesToExpire = Long.parseLong(Configuration.slaAgreementExpirationTime);
			long timeToExpire = System.currentTimeMillis() + minutesToExpire * 60* 1000;
			
			logger.info("Time To Expire: " + timeToExpire);
			
			for(int i=0; i<slats.length; i++) {
				logger.info("Reading the " + i + " SLAT");
				Agreement agreement = new Agreement(); 
				agreement.setAccepted(false);
				agreement.setDeployment(deployment);
				
				logger.info("Agreement craeted for SLAT " + i);
				
				SLASOITemplateRenderer rend = new SLASOITemplateRenderer();
				String xmlRetSlat = rend.renderSLATemplate(slats[i]);
				
				logger.info("Agreement rendered for SLAT " + i);
				logger.info(xmlRetSlat);
				
				agreement.setSlaAgreement(xmlRetSlat);
				agreement.setNegotiationId(negotiationId);
				agreement.setSlaAgreementId(slats[i].getUuid().getValue());
				agreement.setOrderInArray(i);
				agreement.setValidUntil(new Timestamp(timeToExpire));
				
				logger.info("agreement " + agreement + " neg id " + agreement.getNegotiationId());
				
				deployment.addAgreement(agreement);
				
				logger.info("Deployment Id: " + deployment.getId());
				
				System.out.println("#### <- 1");
			}
			
			System.out.println("#### <- 2");
			//deploymentDAO.update(deployment);
		}
	}
}
