package eu.ascetic.paas.applicationmanager.amqp.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

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
 * Test the corrrect behaviour of the pojo class VM
 * 
 */
public class VMTest {

	@Test
	public void pojo() {
		VM vm = new VM();
		vm.setIaasMonitoringVmId("aaa");
		vm.setIaasVmId("bbb");
		vm.setOvfId("ccc");
		vm.setStatus("ddd");
		vm.setVmId("xxx");
		
		assertEquals("aaa", vm.getIaasMonitoringVmId());
		assertEquals("bbb", vm.getIaasVmId());
		assertEquals("ccc", vm.getOvfId());
		assertEquals("ddd", vm.getStatus());
		assertEquals("xxx", vm.getVmId());
	}
}
