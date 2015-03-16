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

package es.bsc.vmmanagercore.manager.components;

import es.bsc.vmmanagercore.cloudmiddleware.CloudMiddleware;
import es.bsc.vmmanagercore.model.images.ImageUploaded;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Tests for ImageManager.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
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
        // Mock cloud middleware response
        List<ImageUploaded> mockedCloudMiddlewareResponse = new ArrayList<>();
        Mockito.when(mockedCloudMiddleware.getVmImages()).thenReturn(mockedCloudMiddlewareResponse);

        assertTrue(imageManager.getVmImages().isEmpty());
    }
    
}
