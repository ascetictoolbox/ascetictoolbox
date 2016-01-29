/**
 Copyright (C) 2013-2014  Barcelona Supercomputing Center

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package es.bsc.vmm.ascetic.manager.components;

import es.bsc.demiurge.core.cloudmiddleware.CloudMiddleware;
import es.bsc.demiurge.core.manager.components.ImageManager;
import es.bsc.demiurge.core.models.images.ImageToUpload;
import es.bsc.demiurge.core.models.images.ImageUploaded;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests for ImageManager.
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class ImageManagerTest {

    private final CloudMiddleware mockedCloudMiddleware = PowerMockito.mock(CloudMiddleware.class);
    private final ImageManager imageManager = new ImageManager(mockedCloudMiddleware);
    
    @Test
    public void getVmImages() {
        // Mock cloud middleware response
        List<ImageUploaded> mockedCloudMiddlewareResponse = new ArrayList<>();
        mockedCloudMiddlewareResponse.add(new ImageUploaded("imageId", "imageName", "active"));
        Mockito.when(mockedCloudMiddleware.getVmImages()).thenReturn(mockedCloudMiddlewareResponse);
        
        assertEquals(mockedCloudMiddlewareResponse, imageManager.getVmImages());
    }
    
    @Test
    public void getVmImagesReturnsEmptyWhenThereAreNotAnyImages() {
        Mockito.when(mockedCloudMiddleware.getVmImages()).thenReturn(new ArrayList<ImageUploaded>());
        assertTrue(imageManager.getVmImages().isEmpty());
    }
    
    @Test
    public void createVmImage() throws Exception {
        // Mock cloud middleware response
        String mockedCloudMiddlewareResponse = "NewImageId";
        ImageToUpload imageToBeCreated = new ImageToUpload("newImageName", "http://fakeUrl.com");
        Mockito.when(mockedCloudMiddleware.createVmImage(imageToBeCreated)).thenReturn(mockedCloudMiddlewareResponse);
        
        assertEquals(mockedCloudMiddlewareResponse, imageManager.createVmImage(imageToBeCreated));
    }
    
    @Test
    public void getVmImage() {
        // Mock cloud middleware response
        ImageUploaded mockedCloudMiddlewareResponse = new ImageUploaded("imageId", "imageName", "active");
        Mockito.when(mockedCloudMiddleware.getVmImage("queryImageId")).thenReturn(mockedCloudMiddlewareResponse);
        
        assertEquals(mockedCloudMiddlewareResponse, imageManager.getVmImage("queryImageId"));
    }
    
    @Test
    public void getVmImageReturnsNullWhenItDoesNotExist() {
        Mockito.when(mockedCloudMiddleware.getVmImage("queryImageId")).thenReturn(null);
        assertNull(imageManager.getVmImage("queryImageId"));
    }
    
    @Test
    public void getVmImagesIds() {
        // Mock cloud middleware response
        List<ImageUploaded> mockedCloudMiddlewareResponse = new ArrayList<>();
        mockedCloudMiddlewareResponse.add(new ImageUploaded("image1", "imageName1", "active"));
        mockedCloudMiddlewareResponse.add(new ImageUploaded("image2", "imageName2", "active"));
        Mockito.when(mockedCloudMiddleware.getVmImages()).thenReturn(mockedCloudMiddlewareResponse);
        
        List<String> imagesIds = new ArrayList<>();
        imagesIds.add("image1");
        imagesIds.add("image2");
        assertTrue(imageManager.getVmImagesIds().containsAll(imagesIds));
    }
    
    @Test
    public void getVmImagesIdsReturnsEmptyWhenThereAreNotAny() {
        Mockito.when(mockedCloudMiddleware.getVmImages()).thenReturn(new ArrayList<ImageUploaded>());
        assertTrue(imageManager.getVmImagesIds().isEmpty());
    }
    
}
