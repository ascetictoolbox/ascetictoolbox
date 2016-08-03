package eu.ascetic.paas.applicationmanager.event.deployment;

import java.util.List;

import org.apache.log4j.Logger;
import org.slasoi.gslam.syntaxconverter.SLASOITemplateParser;
import org.slasoi.slamodel.sla.SLATemplate;
import org.springframework.beans.factory.annotation.Autowired;

import reactor.event.Event;
import reactor.spring.annotation.Consumer;
import reactor.spring.annotation.Selector;
import eu.ascetic.paas.applicationmanager.conf.Configuration;
import eu.ascetic.paas.applicationmanager.dao.AgreementDAO;
import eu.ascetic.paas.applicationmanager.dao.DeploymentDAO;
import eu.ascetic.paas.applicationmanager.event.DeploymentEvent;
import eu.ascetic.paas.applicationmanager.model.Agreement;
import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.paas.applicationmanager.model.Dictionary;
import eu.ascetic.paas.applicationmanager.pm.PriceModellerClient;
import eu.ascetic.paas.applicationmanager.slam.NegotiationWsClient;
import eu.ascetic.paas.applicationmanager.slam.sla.model.SLA;
import eu.ascetic.paas.applicationmanager.slam.translator.SlaTranslator;
import eu.ascetic.paas.applicationmanager.slam.translator.SlaTranslatorImplNoOsgi;

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
 * This POJO reacts to a deployment in NEGOTIATED event.
 * It contacts the PriceModeller, calculates a price and accepts the contract 
 * TODO this should be seriusly udapted during the second year.
 */
@Consumer
public class AcceptAgreementEventHandler {
private static Logger logger = Logger.getLogger(AcceptAgreementEventHandler.class);
	
	@Autowired
	protected DeploymentDAO deploymentDAO;
	@Autowired
	protected AgreementDAO agreementDAO;
	@Autowired
	protected DeploymentEventService deploymentEventService;
	
	/**
	 * Actions to be performed when an agreement is accepted by the user for an
	 * Specific deployment
	 * @param event Deployment event with the information of the deployment to check
	 */
	@Selector(value="topic.deployment.status", reactor="@rootReactor")
	public void acceptAgreement(Event<DeploymentEvent> event) throws Exception {
		DeploymentEvent deploymentEvent = event.getData();
		logger.info("Deployment " + deploymentEvent.getDeploymentId() + " NEGOTIATED, checking if it automatica agreement");
		
		// We need first to read the deployment from the DB:
		Deployment deployment = deploymentDAO.getById(deploymentEvent.getDeploymentId());
		
		PriceModellerClient.getInstance().initializeApplication(deploymentEvent.getApplicationName(), deploymentEvent.getDeploymentId(), deployment.getSchema());

		if(deploymentEvent.getDeploymentStatus().equals(Dictionary.APPLICATION_STATUS_NEGOTIATIED) && deploymentEvent.isAutomaticNegotiation() == true) {
			
			logger.info(" Moving deployment id: " + deploymentEvent.getDeploymentId()  + " to " + Dictionary.APPLICATION_STATUS_NEGOTIATIED + " state");
			
			if(Configuration.enableSLAM.equals("yes")) {
				// We get the list of agreements
				List<Agreement> agreements = deploymentDAO.getById(deploymentEvent.getDeploymentId()).getAgreements();
				
				// We sign the first agreement
				for(Agreement agreement: agreements) {
					if(agreement.getOrderInArray() == 0) {
						SLASOITemplateParser parser = new SLASOITemplateParser();
						SLATemplate slat = parser.parseTemplate(agreement.getSlaAgreement());
						
						// We create a client to the SLAM
						NegotiationWsClient client = new NegotiationWsClient();
						SlaTranslator slaTranslator = new SlaTranslatorImplNoOsgi();
						client.setSlaTranslator(slaTranslator);
						
						logger.debug("Sending create agreement SOAP request...");
						SLA slaAgreement = client.createAgreement(Configuration.slamURL, slat, agreement.getNegotiationId());
						logger.info("SLA:");
						logger.info(slaAgreement);  
						
						// We calculate the new price, since we are not updating the SLATemplate Price I'm doing this here:
						//double price = PriceModellerClient.calculatePrice(1, deployment.getId(), 100.0);
						//deployment.setPrice("" + price);
						
						// TODO uncomment this lines when the slaAgreement it is not null
						// We store the new agreement in the db:
						//SLASOIRenderer rendeder = new SLASOIRenderer();
						//String slaAgreementString = rendeder.renderSLA(slaAgreement);
						
						agreement.setAccepted(true);
						//agreement.setSlaAgreement(slaAgreementString);
						//agreement.setPrice("" + price);
						
						// We set the SLA UUID in the document... 
						deployment = deploymentDAO.getById(deploymentEvent.getDeploymentId());
						deployment.setSlaUUID(slaAgreement.getUuid());
						
						deploymentDAO.update(deployment);
						
						agreementDAO.update(agreement);
				
						finalization(deployment, deploymentEvent);
					}
				}
			} else {
				finalization(deployment, deploymentEvent);
			}
		}
	}
	
	private void finalization(Deployment deployment, DeploymentEvent deploymentEvent) {
		deployment.setStatus(Dictionary.APPLICATION_STATUS_CONTEXTUALIZATION);
		
		deploymentEvent.setDeploymentStatus(deployment.getStatus());
		
		// We save the changes to the DB
		deploymentDAO.update(deployment);
		
		//We notify that the deployment has been modified
		deploymentEventService.fireDeploymentEvent(deploymentEvent);
	}
}
