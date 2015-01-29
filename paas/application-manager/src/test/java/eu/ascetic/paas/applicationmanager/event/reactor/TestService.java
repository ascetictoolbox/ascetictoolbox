package eu.ascetic.paas.applicationmanager.event.reactor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reactor.core.Reactor;
import reactor.event.Event;

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
 * @author David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.garciaperez@atos.net 
 * 
 * Test to check the configuration of the internal event services
 *
 */
@Service
public class TestService {

	@Autowired
	private Reactor rootReactor;


	public void fireEvent(String s) {
		rootReactor.notify("test.topic", Event.wrap(s));
	}

	public void fireEvent2(String s) {
		rootReactor.notify("test.topic2", Event.wrap(s));
	}
}