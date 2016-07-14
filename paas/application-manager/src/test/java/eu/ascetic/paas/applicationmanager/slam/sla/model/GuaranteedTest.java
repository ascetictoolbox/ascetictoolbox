package eu.ascetic.paas.applicationmanager.slam.sla.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

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
 * This class is the Unit test that verifies the correct parsing of the SLA AgreementTerm
 */

public class GuaranteedTest {

	@Test
	public void pojo() {
		Guaranteed guaranteed = new Guaranteed();
		Action action = new Action();
		guaranteed.setAction(action);
		State state = new State();
		guaranteed.setState(state);
		Entry entry = new Entry();
		guaranteed.setEntry(entry);
		
		assertEquals(action, guaranteed.getAction());
		assertEquals(state, guaranteed.getState());
		assertEquals(entry, guaranteed.getEntry());
	}
}
