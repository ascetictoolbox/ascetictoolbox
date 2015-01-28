package eu.ascetic.paas.applicationmanager.event.deployment;

import org.apache.axis2.AxisFault;
import org.apache.log4j.Logger;
import org.slasoi.gslam.core.negotiation.INegotiation;
import org.slasoi.slamodel.sla.SLA;
import org.slasoi.slamodel.sla.SLATemplate;
import org.springframework.beans.factory.annotation.Autowired;

import eu.ascetic.paas.applicationmanager.conf.Configuration;
import eu.ascetic.paas.applicationmanager.dao.DeploymentDAO;
import eu.ascetic.paas.applicationmanager.event.DeploymentEvent;
import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.paas.applicationmanager.model.Dictionary;
import eu.ascetic.paas.applicationmanager.ovf.OVFUtils;
import eu.ascetic.paas.applicationmanager.slam.SLAMClient;
import eu.ascetic.paas.applicationmanager.slam.SLATemplateCreator;
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
public class NegotiationEvent {
	private static Logger logger = Logger.getLogger(NegotiationEvent.class);
	
	@Autowired
	protected DeploymentDAO deploymentDAO;
	@Autowired
	protected DeploymentEventService deploymentEventService;
	
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
				SLATemplate slaTemplate = SLATemplateCreator.generateSLATemplate(ovfDefinition, "http://10.4.0.16/application-manager" + deployment.getHref() + "/ovf");
				logger.debug("Initial SLA Template document: " + slaTemplate);
				
				try {
					// Then we initiate the Negotiation
					SLAMClient client = new SLAMClient(Configuration.slamURL);
					
					String initiatieNegotiationID = client.initiateNegotiation(slaTemplate);
					logger.info("Negotiation ID: " + initiatieNegotiationID);
					
					// After the negotiation it is initiated, we get and negotiation ID, we use it to start the actual negotiation process
					SLATemplate[] slaTemplates = client.negotiate(initiatieNegotiationID, slaTemplate);
					logger.info("Agreement selected: " + slaTemplates[0]);

					// Then we get a list of possible SLAs
					// Since we only have a provider the first year, we actually accept the first contract (this could be changed)
					SLA slaAgreement = client.createAgreement(initiatieNegotiationID, slaTemplates[0]);
					logger.info("Agreement reached... "  + slaAgreement);
				}
				catch(AxisFault exception) {
					logger.warn("ERROR connecting to PaaS SLAM");
					exception.printStackTrace();
				}
				catch(INegotiation.OperationNotPossibleException exception) {
					logger.warn("ERROR starting the initialization of the Negotiation with the PaaS SLAM");
					exception.printStackTrace();
				}
				catch(INegotiation.OperationInProgressException exception) {
					logger.warn("ERROR trying to negotiate an SLA Negotiation");
					exception.printStackTrace();
				}
				catch(INegotiation.InvalidNegotiationIDException exception) {
					logger.warn("ERROR trying to negotiate an SLA Negotiation");
					exception.printStackTrace();
				}
				catch(INegotiation.SLACreationException exception) {
					logger.warn("ERROR creating the SLA Agreement");
					exception.printStackTrace();
				}
				
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
