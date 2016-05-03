package eu.ascetic.paas.applicationmanager.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import eu.ascetic.paas.applicationmanager.dao.ApplicationDAO;
import eu.ascetic.paas.applicationmanager.dao.ImageDAO;
import eu.ascetic.paas.applicationmanager.dao.VMDAO;
import eu.ascetic.paas.applicationmanager.model.Application;
import eu.ascetic.paas.applicationmanager.model.Collection;
import eu.ascetic.paas.applicationmanager.model.Image;
import eu.ascetic.paas.applicationmanager.model.VM;
import eu.ascetic.paas.applicationmanager.providerregistry.PRClient;
import eu.ascetic.paas.applicationmanager.vmmanager.client.VmManagerClient;

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
 * @author David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * e-mail: david.garciaperez@atos.net 
 * 
 * Image cache REST test
 */
public class CacheImageRestTest {

	@Test
	public void getImagesTest() throws Exception {
		ImageDAO imageDAO = mock(ImageDAO.class);
		ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
		
		CacheImageRest cacheImageRest = new CacheImageRest();
		cacheImageRest.applicationDAO = applicationDAO;
		cacheImageRest.imageDAO = imageDAO;
		
		// We prepare the Mocks
		//    First it is necessary to get the application from the DB
		Application application = new Application();
		application.setName("applicationName");
		when(applicationDAO.getByName("applicationName")).thenReturn(application);
		//    Then we need to get the images associated to that application
		Image image1 = new Image();
		image1.setApplication(application);
		image1.setDemo(true);
		image1.setId(1);
		image1.setOvfHref("ovf-href-1");
		image1.setOvfId("ovf-id1");
		image1.setProviderImageId("uuid1");
		Image image2 = new Image();
		image2.setApplication(application);
		image2.setDemo(true);
		image2.setId(2);
		image2.setOvfHref("ovf-href-2");
		image2.setOvfId("ovf-id2");
		image2.setProviderImageId("uuid2");
		List<Image> images = new ArrayList<Image>();
		images.add(image1);
		images.add(image2);
		when(imageDAO.getCacheImagesForApplication(application)).thenReturn(images);
		
		// We test the method
		Response response = cacheImageRest.getCacheImagesForApplication("applicationName");
		
		assertEquals(200, response.getStatus());
		
		String xml = (String) response.getEntity();
		
		JAXBContext jaxbContext = JAXBContext.newInstance(Collection.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		Collection collection = (Collection) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		//Collection
		assertEquals("/applications/applicationName/cache-images", collection.getHref());
		assertEquals(0, collection.getItems().getOffset());
		assertEquals(2, collection.getItems().getTotal());
		assertEquals(2, collection.getItems().getImages().size());
		//     Image 1
		image1 = collection.getItems().getImages().get(0);
		assertEquals("/applications/applicationName/cache-images/1", image1.getHref());
		assertEquals(1, image1.getId());
		assertEquals("ovf-href-1", image1.getOvfHref());
		assertEquals("ovf-id1", image1.getOvfId());
		assertEquals("uuid1", image1.getProviderImageId());
		//     Image 2
		image2 = collection.getItems().getImages().get(1);
		assertEquals("/applications/applicationName/cache-images/2", image2.getHref());
		assertEquals(2, image2.getId());
		assertEquals("ovf-href-2", image2.getOvfHref());
		assertEquals("ovf-id2", image2.getOvfId());
		assertEquals("uuid2", image2.getProviderImageId());
	}
	
	@Test
	public void deleteImageTestNoImageInTheDB() {
		ImageDAO imageDAO = mock(ImageDAO.class);
		CacheImageRest cacheImageRest = new CacheImageRest();
		cacheImageRest.imageDAO = imageDAO;
		
		// We make that the image is not in the db...
		when(imageDAO.getById(1)).thenReturn(null);
		
		Response response = cacheImageRest.deleteCacheImage("applicationName", "1");
		assertEquals(404, response.getStatus());
		assertEquals("No image with id: 1 found in the DB!", (String) response.getEntity());
	}
	
	@Test
	public void deleteImageTestParserIdError() {
		CacheImageRest cacheImageRest = new CacheImageRest();

		Response response = cacheImageRest.deleteCacheImage("applicationName", "aadas");
		assertEquals(400, response.getStatus());
		assertEquals("Id: aadas is not a valid image id!!!", (String) response.getEntity());
	}
	
	@Test
	public void deleteImageTestNoCacheIamge() {
		ImageDAO imageDAO = mock(ImageDAO.class);
		CacheImageRest cacheImageRest = new CacheImageRest();
		cacheImageRest.imageDAO = imageDAO;
		
		// We make that the image is in the db but no cache image...
		Image image1 = new Image();
		image1.setDemo(false);
		image1.setId(1);
		image1.setOvfHref("ovf-href-1");
		image1.setOvfId("ovf-id1");
		image1.setProviderImageId("uuid1");
		when(imageDAO.getById(1)).thenReturn(image1);
		
		Response response = cacheImageRest.deleteCacheImage("applicationName", "1");
		assertEquals(400, response.getStatus());
		assertEquals("Image with ID: 1 is not a cache Image, not possible to delete it.", (String) response.getEntity());
	}
	
	@Test
	public void deleteImageTestWithStillVMsUsingIt() {
		ImageDAO imageDAO = mock(ImageDAO.class);
		VMDAO vmDAO = mock(VMDAO.class);
		
		CacheImageRest cacheImageRest = new CacheImageRest();
		cacheImageRest.imageDAO = imageDAO;
		cacheImageRest.vmDAO = vmDAO;
		
		Image image = new Image();
		image.setDemo(true);
		image.setId(1);
		image.setOvfHref("ovf-href-1");
		image.setOvfId("ovf-id1");
		image.setProviderImageId("uuid1");
		when(imageDAO.getById(1)).thenReturn(image);
		
		VM vm = new VM();
		List<VM> vms = new ArrayList<VM>();
		vms.add(vm);
		
		when(vmDAO.getNotDeletedVMsWithImage(image)).thenReturn(vms);
		
		Response response = cacheImageRest.deleteCacheImage("applicationName", "1");
		assertEquals(409, response.getStatus());
		assertEquals("Image with ID: 1 is still being used by one or more VMs.", (String) response.getEntity());
	}
	
	@Test
	public void deleteImageTest() {
		ImageDAO imageDAO = mock(ImageDAO.class);
		VMDAO vmDAO = mock(VMDAO.class);
		VmManagerClient vMManagerClient = mock(VmManagerClient.class);
		
		PRClient prClient = mock(PRClient.class);
		when(prClient.getVMMClient(-1)).thenReturn(vMManagerClient);
		
		CacheImageRest cacheImageRest = new CacheImageRest();
		cacheImageRest.imageDAO = imageDAO;
		cacheImageRest.vmDAO = vmDAO;
		cacheImageRest.prClient = prClient;
		
		Image image = new Image();
		image.setDemo(true);
		image.setId(1);
		image.setOvfHref("ovf-href-1");
		image.setOvfId("ovf-id1");
		image.setProviderImageId("uuid1");
		when(imageDAO.getById(1)).thenReturn(image);
		
		List<VM> vms = new ArrayList<VM>();
		when(vmDAO.getNotDeletedVMsWithImage(image)).thenReturn(vms);
		
		when(imageDAO.update(image)).thenReturn(true);
		
		Response response = cacheImageRest.deleteCacheImage("applicationName", "1");
		assertEquals(204, response.getStatus());
		assertEquals("", (String) response.getEntity());
		
		ArgumentCaptor<Image> imageCaptor = ArgumentCaptor.forClass(Image.class);
		verify(imageDAO, times(1)).update(imageCaptor.capture());
	}
	
	@Test
	public void deleteImageWithProviderIdTest() {
		ImageDAO imageDAO = mock(ImageDAO.class);
		VMDAO vmDAO = mock(VMDAO.class);
		VmManagerClient vMManagerClient = mock(VmManagerClient.class);
		
		PRClient prClient = mock(PRClient.class);
		when(prClient.getVMMClient(2)).thenReturn(vMManagerClient);
		
		CacheImageRest cacheImageRest = new CacheImageRest();
		cacheImageRest.imageDAO = imageDAO;
		cacheImageRest.vmDAO = vmDAO;
		cacheImageRest.prClient = prClient;
		
		Image image = new Image();
		image.setDemo(true);
		image.setId(1);
		image.setOvfHref("ovf-href-1");
		image.setOvfId("ovf-id1");
		image.setProviderImageId("uuid1");
		image.setProviderId("2");
		when(imageDAO.getById(1)).thenReturn(image);
		
		List<VM> vms = new ArrayList<VM>();
		when(vmDAO.getNotDeletedVMsWithImage(image)).thenReturn(vms);
		
		when(imageDAO.update(image)).thenReturn(true);
		
		Response response = cacheImageRest.deleteCacheImage("applicationName", "1");
		assertEquals(204, response.getStatus());
		assertEquals("", (String) response.getEntity());
		
		ArgumentCaptor<Image> imageCaptor = ArgumentCaptor.forClass(Image.class);
		verify(imageDAO, times(1)).update(imageCaptor.capture());
	}
	
	
	@Test
	public void deleteImageWithEmptyStringTest() {
		ImageDAO imageDAO = mock(ImageDAO.class);
		VMDAO vmDAO = mock(VMDAO.class);
		VmManagerClient vMManagerClient = mock(VmManagerClient.class);
		
		PRClient prClient = mock(PRClient.class);
		when(prClient.getVMMClient(-1)).thenReturn(vMManagerClient);
		
		CacheImageRest cacheImageRest = new CacheImageRest();
		cacheImageRest.imageDAO = imageDAO;
		cacheImageRest.vmDAO = vmDAO;
		cacheImageRest.prClient = prClient;
		
		Image image = new Image();
		image.setDemo(true);
		image.setId(1);
		image.setOvfHref("ovf-href-1");
		image.setOvfId("ovf-id1");
		image.setProviderImageId("uuid1");
		image.setProviderId("");
		when(imageDAO.getById(1)).thenReturn(image);
		
		List<VM> vms = new ArrayList<VM>();
		when(vmDAO.getNotDeletedVMsWithImage(image)).thenReturn(vms);
		
		when(imageDAO.update(image)).thenReturn(true);
		
		Response response = cacheImageRest.deleteCacheImage("applicationName", "1");
		assertEquals(204, response.getStatus());
		assertEquals("", (String) response.getEntity());
		
		ArgumentCaptor<Image> imageCaptor = ArgumentCaptor.forClass(Image.class);
		verify(imageDAO, times(1)).update(imageCaptor.capture());
	}
	
	@Test
	public void deleteImageWithInvalidProviderString() {
		ImageDAO imageDAO = mock(ImageDAO.class);
		VMDAO vmDAO = mock(VMDAO.class);
		
		CacheImageRest cacheImageRest = new CacheImageRest();
		cacheImageRest.imageDAO = imageDAO;
		cacheImageRest.vmDAO = vmDAO;
		
		Image image = new Image();
		image.setDemo(true);
		image.setId(1);
		image.setOvfHref("ovf-href-1");
		image.setOvfId("ovf-id1");
		image.setProviderImageId("uuid1");
		image.setProviderId("asdfas");
		when(imageDAO.getById(1)).thenReturn(image);
		
		List<VM> vms = new ArrayList<VM>();
		when(vmDAO.getNotDeletedVMsWithImage(image)).thenReturn(vms);
		
		Response response = cacheImageRest.deleteCacheImage("applicationName", "1");
		assertEquals(500, response.getStatus());
		assertEquals("Wrong provider ID stored in the DB", (String) response.getEntity());	
	}
	
	@Test
	public void deleteImageWithNotAvailableProvider() {
		ImageDAO imageDAO = mock(ImageDAO.class);
		VMDAO vmDAO = mock(VMDAO.class);
		
		PRClient prClient = mock(PRClient.class);
		when(prClient.getVMMClient(3)).thenReturn(null);
		
		CacheImageRest cacheImageRest = new CacheImageRest();
		cacheImageRest.imageDAO = imageDAO;
		cacheImageRest.vmDAO = vmDAO;
		cacheImageRest.prClient = prClient;
		
		Image image = new Image();
		image.setDemo(true);
		image.setId(1);
		image.setOvfHref("ovf-href-1");
		image.setOvfId("ovf-id1");
		image.setProviderImageId("uuid1");
		image.setProviderId("3");
		when(imageDAO.getById(1)).thenReturn(image);
		
		List<VM> vms = new ArrayList<VM>();
		when(vmDAO.getNotDeletedVMsWithImage(image)).thenReturn(vms);
		
		Response response = cacheImageRest.deleteCacheImage("applicationName", "1");
		assertEquals(500, response.getStatus());
		assertEquals("No provider with ID: 3", (String) response.getEntity());
	}
}
