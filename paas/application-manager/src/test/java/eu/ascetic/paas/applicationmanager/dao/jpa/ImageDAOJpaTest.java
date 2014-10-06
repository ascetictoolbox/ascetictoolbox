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

import eu.ascetic.paas.applicationmanager.dao.ImageDAO;
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
 * @email david.garciaperez@atos.net 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/application-manager-db-JPA-test-context.xml")
public class ImageDAOJpaTest extends AbstractTransactionalJUnit4SpringContextTests {
	@Autowired
	protected ImageDAO imageDAO;
	
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
		
		boolean saved = imageDAO.save(image);
		assertTrue(saved);
		
		List<Image> images = imageDAO.getAll();
		size = size + 1;
		assertEquals(size, images.size());
		
		Image imageFromDatabase = images.get(size-1);
		assertEquals("ovf-id", imageFromDatabase.getOvfId());
		assertEquals("provider-image-id", imageFromDatabase.getProviderImageId());
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
}

