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
 * @email david.garciaperez@atos.net 
 * 
 * Collection of unit test to verify that the POJO class representing an Application works as expected
 * 
 */
public class ApplicationTest {

	@Test
	public void testPojo() {
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
		
		assertEquals(links, application.getLinks());
		assertEquals(1, application.getId());
		assertEquals("href", application.getHref());
		assertEquals(deployments, application.getDeployments());
		assertEquals("name", application.getName());
		assertEquals(images, application.getImages());
	}
	
	@Test
	public void addLinkTest() {
		Application application = new Application();
		
		assertEquals(null, application.getLinks());
		
		Link link = new Link();
		application.addLink(link);
		assertEquals(link, application.getLinks().get(0));
	}
	
	@Test
	public void addDeploymentTest() {
		Application application = new Application();
		
		assertEquals(null, application.getDeployments());
		
		Deployment deployment = new Deployment();
		application.addDeployment(deployment);
		
		assertEquals(deployment, application.getDeployments().get(0));
	}
	
	@Test
	public void addImageTest() {
		Application application = new Application();
		
		assertEquals(null, application.getImages());
		
		Image image = new Image();
		application.addImage(image);
		
		assertEquals(1, application.getImages().size());
		assertEquals(image, application.getImages().get(0));
	}
}
