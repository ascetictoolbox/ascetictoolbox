package eu.ascetic.paas.applicationmanager.vmmanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.Vm;

public class VmManagerUtilsTest {

	@Test
	public void voidGetOvfIdTest() {
		
		List<Vm> vms = new ArrayList<Vm>();
		
		Vm vm1 = new Vm();
		vm1.setInstanceName("name1");
		vm1.setOvfId("ovf1");
		vms.add(vm1);
		
		Vm vm2 = new Vm();
		vm2.setInstanceName("name2");
		vm2.setOvfId("ovf2");
		vms.add(vm2);
		
		String ovfId = VmManagerUtils.getOvfID("name2", vms);
		
		assertEquals("ovf2", ovfId);
		
		ovfId = VmManagerUtils.getOvfID("name3", vms);
		
		assertNull(ovfId);
	}
}
