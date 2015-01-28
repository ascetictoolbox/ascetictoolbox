package eu.ascetic.paas.applicationmanager.event.deployment;

import static eu.ascetic.paas.applicationmanager.Dictionary.DEPLOYMENT_EVENT_TOPIC;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import eu.ascetic.paas.applicationmanager.event.DeploymentEvent;
import reactor.core.Reactor;
import reactor.event.Event;

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
 * Test to check that the Deployment service sends the mesages
 *
 */
public class DeploymentServiceTest {

	@SuppressWarnings("rawtypes")
	@Test
	public void testfireDeploymentEvent() {
		Reactor reactor = mock(Reactor.class);
		
		DeploymentEventService service = new DeploymentEventService();
		service.rootReactor = reactor;
		
		DeploymentEvent event = new DeploymentEvent();
		event.setDeploymentId(2);
		event.setDeploymentStatus("XXXX");
		
		service.fireDeploymentEvent(event);
		
		ArgumentCaptor<Event> argument = ArgumentCaptor.forClass(Event.class);
		verify(reactor).notify(eq(DEPLOYMENT_EVENT_TOPIC), argument.capture());
		
		assertEquals(2, ((DeploymentEvent) argument.getValue().getData()).getDeploymentId());
		assertEquals("XXXX", ((DeploymentEvent) argument.getValue().getData()).getDeploymentStatus());
	}
}
