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

import eu.ascetic.paas.applicationmanager.dao.ApplicationDAO;
import eu.ascetic.paas.applicationmanager.dao.ImageDAO;
import eu.ascetic.paas.applicationmanager.model.Application;
import eu.ascetic.paas.applicationmanager.model.Image;

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
 * @author: David Garcia Perez. Ato#s Research and Innovation, Atos SPAIN SA
 * e-mail david.garciaperez@atos.net 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/application-manager-db-JPA-test-context.xml")
public class ImageDAOJpaTest extends AbstractTransactionalJUnit4SpringContextTests {
	@Autowired
	protected ImageDAO imageDAO;
	@Autowired
	protected ApplicationDAO applicationDAO;
	
	@Test
	public void notNull() {
		if(imageDAO == null) fail();
	}
	
	@Test
	public void saveGetAll() {
		int size = imageDAO.getAll().size();
		
		Image image = new Image();
		image.setOvfId("ovf-id");
		image.setProviderImageId("provider-image-id");
		image.setDemo(true);
		image.setOvfHref("ovf-href");
		image.setProviderId("provider-id");
		
		boolean saved = imageDAO.save(image);
		assertTrue(saved);
		
		List<Image> images = imageDAO.getAll();
		size = size + 1;
		assertEquals(size, images.size());
		
		Image imageFromDatabase = images.get(size-1);
		assertEquals("ovf-id", imageFromDatabase.getOvfId());
		assertEquals("provider-image-id", imageFromDatabase.getProviderImageId());
		assertEquals("provider-id", imageFromDatabase.getProviderId());
		assertTrue(imageFromDatabase.isDemo());
		assertEquals("ovf-href", image.getOvfHref());
	}
	
	@Test
	public void getById() {
		int size = imageDAO.getAll().size();
		
		Image image = new Image();
		image.setOvfId("ovf-id");
		image.setProviderImageId("provider-image-id");
		
		boolean saved = imageDAO.save(image);
		assertTrue(saved);
			
		Image imageFromDatabase = imageDAO.getAll().get(size);
		int id = imageFromDatabase.getId();
		imageFromDatabase = imageDAO.getById(id);
		assertEquals("ovf-id", imageFromDatabase.getOvfId());
		assertEquals("provider-image-id", imageFromDatabase.getProviderImageId());
		
		Image nullImage = imageDAO.getById(30000);
		assertEquals(null, nullImage);
	}
	
	@Test
	public void delete() {
		int size = imageDAO.getAll().size();
		
		Image image = new Image();
		image.setOvfId("ovf-id");
		image.setProviderImageId("provider-image-id");
		
		boolean saved = imageDAO.save(image);
		assertTrue(saved);
		
		Image imageFromDatabase = imageDAO.getAll().get(size);
		int id = imageFromDatabase.getId();
		
		boolean deleted = imageDAO.delete(imageFromDatabase);
		assertTrue(deleted);
		
		deleted = imageDAO.delete(imageFromDatabase);
		assertTrue(!deleted);
		
		imageFromDatabase = imageDAO.getById(id);
		assertEquals(null, imageFromDatabase);
	}
	
	@Test
	public void update() {
		int size = imageDAO.getAll().size();
		
		Image image = new Image();
		image.setOvfId("ovf-id");
		image.setProviderImageId("provider-image-id");
		
		boolean saved = imageDAO.save(image);
		assertTrue(saved);
		
		Image imageFromDatabase = imageDAO.getAll().get(size);
		int id = imageFromDatabase.getId();
		assertEquals("ovf-id", imageFromDatabase.getOvfId());
		
		imageFromDatabase.setOvfId("ovf-id2");

		boolean updated = imageDAO.update(imageFromDatabase);
		assertTrue(updated);
		
		imageFromDatabase = imageDAO.getById(id);
		assertEquals("ovf-id2", imageFromDatabase.getOvfId());
	}
	
	@Test
	public void getLastImageWithOvfIdTest() {
		int size = imageDAO.getAll().size();
		
		Image image1 = new Image();
		image1.setOvfId("ovf-id");
		image1.setProviderImageId("provider-image-id");
		
		boolean saved = imageDAO.save(image1);
		assertTrue(saved);
		
		Image image2 = new Image();
		image2.setOvfId("ovf-id");
		image2.setProviderImageId("provider-image-id");
		
		saved = imageDAO.save(image2);
		assertTrue(saved);
		
		Image image3 = new Image();
		image3.setOvfId("ovf-id");
		image3.setProviderImageId("provider-image-id");
		
		saved = imageDAO.save(image3);
		assertTrue(saved);
		Image image3FromDatabase = imageDAO.getAll().get(size+2);
		int id3 = image3FromDatabase.getId();
		
		image3FromDatabase = imageDAO.getLastImageWithOvfId("ovf-id");
		assertEquals(id3, image3FromDatabase.getId());
	}
	
	@Test
	public void getDemoCacheImage() {
		Image image1 = new Image();
		image1.setOvfId("ovf-id1");
		image1.setProviderImageId("provider-image-id1");
		image1.setDemo(true);
		image1.setOvfHref("href1");
		
		boolean saved = imageDAO.save(image1);
		assertTrue(saved);
		
		Image image2 = new Image();
		image2.setOvfId("ovf-id2");
		image2.setProviderImageId("provider-image-id2");
		image2.setDemo(false);
		image2.setOvfHref("href2");
		
		saved = imageDAO.save(image2);
		assertTrue(saved);
		
		Image image3 = new Image();
		image3.setOvfId("ovf-id3");
		image3.setProviderImageId("provider-image-id3");
		image3.setDemo(true);
		image3.setOvfHref("href1");
		
		saved = imageDAO.save(image3);
		assertTrue(saved);
		
		Image image4 = new Image();
		image4.setOvfId("ovf-id1");
		image4.setProviderImageId("provider-image-id4");
		image4.setDemo(true);
		image4.setOvfHref("href4");
		
		Image image5 = new Image();
		image5.setOvfId("ovf-id1");
		image5.setProviderImageId("provider-image-id5");
		image5.setDemo(false);
		image5.setOvfHref("href1");
		
		saved = imageDAO.save(image5);
		assertTrue(saved);
		
		saved = imageDAO.save(image4);
		assertTrue(saved);
		
		Image image = imageDAO.getDemoCacheImage("ovf-id1", "href1");
		assertEquals("provider-image-id1", image.getProviderImageId());
		
		image = imageDAO.getDemoCacheImage("ovf-id6", "href6");
		assertEquals(null, image);
	}
	
	@Test
	public void getCacheImagesForApplicationNotInDB() {
		Application application = new Application();
		
		// We verify that we obtain an empty list
		List<Image> images = imageDAO.getCacheImagesForApplication(application);
		assertEquals(0, images.size());
		
		// We verify that we obtain an empty list
		images = imageDAO.getCacheImagesForApplication(null);
		assertEquals(0, images.size());
	}
	
	@Test
	public void getCacheImagesForApplicationTest() {
		Image image1 = new Image();
		image1.setOvfId("ovf-id1");
		image1.setProviderImageId("provider-image-id1");
		image1.setDemo(true);
		image1.setOvfHref("href1");
		
		boolean saved = imageDAO.save(image1);
		assertTrue(saved);
		
		Image image2 = new Image();
		image2.setOvfId("ovf-id2");
		image2.setProviderImageId("provider-image-id2");
		image2.setDemo(false);
		image2.setOvfHref("href2");
		
		saved = imageDAO.save(image2);
		assertTrue(saved);
		
		Image image3 = new Image();
		image3.setOvfId("ovf-id3");
		image3.setProviderImageId("provider-image-id3");
		image3.setDemo(true);
		image3.setOvfHref("href1");
		
		saved = imageDAO.save(image3);
		assertTrue(saved);
		
		Image image4 = new Image();
		image4.setOvfId("ovf-id1");
		image4.setProviderImageId("provider-image-id4");
		image4.setDemo(true);
		image4.setOvfHref("href4");
		
		Image image5 = new Image();
		image5.setOvfId("ovf-id1");
		image5.setProviderImageId("provider-image-id5");
		image5.setDemo(false);
		image5.setOvfHref("href1");
		
		int size = applicationDAO.getAll().size();
		Application application = new Application();
		application.setName("TestX");
		applicationDAO.save(application);
		application = applicationDAO.getAll().get(size);
		
		application.addImage(image1);
		application.addImage(image2);
		application.addImage(image4);
		
		applicationDAO.update(application);
		
		List<Image> images = imageDAO.getCacheImagesForApplication(application);
		assertEquals(2, images.size());
		assertEquals("href1", images.get(0).getOvfHref());
		assertEquals("href4", images.get(1).getOvfHref());
	}
}

