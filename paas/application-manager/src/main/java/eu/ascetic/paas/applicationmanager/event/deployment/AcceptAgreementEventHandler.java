package eu.ascetic.paas.applicationmanager.event.deployment;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import reactor.event.Event;
import reactor.spring.annotation.Consumer;
import reactor.spring.annotation.Selector;
import eu.ascetic.paas.applicationmanager.dao.DeploymentDAO;
import eu.ascetic.paas.applicationmanager.event.DeploymentEvent;
import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.paas.applicationmanager.model.Dictionary;
import eu.ascetic.paas.applicationmanager.pm.PriceModellerClient;

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
	protected DeploymentEventService deploymentEventService;
	
	/**
	 * Actions to be performed when an agreement is accepted by the user for an
	 * Specific deployment
	 * @param event Deployment event with the information of the deployment to check
	 */
	@Selector(value="topic.deployment.status", reactor="@rootReactor")
	public void acceptAgreement(Event<DeploymentEvent> event) {
		DeploymentEvent deploymentEvent = event.getData();

		if(deploymentEvent.getDeploymentStatus().equals(Dictionary.APPLICATION_STATUS_NEGOTIATIED)) {
			logger.info(" Moving deployment id: " + deploymentEvent.getDeploymentId()  + " to " + Dictionary.APPLICATION_STATUS_NEGOTIATIED + " state");
			
			// We need first to read the deployment from the DB:
			Deployment deployment = deploymentDAO.getById(deploymentEvent.getDeploymentId());
			
			// We calculate the new price, since we are not updating the SLATemplate Price I'm doing this here:
			double price = PriceModellerClient.calculatePrice(1, deployment.getId(), 100.0);
			
			// Since we are not doing this right now, we move the application to the next step
			deployment.setStatus(Dictionary.APPLICATION_STATUS_CONTEXTUALIZATION);
			deployment.setPrice("" + price);
			deploymentEvent.setDeploymentStatus(deployment.getStatus());
			
			// We save the changes to the DB
			deploymentDAO.update(deployment);
			
			//We notify that the deployment has been modified
			deploymentEventService.fireDeploymentEvent(deploymentEvent);
		}
	}
}
