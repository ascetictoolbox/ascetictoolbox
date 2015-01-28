package eu.ascetic.paas.applicationmanager.event.reactor;

import org.springframework.beans.factory.annotation.Autowired;

import reactor.event.Event;
import reactor.spring.annotation.Consumer;
import reactor.spring.annotation.Selector;

/**
 * 
 * Copyright 2014 ATOS SPAIN S.A. 
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
 * Test to check the configuration of the internal event services
 *
 */
@Consumer
public class HandlerBean {
	
//	@Autowired
//	@Qualifier("rootReactor")
//	private Reactor rootReactor;
	
	@Autowired
	protected TestService testService;

	@Selector(value="test.topic", reactor="@rootReactor")
	public void handleTestTopic(Event<String> evt) {

		String event = evt.getData();
		System.out.println("Getting the event - " + event);
		
		//rootReactor.notify("test.topic2", Event.wrap("otro event!!!"));
		testService.fireEvent2("otro event!!!");
	}
}
