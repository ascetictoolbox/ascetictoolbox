package eu.ascetic.paas.applicationmanager.amqp;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import eu.ascetic.paas.applicationmanager.amqp.model.ApplicationManagerMessage;
import eu.ascetic.paas.applicationmanager.model.Application;
import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.paas.applicationmanager.model.Image;
import eu.ascetic.paas.applicationmanager.model.Link;
import eu.ascetic.paas.applicationmanager.model.VM;

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
 * @author David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.garciaperez@atos.net 
 * 
 * It test the correct behaviour of the MessageCreator class
 *
 */
public class MessageCreatorTest {
	
	@Test
	public void testFromApplication() {
		Application application = new Application();
		application.setHref("href");
		application.setId(1);
		application.setName("name");
		List<Link> links = new ArrayList<Link>();
		application.setLinks(links);
		List<Deployment> deployments = new ArrayList<Deployment>();
		application.setDeployments(deployments);
		List<Image> images = new ArrayList<Image>();
		application.setImages(images);
		
		Deployment deployment1 = new Deployment();
		deployment1.setId(11);
		deployment1.setHref("href");
		deployment1.setPrice("provider-id");
		deployment1.setStatus("STATUS");
		deployment1.setStartDate("aaa");
		deployment1.setEndDate("bbb");
		deployment1.setSlaAgreement("sla");
		List<VM> vms = new ArrayList<VM>();
		deployment1.setVms(vms);
		
		deployments.add(deployment1);
		
		Deployment deployment2 = new Deployment();
		deployment2.setId(22);
		deployment2.setHref("href");
		deployment2.setPrice("provider-id");
		deployment2.setStatus("STATUS2");
		deployment2.setStartDate("aaa");
		deployment2.setEndDate("bbb");
		deployment2.setSlaAgreement("sla");
		
		deployments.add(deployment2);
		
		VM vm1 = new VM();
		vm1.setId(12);
		vm1.setHref("href1");
		vm1.setOvfId("ovfId1");
		vm1.setProviderId("provider-id1");
		vm1.setProviderVmId("provider-vm-id1");
		vm1.setStatus("XXX1");
		vm1.setIp("172.0.0.1");
		vm1.setSlaAgreement("slaAggrementId1");
		
		vms.add(vm1);
		
		VM vm2 = new VM();
		vm2.setId(22);
		vm2.setHref("href2");
		vm2.setOvfId("ovfId2");
		vm2.setProviderId("provider-id2");
		vm2.setProviderVmId("provider-vm-id2");
		vm2.setStatus("XXX2");
		vm2.setIp("172.0.0.12");
		vm2.setSlaAgreement("slaAggrementId2");
		
		vms.add(vm2);
		
		ApplicationManagerMessage amMessage = MessageCreator.fromApplication(application);
		
		assertEquals("name", amMessage.getApplicationId());
		assertEquals("11", amMessage.getDeploymentId());
		assertEquals("STATUS", amMessage.getStatus());
		
		assertEquals("provider-vm-id1", amMessage.getVms().get(0).getIaasVmId());
		assertEquals("ovfId1", amMessage.getVms().get(0).getOvfId());
		assertEquals("XXX1", amMessage.getVms().get(0).getStatus());
		assertEquals("12", amMessage.getVms().get(0).getVmId());
		
		assertEquals("provider-vm-id2", amMessage.getVms().get(1).getIaasVmId());
		assertEquals("ovfId2", amMessage.getVms().get(1).getOvfId());
		assertEquals("XXX2", amMessage.getVms().get(1).getStatus());
		assertEquals("22", amMessage.getVms().get(1).getVmId());
	}
	
	@Test
	public void testFromApplicationNull() {
		ApplicationManagerMessage amMessage = MessageCreator.fromApplication(null);
		assertEquals(null, amMessage);
	}
	
	@Test
	public void testFromApplicationMoreNullTest() {

		Application application1 = new Application();
		application1.setHref("href");
		application1.setId(1);
		application1.setName("name");
		
		ApplicationManagerMessage amMessage = MessageCreator.fromApplication(application1);
		
		assertEquals("name", amMessage.getApplicationId());
		
		Application application2 = new Application();
		application2.setHref("href");
		application2.setId(1);
		application2.setName("name");
		List<Link> links = new ArrayList<Link>();
		application2.setLinks(links);
		List<Deployment> deployments = new ArrayList<Deployment>();
		application2.setDeployments(deployments);
		
		amMessage = MessageCreator.fromApplication(application2);
		
		assertEquals("name", amMessage.getApplicationId());
		
		Application application3 = new Application();
		application3.setHref("href");
		application3.setId(1);
		application3.setName("name");
		application3.setDeployments(null);
		
		amMessage = MessageCreator.fromApplication(application3);
		
		assertEquals("name", amMessage.getApplicationId());
		
		Application application4 = new Application();
		application4.setHref("href");
		application4.setId(1);
		application4.setName("name");
		List<Deployment> deployments4 = new ArrayList<Deployment>();
		application4.setDeployments(deployments4);
		
		Deployment deployment4 = new Deployment();
		deployment4.setId(11);
		deployment4.setHref("href");
		deployment4.setPrice("provider-id");
		deployment4.setStatus("STATUS");
		deployment4.setStartDate("aaa");
		deployment4.setEndDate("bbb");
		deployment4.setSlaAgreement("sla");
		deployment4.setVms(null);
		
		deployments4.add(deployment4);
		
		amMessage = MessageCreator.fromApplication(application4);
		
		assertEquals("name", amMessage.getApplicationId());
	}
}
