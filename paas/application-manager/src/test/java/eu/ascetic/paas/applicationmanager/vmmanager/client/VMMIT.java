package eu.ascetic.paas.applicationmanager.vmmanager.client;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import es.bsc.vmmclient.models.Vm;

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
 * Integration test for the VMM client
 */

public class VMMIT {

	@Test
	public void deployVM() {
		
		String url = "http://localhost:34372/api/v1";
		
		Vm vm = new Vm("MySQL_1", "c88a4442-6f80-4f6e-8269-d4cefc862c07", 1, 1024, 20, 0, "/mnt/cephfs/ascetic/vmc/repository/davidgpTestApp/mysqlA.iso_1", "davidgpTestApp", "mysqlA", "");
		es.bsc.vmmclient.vmm.VmManagerClient vmm = new es.bsc.vmmclient.vmm.VmManagerClient(url);
		List<Vm> vms = new ArrayList<Vm>();
		vms.add(vm);
		
		List<String> vmsString = vmm.deployVms(vms);
		
		for(String id : vmsString) {
			System.out.println("ID " + id);
		}
	}
	
}
