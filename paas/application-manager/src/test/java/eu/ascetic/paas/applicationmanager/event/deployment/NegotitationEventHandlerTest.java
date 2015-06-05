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
import eu.ascetic.amqp.client.AmqpMessageReceiver;
import eu.ascetic.paas.applicationmanager.amqp.AbstractTest;
import eu.ascetic.paas.applicationmanager.amqp.AmqpListListener;
import eu.ascetic.paas.applicationmanager.conf.Configuration;
import eu.ascetic.paas.applicationmanager.dao.DeploymentDAO;
import eu.ascetic.paas.applicationmanager.event.DeploymentEvent;
import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.paas.applicationmanager.model.Dictionary;
import eu.ascetic.paas.applicationmanager.model.converter.ModelConverter;

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
public class NegotitationEventHandlerTest extends AbstractTest {

	@Test
	public void testWrongStateDoesNothing() {
		DeploymentDAO deploymentDAO = mock(DeploymentDAO.class);
		DeploymentEventService deploymentEventService = mock(DeploymentEventService.class);
		
		NegotiationEventHandler negotiationEvent = new NegotiationEventHandler();
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
	public void negotiationDisabled() throws Exception {
		// We manually modify the configuration object to be able to use personally for this test
		Configuration.amqpAddress = "localhost:7672";
		Configuration.amqpUsername = "guest";
		Configuration.amqpPassword = "guest";
		
		// We set a listener to get the sent message from the MessageQueue
		AmqpMessageReceiver receiver = new AmqpMessageReceiver(Configuration.amqpAddress, Configuration.amqpUsername, Configuration.amqpPassword,  "APPLICATION.>", true);
		AmqpListListener listener = new AmqpListListener();
		receiver.setMessageConsumer(listener);
		
		Configuration.enableSLAM = "false";
		
		DeploymentDAO deploymentDAO = mock(DeploymentDAO.class);
		DeploymentEventService deploymentEventService = mock(DeploymentEventService.class);
		
		NegotiationEventHandler negotiationEvent = new NegotiationEventHandler();
		negotiationEvent.deploymentDAO = deploymentDAO;
		negotiationEvent.deploymentEventService = deploymentEventService;
		
		DeploymentEvent deploymentEvent = new DeploymentEvent();
		deploymentEvent.setDeploymentId(22);
		deploymentEvent.setDeploymentStatus(Dictionary.APPLICATION_STATUS_NEGOTIATION);
		deploymentEvent.setApplicationName("applicationName");
		
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
		assertEquals("applicationName", argument.getValue().getApplicationName());
		
		// We verify that the right messages were sent to the AMQP
		Thread.sleep(1000l);
		assertEquals(2, listener.getTextMessages().size());
		
		assertEquals("APPLICATION.applicationName.DEPLOYMENT.22.NEGOTIATING", listener.getTextMessages().get(0).getJMSDestination().toString());
		assertEquals("applicationName", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(0).getText()).getApplicationId());
		assertEquals("22", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(0).getText()).getDeploymentId());
		assertEquals("NEGOTIATING", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(0).getText()).getStatus());
		
		assertEquals("APPLICATION.applicationName.DEPLOYMENT.22.NEGOTIATED", listener.getTextMessages().get(1).getJMSDestination().toString());
		assertEquals("applicationName", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(1).getText()).getApplicationId());
		assertEquals("22", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(1).getText()).getDeploymentId());
		assertEquals("NEGOTIATED", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(1).getText()).getStatus());
		
		receiver.close();
	}
}
