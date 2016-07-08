/**
   Copyright 2014-2015 Hewlett-Packard Development Company, L.P.  
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.test;

import java.util.Date;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.table.VirtualMachine;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.ApplicationRegistry;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.mapper.AppRegistryMapper;

public class AppRegistry {

	private static ApplicationRegistry manager;
	private static AppRegistryMapper mapper;
	
	@BeforeClass
	public static void setup() {
		
		manager = ApplicationRegistry.getRegistry("com.mysql.jdbc.Driver","jdbc:mysql://192.168.0.8:3306/ascetic_paas_em","root","root");		
		mapper = manager.getSession().getMapper(AppRegistryMapper.class);
	}
	
	
	@Test
	public void testCreate() {
		VirtualMachine vm = new VirtualMachine();
		vm.setProviderid("11111");
		vm.setApplicationid("app1");
		vm.setDeploymentid("1");
		vm.setVmid("1");
		vm.setIaasid("iaas1");
		Date date = new Date();
		vm.setStart(date.getTime());
		mapper.createVM(vm);
		
		
		
		VirtualMachine vm2 = new VirtualMachine();
		vm2.setProviderid("00000");
		vm2.setApplicationid("app2");
		vm2.setDeploymentid("2");
		vm2.setVmid("11");
		vm2.setIaasid("iaas2");
		date = new Date();
		vm2.setStart(date.getTime());
		mapper.createVM(vm2);
		
		VirtualMachine vmext = mapper.selectByVmp("11111","app1", "1", "1");
		System.out.println(vmext.toString());
		List<VirtualMachine> machines;
		machines = mapper.selectByApp("app1");
		
		System.out.println(machines.size());
		
		machines = mapper.selectByDeploy("11111","app1","1");
		System.out.println(machines.size());
		
		vm2.setModelid(555);
		vm2.setProfileid(897);
		
		mapper.setModel(vm2);
		mapper.setProfile(vm2);
		
		VirtualMachine reshaped = mapper.selectByIaaSId("iaas2");
		System.out.println(reshaped.getModelid() + reshaped.getProfileid());
		
		date = new Date();
		vm.setStop(date.getTime());
		mapper.stopVM(vm);
		reshaped = mapper.selectByIaaSId("iaas1");
		System.out.println(reshaped.getStop());
	}
}
