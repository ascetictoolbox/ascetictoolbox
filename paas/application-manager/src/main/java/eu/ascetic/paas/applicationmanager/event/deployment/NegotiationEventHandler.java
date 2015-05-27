package eu.ascetic.paas.applicationmanager.event.deployment;

import org.apache.log4j.Logger;
import org.slasoi.slamodel.sla.SLA;
import org.slasoi.slamodel.sla.SLATemplate;
import org.springframework.beans.factory.annotation.Autowired;

import eu.ascetic.paas.applicationmanager.conf.Configuration;
import eu.ascetic.paas.applicationmanager.dao.DeploymentDAO;
import eu.ascetic.paas.applicationmanager.event.DeploymentEvent;
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
	public void negotiationProcess(Event<DeploymentEvent> event) {
		DeploymentEvent deploymentEvent = event.getData();

		if(deploymentEvent.getDeploymentStatus().equals(Dictionary.APPLICATION_STATUS_NEGOTIATION)) {
			logger.info(" Moving deployment id: " + deploymentEvent.getDeploymentId()  + " to " + Dictionary.APPLICATION_STATUS_NEGOTIATING + " state");
			
			// We need first to read the deployment from the DB:
			Deployment deployment = deploymentDAO.getById(deploymentEvent.getDeploymentId());
			
			deployment.setStatus(Dictionary.APPLICATION_STATUS_NEGOTIATING);
			// We save the changes to the DB
			deploymentDAO.update(deployment);
			
			if(Configuration.enableSLAM.equals("yes")) {

				// First we create the SLA template from the OVF
				OvfDefinition ovfDefinition = OVFUtils.getOvfDefinition(deployment.getOvf());
				SLATemplate slaTemplate = SLATemplateCreator.generateSLATemplate(ovfDefinition, Configuration.applicationManagerUrl + "/application-manager" + deployment.getHref() + "/ovf");
				logger.debug("Initial SLA Template document: " + slaTemplate);

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
				SLATemplate[] slats = client.negotiate(Configuration.slamURL, slaTemplate, negId);

				//					SLASOITemplateRenderer rend = new SLASOITemplateRenderer();
				//					String xmlRetSlat = rend.renderSLATemplate(slats[0]); // TODO I need to retrieve a list of negotiations
				//					logger.debug("SLA Template:");
				//					logger.debug(xmlRetSlat);


				// New code

				//					SLASOITemplateParser parser = new SLASOITemplateParser();
				//					SLATemplate slat = parser.parseTemplate(slatXml);
				logger.debug("Sending create agreement SOAP request...");
				SLA slaAgreement = client.createAgreement(Configuration.slamURL, slats[0], negId);  // TODO I need to store this somewhere... 
				logger.debug("SLA:");
				logger.debug(slaAgreement);  

			}
			
			deployment.setStatus(Dictionary.APPLICATION_STATUS_NEGOTIATIED);
			deploymentEvent.setDeploymentStatus(deployment.getStatus());
			
			// We save the changes to the DB
			deploymentDAO.update(deployment);
			
			//We notify that the deployment has been modified
			deploymentEventService.fireDeploymentEvent(deploymentEvent);
		}
	}
}
