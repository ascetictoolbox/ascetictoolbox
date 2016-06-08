package eu.ascetic.paas.applicationmanager.model;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

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
 * @author: David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.garciaperez@atos.net 
 * 
 * Unit test to verify the correct functioning of this POJO
 * 
 *
 */
public class DeploymentTest {

	@Test
	public void pojoTest() {
		Deployment deployment = new Deployment();
		deployment.setId(11);
		deployment.setHref("href");
		deployment.setDeploymentName("name");
		deployment.setPrice("provider-id");
		deployment.setStatus("STATUS");
		deployment.setStartDate("aaa");
		deployment.setEndDate("bbb");
		List<Link> links = new ArrayList<Link>();
		deployment.setLinks(links);
		List<VM> vms = new ArrayList<VM>();
		deployment.setVms(vms);
		
		assertEquals(11, deployment.getId());
		assertEquals("href", deployment.getHref());
		assertEquals("provider-id", deployment.getPrice());
		assertEquals("STATUS", deployment.getStatus());
		assertEquals(links, deployment.getLinks());
		assertEquals(vms, deployment.getVms());
		assertEquals("aaa", deployment.getStartDate());
		assertEquals("bbb", deployment.getEndDate());
		assertEquals("name", deployment.getDeploymentName());
		
		// We test the default value first and then we set it to something different
		assertEquals(1, deployment.getSchema());
		deployment.setSchema(3);
		assertEquals(3, deployment.getSchema());
	}
	
	@Test
	public void addLinkTest() {
		Deployment deployment = new Deployment();
		
		assertEquals(null, deployment.getLinks());
		
		Link link = new Link();
		deployment.addLink(link);
		assertEquals(link, deployment.getLinks().get(0));
	}
	
	@Test
	public void addVMTest() {
		Deployment deployment = new Deployment();
		
		assertEquals(null, deployment.getVms());
		
		VM vm = new VM();
		deployment.addVM(vm);
		
		assertEquals(1, deployment.getVms().size());
		assertEquals(vm, deployment.getVms().get(0));
	}
	
	@Test
	public void addAgreementTest() {
		Deployment deployment = new Deployment();
		
		assertEquals(null, deployment.getAgreements());
		
		Agreement agreement = new Agreement();
		deployment.addAgreement(agreement);
		
		assertEquals(1, deployment.getAgreements().size());
		assertEquals(agreement, deployment.getAgreements().get(0));
	}
}
