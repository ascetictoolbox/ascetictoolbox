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
		// M. Fontanella - 05 Feb 2016 - begin
		manager = ApplicationRegistry.getRegistry("com.mysql.jdbc.Driver","jdbc:mysql://192.168.0.8:3306/ascetic_paas_em","root","root");
		// M. Fontanella - 05 Feb 2016 - end
		mapper = manager.getSession().getMapper(AppRegistryMapper.class);
	}
	
	
	@Test
	public void testCreate() {
		VirtualMachine vm = new VirtualMachine();
		// M. Fontanella - 05 Feb 2016 - begin
		// M. Fontanella - 20 Jan 2016 - begin 
		vm.setProviderid("11111");
		// M. Fontanella - 20 Jan 2016 - end
		vm.setApplicationid("app1");
		// M. Fontanella - 10 Feb 2016 - begin
		vm.setDeploymentid("1");
		vm.setVmid("1");
		// M. Fontanella - 10 Feb 2016 - end
		vm.setIaasid("iaas1");
		// M. Fontanella - 05 Feb 2016 - end
		Date date = new Date();
		vm.setStart(date.getTime());
		mapper.createVM(vm);
		
		
		
		VirtualMachine vm2 = new VirtualMachine();
		// M. Fontanella - 05 Feb 2016 - begin
		// M. Fontanella - 20 Jan 2016 - begin 
		vm2.setProviderid("00000");
		// M. Fontanella - 20 Jan 2016 - end
		vm2.setApplicationid("app2");
		// M. Fontanella - 10 Feb 2016 - begin
		vm2.setDeploymentid("2");
		vm2.setVmid("11");
		// M. Fontanella - 10 Feb 2016 - end
		vm2.setIaasid("iaas2");
		// M. Fontanella - 05 Feb 2016 - end
		date = new Date();
		vm2.setStart(date.getTime());
		mapper.createVM(vm2);
		
		// M. Fontanella - 05 Feb 2016 - begin
		// M. Fontanella - 20 Jan 2016 - begin
		// M. Fontanella - 10 Feb 2016 - begin
		VirtualMachine vmext = mapper.selectByVmp("11111","app1", "1", "1");
		// M. Fontanella - 10 Feb 2016 - end
		// M. Fontanella - 20 Jan 2016 - end
		// M. Fontanella - 05 Feb 2016 - end
		System.out.println(vmext.toString());
		List<VirtualMachine> machines;
		// M. Fontanella - 05 Feb 2016 - begin
		machines = mapper.selectByApp("app1");
		// M. Fontanella - 05 Feb 2016 - end
		
		System.out.println(machines.size());
		
		// M. Fontanella - 05 Feb 2016 - begin
		machines = mapper.selectByDeploy("app1","1");
		// M. Fontanella - 05 Feb 2016 - end
		
		System.out.println(machines.size());
		
		vm2.setModelid(555);
		vm2.setProfileid(897);
		
		mapper.setModel(vm2);
		mapper.setProfile(vm2);
		
		// M. Fontanella - 05 Feb 2016 - begin
		VirtualMachine reshaped = mapper.selectByIaaSId("iaas2");
		// M. Fontanella - 05 Feb 2016 - end
		System.out.println(reshaped.getModelid() + reshaped.getProfileid());
		
		date = new Date();
		vm.setStop(date.getTime());
		mapper.stopVM(vm);
		// M. Fontanella - 05 Feb 2016 - begin
		reshaped = mapper.selectByIaaSId("iaas1");
		// M. Fontanella - 05 Feb 2016 - end
		System.out.println(reshaped.getStop());
	}
}
