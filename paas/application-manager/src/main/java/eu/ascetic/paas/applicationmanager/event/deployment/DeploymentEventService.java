package eu.ascetic.paas.applicationmanager.event.deployment;

import static eu.ascetic.paas.applicationmanager.Dictionary.DEPLOYMENT_EVENT_TOPIC;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.ascetic.paas.applicationmanager.event.DeploymentEvent;
import reactor.core.Reactor;
import reactor.event.Event;
import reactor.spring.annotation.Consumer;

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
 * This is the service used to launch events related to a Deployment
 */

@Service
public class DeploymentEventService {
	private static Logger logger = Logger.getLogger(DeploymentEventService.class);

	@Autowired
	protected Reactor rootReactor;

	public void fireDeploymentEvent(DeploymentEvent event) {
		logger.debug(" Sending internal event for topic: " + DEPLOYMENT_EVENT_TOPIC + " with deployment id: " + event.getDeploymentId() + " with status: " + event.getDeploymentStatus());
		rootReactor.notify(DEPLOYMENT_EVENT_TOPIC, Event.wrap(event));
	}
}
