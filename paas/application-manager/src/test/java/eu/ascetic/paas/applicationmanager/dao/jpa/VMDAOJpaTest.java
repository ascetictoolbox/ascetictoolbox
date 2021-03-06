package eu.ascetic.paas.applicationmanager.dao.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static eu.ascetic.paas.applicationmanager.Dictionary.STATE_VM_DELETED;
import static eu.ascetic.paas.applicationmanager.Dictionary.STATE_VM_ACTIVE;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eu.ascetic.paas.applicationmanager.dao.DeploymentDAO;
import eu.ascetic.paas.applicationmanager.dao.ImageDAO;
import eu.ascetic.paas.applicationmanager.dao.VMDAO;
import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.paas.applicationmanager.model.Image;
import eu.ascetic.paas.applicationmanager.model.VM;

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
 * @author David Garcia Perez. Ato#s Research and Innovation, Atos SPAIN SA
 * e-mail david.garciaperez@atos.net 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/application-manager-db-JPA-test-context.xml")
public class VMDAOJpaTest extends AbstractTransactionalJUnit4SpringContextTests {
	@Autowired
	protected VMDAO vmDAO;
	@Autowired
	protected DeploymentDAO deploymentDAO;
	@Autowired
	protected ImageDAO imageDAO;
	
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
	
	@Test
	public void vmWithImages() {
		int size = vmDAO.getAll().size();
		
		VM vm = new VM();
		vm.setIp("127.0.0.1");
		vm.setOvfId("ovf-id");
		vm.setProviderId("provider-id");
		vm.setProviderVmId("provider-vm-id");
		vm.setSlaAgreement("sla-agreement");
		vm.setStatus("RUNNING");
		
		Image image = new Image();
		image.setProviderImageId("providerId");
		image.setOvfId("ovf-id11");
		
		int sizeImages = imageDAO.getAll().size();
		imageDAO.save(image);
		image = imageDAO.getAll().get(sizeImages);
		
		vm.addImage(image);
		
		boolean saved = vmDAO.save(vm);
		assertTrue(saved);
		
		VM vmFromDatabase = vmDAO.getAll().get(size);
		int id = vmFromDatabase.getId();
		
		assertEquals(1, vmFromDatabase.getImages().size());
		assertEquals("ovf-id11", vmFromDatabase.getImages().get(0).getOvfId());
		
		image = vmFromDatabase.getImages().get(0);
		image.setOvfId("ovf-id22");
		
		boolean updated = vmDAO.update(vmFromDatabase);
		assertTrue(updated);
		
		vmFromDatabase = vmDAO.getById(id);
		assertEquals("ovf-id22", vmFromDatabase.getImages().get(0).getOvfId());
	}
	
	@Test
	public void getVMWithProviderVMIdTest() {
		int sizeDeployments = deploymentDAO.getAll().size();
		Deployment deployment = new Deployment();
		deployment.setStatus("PEPITO");
		deploymentDAO.save(deployment);
		deployment = deploymentDAO.getAll().get(sizeDeployments);
		
		int sizeVMs = vmDAO.getAll().size();
		
		VM vm1 = new VM();
		vm1.setProviderId("1");
		vm1.setProviderVmId("1212");
		deployment.addVM(vm1);
		vm1.setDeployment(deployment);
		vmDAO.save(vm1);
		vm1 = vmDAO.getAll().get(sizeVMs);
		deploymentDAO.update(deployment);
		
		VM vm2 = new VM();
		vm2.setProviderId("2");
		vm2.setProviderVmId("11212");
		deployment.addVM(vm2);
		vm2.setDeployment(deployment);
		vmDAO.save(vm2);
		vm2 = vmDAO.getAll().get(sizeVMs + 1);
		deploymentDAO.update(deployment);
		
		assertEquals(vm1, vmDAO.getVMWithProviderVMId("1212", "1"));
		assertEquals(vmDAO.getVMWithProviderVMId("1212", "1").getDeployment().getId(), deployment.getId());
		
		assertEquals(vm2, vmDAO.getVMWithProviderVMId("11212", "2"));
		assertEquals(vmDAO.getVMWithProviderVMId("11212", "2").getDeployment().getId(), deployment.getId());
		
		assertEquals(null, vmDAO.getVMWithProviderVMId("112212", "22"));
	}
	
	@Test
	public void getNoDeletedVMsWithImage() {
		int size = imageDAO.getAll().size();
		Image image1 = new Image();
		image1.setOvfHref("ovf-href");
		image1.setOvfId("ovf-id");
		image1.setProviderImageId("uuid");
		Image image2 = new Image();
		image2.setOvfHref("ovf-href");
		image2.setOvfId("ovf-id");
		image2.setProviderImageId("uuid");
		
		imageDAO.save(image1);
		image1 = imageDAO.getAll().get(size);
		imageDAO.save(image2);
		image2 = imageDAO.getAll().get(size+1);
		
		VM vm1 = new VM();
		vm1.setIp("127.0.0.1");
		vm1.setOvfId("ovf-id");
		vm1.setProviderId("provider-id");
		vm1.setProviderVmId("provider-vm-id");
		vm1.setSlaAgreement("sla-agreement");
		vm1.setStatus("ACTIVE");
		vm1.addImage(image1);
		
		VM vm2 = new VM();
		vm2.setIp("127.0.0.1");
		vm2.setOvfId("ovf-id");
		vm2.setProviderId("provider-id");
		vm2.setProviderVmId("provider-vm-id");
		vm2.setSlaAgreement("sla-agreement");
		vm2.setStatus("DELETED");
		vm2.addImage(image1);
		
		VM vm3 = new VM();
		vm3.setIp("127.0.0.1");
		vm3.setOvfId("ovf-id");
		vm3.setProviderId("provider-id");
		vm3.setProviderVmId("provider-vm-id");
		vm3.setSlaAgreement("sla-agreement");
		vm3.setStatus("DELETED");
		vm3.addImage(image1);
		
		VM vm4 = new VM();
		vm4.setIp("127.0.0.1");
		vm4.setOvfId("ovf-id");
		vm4.setProviderId("provider-id");
		vm4.setProviderVmId("provider-vm-id");
		vm4.setSlaAgreement("sla-agreement");
		vm4.setStatus("DELETED");
		vm4.addImage(image2);
		
		vmDAO.save(vm1);
		vmDAO.save(vm2);
		vmDAO.save(vm3);
		vmDAO.save(vm4);
		
		List<VM> vms1 = vmDAO.getNotDeletedVMsWithImage(image1);
		List<VM> vms2 = vmDAO.getNotDeletedVMsWithImage(image2);
		
		assertEquals(1, vms1.size());
		assertEquals(0, vms2.size());
	}
	
	@Test
	public void getNumberOfVMsWithOVfIdForDeploymentNotDeletedTest() {
		// Prior
		Deployment deployment1 = new Deployment();
		deployment1.setStatus("RUNNING");
		deployment1.setPrice("expensive");

		VM vm1 = new VM();
		vm1.setIp("127.0.0.1");
		vm1.setOvfId("ovf-id2");
		vm1.setProviderId("provider-id");
		vm1.setProviderVmId("provider-vm-id");
		vm1.setSlaAgreement("sla-agreement");
		vm1.setStatus("RUNNING");

		VM vm2 = new VM();
		vm2.setIp("127.0.0.1");
		vm2.setOvfId("ovf-id");
		vm2.setProviderId("provider-id");
		vm2.setProviderVmId("provider-vm-id");
		vm2.setSlaAgreement("sla-agreement");
		vm2.setStatus("RUNNING");
		
		VM vm3 = new VM();
		vm3.setIp("127.0.0.1");
		vm3.setOvfId("ovf-id1");
		vm3.setProviderId("provider-id");
		vm3.setProviderVmId("provider-vm-id");
		vm3.setSlaAgreement("sla-agreement");
		vm3.setStatus("RUNNING");

		deployment1.addVM(vm1);
		deployment1.addVM(vm2);
		deployment1.addVM(vm3);
		
		Deployment deployment2 = new Deployment();
		deployment2.setStatus("RUNNING");
		deployment2.setPrice("expensive");

		VM vm4 = new VM();
		vm4.setIp("127.0.0.1");
		vm4.setOvfId("ovf-id");
		vm4.setProviderId("provider-id");
		vm4.setProviderVmId("provider-vm-id");
		vm4.setSlaAgreement("sla-agreement");
		vm4.setStatus("RUNNING");

		VM vm5 = new VM();
		vm5.setIp("127.0.0.1");
		vm5.setOvfId("ovf-id");
		vm5.setProviderId("provider-id");
		vm5.setProviderVmId("provider-vm-id");
		vm5.setSlaAgreement("sla-agreement");
		vm5.setStatus("RUNNING");
		
		VM vm6 = new VM();
		vm6.setIp("127.0.0.1");
		vm6.setOvfId("ovf-id1");
		vm6.setProviderId("provider-id");
		vm6.setProviderVmId("provider-vm-id");
		vm6.setSlaAgreement("sla-agreement");
		vm6.setStatus("RUNNING");
		
		VM vm7 = new VM();
		vm7.setIp("127.0.0.1");
		vm7.setOvfId("ovf-id");
		vm7.setProviderId("provider-id");
		vm7.setProviderVmId("provider-vm-id");
		vm7.setSlaAgreement("sla-agreement");
		vm7.setStatus(STATE_VM_DELETED);

		deployment2.addVM(vm4);
		deployment2.addVM(vm5);
		deployment2.addVM(vm6);
		deployment2.addVM(vm7);
		
		int size = deploymentDAO.getAll().size();
		
		boolean saved = deploymentDAO.save(deployment1);
		assertTrue(saved);
		Deployment deploymentFromDatabase = deploymentDAO.getAll().get(size);
		int deploymentId1 = deploymentFromDatabase.getId();
		saved = deploymentDAO.save(deployment2);
		deploymentFromDatabase = deploymentDAO.getAll().get(size + 1);
		int deploymentId2 = deploymentFromDatabase.getId();
		assertTrue(saved);
		
		// Test
		List<VM> vms = vmDAO.getVMsWithOVfIdForDeploymentNotDeleted("ovf-id", deploymentId2);
		assertEquals(2, vms.size());
		vms = vmDAO.getVMsWithOVfIdForDeploymentNotDeleted("ovf-id", deploymentId1);
		assertEquals(1, vms.size());
	}
	
	@Test
	public void getVMsWithOvfIdAndActive() {
		// Prior
		Deployment deployment1 = new Deployment();
		deployment1.setStatus("RUNNING");
		deployment1.setPrice("expensive");
		
		VM vm1 = new VM();
		vm1.setOvfId("ovfid1");
		vm1.setStatus(STATE_VM_ACTIVE);
		
		VM vm2 = new VM();
		vm2.setOvfId("ovfid1");
		vm2.setStatus(STATE_VM_ACTIVE);
		
		VM vm3 = new VM();
		vm3.setOvfId("ovfid1");
		vm3.setStatus(STATE_VM_DELETED);
		
		VM vm4 = new VM();
		vm4.setOvfId("ovfid2");
		vm4.setStatus(STATE_VM_DELETED);
		
		VM vm5 = new VM();
		vm5.setOvfId("ovfid1");
		vm5.setStatus("whatever");
		
		deployment1.addVM(vm1);
		deployment1.addVM(vm2);
		deployment1.addVM(vm3);
		deployment1.addVM(vm4);
		deployment1.addVM(vm5);
		
		deploymentDAO.save(deployment1);
		
		List<VM> vms = vmDAO.getVMsWithOvfIdAndActive(deployment1.getId(), "ovfid1");
		
		assertEquals(2, vms.size());
		assertEquals(vm1, vms.get(0));
		assertEquals(vm2, vms.get(1));
		
		 vms = vmDAO.getVMsWithOvfIdAndActive(deployment1.getId(), "ovfid2");
			
		 assertEquals(0, vms.size());
	}
}

