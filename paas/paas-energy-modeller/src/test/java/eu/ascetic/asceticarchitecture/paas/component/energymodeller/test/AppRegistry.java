package eu.ascetic.asceticarchitecture.paas.component.energymodeller.test;

import java.util.Date;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.ApplicationRegistry;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.mapper.AppRegistryMapper;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.mapper.VirtualMachine;

public class AppRegistry {

	private static ApplicationRegistry manager;
	private static AppRegistryMapper mapper;
	
	@BeforeClass
	public static void setup() {
		manager = ApplicationRegistry.getRegistry("com.mysql.jdbc.Driver","jdbc:mysql://10.15.5.55:3306/ascetic_paas_em","root","root");
		mapper = manager.getSession().getMapper(AppRegistryMapper.class);
	}
	
	
	@Test
	public void testCreate() {
		VirtualMachine vm = new VirtualMachine();
		vm.setApp_id("123app");
		vm.setDeploy_id(234);
		vm.setVm_id(345);
		vm.setIaas_id("ab013-11");
		Date date = new Date();
		vm.setStart(date.getTime());
		mapper.createVM(vm);
		
		
		
		VirtualMachine vm2 = new VirtualMachine();
		vm2.setApp_id("123app");
		vm2.setDeploy_id(234);
		vm2.setVm_id(890);
		vm2.setIaas_id("ab013-12");
		date = new Date();
		vm2.setStart(date.getTime());
		mapper.createVM(vm2);
		
		VirtualMachine vmext = mapper.selectByVmp("123", 234, 345);
		System.out.println(vmext.toString());
		List<VirtualMachine> machines;
		machines = mapper.selectByApp("123");
		
		System.out.println(machines.size());
		
		machines = mapper.selectByDeploy("123",234);
		
		System.out.println(machines.size());
		
		vm2.setModel_id(555);
		vm2.setProfile_id(897);
		
		mapper.setModel(vm2);
		mapper.setProfile(vm2);
		
		VirtualMachine reshaped = mapper.selectByIaaSId("ab013-12");
		System.out.println(reshaped.getModel_id() + reshaped.getProfile_id());
		
		date = new Date();
		vm.setStop(date.getTime());
		mapper.stopVM(vm);
		reshaped = mapper.selectByIaaSId("ab013-11");
		System.out.println(reshaped.getStop());
	}
}
