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
 * Test the POJO Item funcitonality
 * 
 *
 */
public class ItemTest {
	
	@Test
	public void pojoTest() {
		Items items = new Items();
		items.setOffset(1);
		items.setTotal(2);
		
		List<Application> applications = new ArrayList<Application>();
		items.setApplications(applications);
		
		List<Deployment> deployments = new ArrayList<Deployment>();
		items.setDeployments(deployments);
		
		List<VM> vms = new ArrayList<VM>();
		items.setVms(vms);
		
		List<Agreement> agreements = new ArrayList<Agreement>();
		items.setAgreements(agreements);
		
		List<Image> images = new ArrayList<Image>();
		items.setImages(images);
		
		List<EnergySample> energySamples = new ArrayList<EnergySample>();
		items.setEnergySamples(energySamples);
		
		assertEquals(1, items.getOffset());
		assertEquals(2, items.getTotal());
		assertEquals(applications, items.getApplications());
		assertEquals(deployments, items.getDeployments());
		assertEquals(vms, items.getVms());
		assertEquals(agreements, items.getAgreements());
		assertEquals(energySamples, items.getEnergySamples());
	}
	
	@Test
	public void addImage() {
		Items items = new Items();
		assertEquals(null, items.getImages());
		
		Image image = new Image();
		items.addImage(image);
		
		assertEquals(1, items.getImages().size());
		assertEquals(image, items.getImages().get(0));
	}
	
	@Test
	public void addApplicationTest() {
		Items items = new Items();
		assertEquals(null, items.getApplications());
		
		Application application = new Application();
		items.addApplication(application);
		
		assertEquals(1, items.getApplications().size());
		assertEquals(application, items.getApplications().get(0));
	}
	
	@Test
	public void addDeploymentTest() {
		Items items = new Items();
		assertEquals(null, items.getDeployments());
		
		Deployment deployment = new Deployment();
		items.addDeployment(deployment);
		
		assertEquals(1, items.getDeployments().size());
		assertEquals(deployment, items.getDeployments().get(0));
	}
	
	@Test
	public void addVmsTest() {
		Items items = new Items();
		assertEquals(null, items.getVms());
		
		VM vm = new VM();
		items.addVm(vm);
		
		assertEquals(1, items.getVms().size());
		assertEquals(vm, items.getVms().get(0));
	}
	
	@Test
	public void addAgreementTest() {
		Items items = new Items();
		
		assertEquals(null, items.getAgreements());
		
		Agreement agreement = new Agreement();
		items.addAgreement(agreement);
		
		assertEquals(1, items.getAgreements().size());
		assertEquals(agreement, items.getAgreements().get(0));
	}
	
	@Test
	public void addEnergySampleTest() {
		Items items = new Items();
		
		assertEquals(null, items.getEnergySamples());
		
		EnergySample energySample = new EnergySample();
		items.addEnergySample(energySample);
		
		assertEquals(1, items.getEnergySamples().size());
		assertEquals(energySample, items.getEnergySamples().get(0));
	}
}
