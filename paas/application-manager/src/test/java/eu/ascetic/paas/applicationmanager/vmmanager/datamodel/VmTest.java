package eu.ascetic.paas.applicationmanager.vmmanager.datamodel;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
 * This class is the Unit test that verifies the correct work of the Vm VMManager class for ASCETiC
 */
public class VmTest {

	@Test
	public void equalsTest() {
		Vm vm = new Vm("name", "image", 1, 2, 3, "initScript", "appId");
		
		assertFalse(vm.equals(new Vm()));
		assertTrue(vm.equals(vm));
		assertTrue(vm.equals(new Vm("name", "image", 1, 2, 3, "initScript", "appId")));
		assertFalse(vm.equals(new Vm("name1", "image", 1, 2, 3, "initScript", "appId")));
		assertFalse(vm.equals(new Vm("name", "image1", 1, 2, 3, "initScript", "appId")));
		assertFalse(vm.equals(new Vm("name", "image", 3, 2, 3, "initScript", "appId")));
		assertFalse(vm.equals(new Vm("name", "image", 1, 3, 3, "initScript", "appId")));
		assertFalse(vm.equals(new Vm("name", "image", 1, 2, 4, "initScript", "appId")));
		assertFalse(vm.equals(new Vm("name", "image", 1, 2, 3, "initScript1", "appId")));
		assertFalse(vm.equals(new Vm("name", "image", 1, 2, 3, "initScript", "appId1")));
	}
}
