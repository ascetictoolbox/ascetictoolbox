package eu.ascetic.paas.applicationmanager.event.deployment;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Test;

import reactor.event.Event;
import eu.ascetic.paas.applicationmanager.dao.DeploymentDAO;
import eu.ascetic.paas.applicationmanager.event.DeploymentEvent;
import eu.ascetic.paas.applicationmanager.model.Deployment;

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
 * Test that verifies the Application Manager reacts well to the event that a new 
 * deployment has been negottiated and accepted. The deployment it is ready for 
 * contextualization.
 */

public class ContextualizationEventHandlerTest {
	
	@Test
	public void testWrongStateDoesNothing() throws Exception {
		DeploymentDAO deploymentDAO = mock(DeploymentDAO.class);
		DeploymentEventService deploymentEventService = mock(DeploymentEventService.class);
		
		ContextualizationEventHandler acceptAgreementEvent = new ContextualizationEventHandler();
		acceptAgreementEvent.deploymentDAO = deploymentDAO;
		acceptAgreementEvent.deploymentEventService = deploymentEventService;
		
		DeploymentEvent deploymentEvent = new DeploymentEvent();
		deploymentEvent.setDeploymentId(22);
		deploymentEvent.setDeploymentStatus("1111");
		
		acceptAgreementEvent.contextualizeImagesOfADeployment(Event.wrap(deploymentEvent));
		
		verify(deploymentDAO, never()).getById(deploymentEvent.getDeploymentId());
		verify(deploymentDAO, never()).update(any(Deployment.class));
		verify(deploymentEventService, never()).fireDeploymentEvent(any(DeploymentEvent.class));
	}
	
//  TODO needs to implement this method, to have unit test that verifies the interaction between the 
//	     VMContextualizer and Application Manager. 
//	@Test
//	public void testChangeState() {
//		
//	}
}
