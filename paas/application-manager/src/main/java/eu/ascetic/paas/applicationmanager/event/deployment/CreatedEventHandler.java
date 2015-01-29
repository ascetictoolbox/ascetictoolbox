package eu.ascetic.paas.applicationmanager.event.deployment;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import eu.ascetic.paas.applicationmanager.dao.DeploymentDAO;
import eu.ascetic.paas.applicationmanager.event.DeploymentEvent;
import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.paas.applicationmanager.model.Dictionary;

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
 * @author: David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.garciaperez@atos.net 
 * 
 * This POJO reacts to the creation of a deployment. For the moment, this process is
 * automatic, so it just moves it to an NEGOTIATION state
 */
@Consumer
public class CreatedEventHandler {
	private static Logger logger = Logger.getLogger(CreatedEventHandler.class);
	
	@Autowired
	protected DeploymentDAO deploymentDAO;
	@Autowired
	protected DeploymentEventService deploymentEventService;

	@Selector(value="topic.deployment.status", reactor="@rootReactor")
	public void deploymentCreated(Event<DeploymentEvent> event) {

		DeploymentEvent deploymentEvent = event.getData();

		if(deploymentEvent.getDeploymentStatus().equals(Dictionary.APPLICATION_STATUS_SUBMITTED)) {
			logger.info(" Moving deployment id: " + deploymentEvent.getDeploymentId()  + " to NEGOTIATION state");
			
			// We need first to read the deployment from the DB:
			Deployment deployment = deploymentDAO.getById(deploymentEvent.getDeploymentId());
			
			deployment.setStatus(Dictionary.APPLICATION_STATUS_NEGOTIATION);
			deploymentEvent.setDeploymentStatus(deployment.getStatus());
			
			// We save the changes to the DB
			deploymentDAO.update(deployment);
			
			//We notify that the deployment has been modified
			deploymentEventService.fireDeploymentEvent(deploymentEvent);
		}	
	}
}
