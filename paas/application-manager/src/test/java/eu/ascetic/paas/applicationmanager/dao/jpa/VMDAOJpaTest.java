package eu.ascetic.paas.applicationmanager.dao.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eu.ascetic.paas.applicationmanager.dao.VMDAO;
import eu.ascetic.paas.applicationmanager.model.VM;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/application-manager-db-JPA-test-context.xml")
public class VMDAOJpaTest extends AbstractTransactionalJUnit4SpringContextTests {
	@Autowired
	protected VMDAO vmDAO;
	
	@Test
	public void notNull() {
		if(vmDAO == null) fail();
	}
	
	@Test
	public void saveGetAll() {
		int size = vmDAO.getAll().size();
		
		VM vm = new VM();
		vm.setIp("127.0.0.1");
		vm.setOvfId("ovf-id");
		vm.setProviderId("provider-id");
		vm.setProviderVmId("provider-vm-id");
		vm.setSlaAgreement("sla-agreement");
		vm.setStatus("RUNNING");
		
		boolean saved = vmDAO.save(vm);
		assertTrue(saved);
		
		List<VM> vms = vmDAO.getAll();
		size = size + 1;
		assertEquals(size, vms.size());
		
		VM vmFromDatabase = vms.get(size-1);
		assertEquals("RUNNING", vmFromDatabase.getStatus());
		assertEquals("127.0.0.1", vmFromDatabase.getIp());
		assertEquals("ovf-id", vmFromDatabase.getOvfId());
		assertEquals("provider-id", vmFromDatabase.getProviderId());
		assertEquals("provider-vm-id", vmFromDatabase.getProviderVmId());
		assertEquals("sla-agreement", vmFromDatabase.getSlaAgreement());
	}
	
	@Test
	public void getById() {
		int size = vmDAO.getAll().size();
		
		VM vm = new VM();
		vm.setIp("127.0.0.1");
		vm.setOvfId("ovf-id");
		vm.setProviderId("provider-id");
		vm.setProviderVmId("provider-vm-id");
		vm.setSlaAgreement("sla-agreement");
		vm.setStatus("RUNNING");
		
		boolean saved = vmDAO.save(vm);
		assertTrue(saved);
			
		VM vmFromDatabase = vmDAO.getAll().get(size);
		int id = vmFromDatabase.getId();
		vmFromDatabase = vmDAO.getById(id);
		assertEquals("RUNNING", vmFromDatabase.getStatus());
		assertEquals("127.0.0.1", vmFromDatabase.getIp());
		assertEquals("ovf-id", vmFromDatabase.getOvfId());
		assertEquals("provider-id", vmFromDatabase.getProviderId());
		assertEquals("provider-vm-id", vmFromDatabase.getProviderVmId());
		assertEquals("sla-agreement", vmFromDatabase.getSlaAgreement());
		
		VM nullVM = vmDAO.getById(30000);
		assertEquals(null, nullVM);
	}
	
	@Test
	public void delete() {
		int size = vmDAO.getAll().size();
		
		VM vm = new VM();
		vm.setIp("127.0.0.1");
		vm.setOvfId("ovf-id");
		vm.setProviderId("provider-id");
		vm.setProviderVmId("provider-vm-id");
		vm.setSlaAgreement("sla-agreement");
		vm.setStatus("RUNNING");
		
		boolean saved = vmDAO.save(vm);
		assertTrue(saved);
			
		VM vmFromDatabase = vmDAO.getAll().get(size);
		int id = vmFromDatabase.getId();
		
		boolean deleted = vmDAO.delete(vmFromDatabase);
		assertTrue(deleted);
		
		deleted = vmDAO.delete(vmFromDatabase);
		assertTrue(!deleted);
		
		vmFromDatabase = vmDAO.getById(id);
		assertEquals(null, vmFromDatabase);
	}
	
	@Test
	public void update() {
		int size = vmDAO.getAll().size();
		
		VM vm = new VM();
		vm.setIp("127.0.0.1");
		vm.setOvfId("ovf-id");
		vm.setProviderId("provider-id");
		vm.setProviderVmId("provider-vm-id");
		vm.setSlaAgreement("sla-agreement");
		vm.setStatus("RUNNING");
		
		boolean saved = vmDAO.save(vm);
		assertTrue(saved);
		
		VM vmFromDatabase = vmDAO.getAll().get(size);
		int id = vmFromDatabase.getId();
		assertEquals("RUNNING", vmFromDatabase.getStatus());
		
		vmFromDatabase.setStatus("STOPPED");

		boolean updated = vmDAO.update(vmFromDatabase);
		assertTrue(updated);
		
		vmFromDatabase = vmDAO.getById(id);
		assertEquals("STOPPED", vmFromDatabase.getStatus());
	}
}

