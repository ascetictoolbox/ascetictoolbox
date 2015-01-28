package eu.ascetic.paas.applicationmanager.event.deployment;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import reactor.event.Event;
import eu.ascetic.paas.applicationmanager.conf.Configuration;
import eu.ascetic.paas.applicationmanager.dao.DeploymentDAO;
import eu.ascetic.paas.applicationmanager.event.DeploymentEvent;
import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.paas.applicationmanager.model.Dictionary;

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
 * Test that verifies the Application Manager reacts well to the event that a 
 * deployment that has been moved to its Negotiation step
 */
public class NegotitationEventTest {

	@Test
	public void testWrongStateDoesNothing() {
		DeploymentDAO deploymentDAO = mock(DeploymentDAO.class);
		DeploymentEventService deploymentEventService = mock(DeploymentEventService.class);
		
		NegotiationEvent negotiationEvent = new NegotiationEvent();
		negotiationEvent.deploymentDAO = deploymentDAO;
		negotiationEvent.deploymentEventService = deploymentEventService;
		
		DeploymentEvent deploymentEvent = new DeploymentEvent();
		deploymentEvent.setDeploymentId(22);
		deploymentEvent.setDeploymentStatus("1111");
		
		negotiationEvent.negotiationProcess(Event.wrap(deploymentEvent));
		
		verify(deploymentDAO, never()).getById(deploymentEvent.getDeploymentId());
		verify(deploymentDAO, never()).update(any(Deployment.class));
		verify(deploymentEventService, never()).fireDeploymentEvent(any(DeploymentEvent.class));
	}
	
	@Test
	public void negotiationDisabled() {
		Configuration.enableSLAM = "false";
		
		DeploymentDAO deploymentDAO = mock(DeploymentDAO.class);
		DeploymentEventService deploymentEventService = mock(DeploymentEventService.class);
		
		NegotiationEvent negotiationEvent = new NegotiationEvent();
		negotiationEvent.deploymentDAO = deploymentDAO;
		negotiationEvent.deploymentEventService = deploymentEventService;
		
		DeploymentEvent deploymentEvent = new DeploymentEvent();
		deploymentEvent.setDeploymentId(22);
		deploymentEvent.setDeploymentStatus(Dictionary.APPLICATION_STATUS_NEGOTIATION);
		
		Deployment deployment = new Deployment();
		deployment.setId(22);
		deployment.setStatus("UNKNOWN");
		
		when(deploymentDAO.getById(22)).thenReturn(deployment);
		when(deploymentDAO.update(deployment)).thenReturn(true);
		
		negotiationEvent.negotiationProcess(Event.wrap(deploymentEvent));
		
		ArgumentCaptor<DeploymentEvent> argument = ArgumentCaptor.forClass(DeploymentEvent.class);
		verify(deploymentEventService).fireDeploymentEvent(argument.capture());
		
		assertEquals(22, argument.getValue().getDeploymentId());
		assertEquals(Dictionary.APPLICATION_STATUS_NEGOTIATIED, argument.getValue().getDeploymentStatus());
	}
}
