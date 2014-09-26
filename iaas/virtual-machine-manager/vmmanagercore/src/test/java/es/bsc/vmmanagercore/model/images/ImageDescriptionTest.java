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

package es.bsc.vmmanagercore.model.images;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
*
*
* @author David Ortiz Lopez (david.ortiz@bsc.es)
*
*/
public class ImageDescriptionTest {

    private ImageUploaded imageDesc;

    @Before
    public void setUp() {
        imageDesc = new ImageUploaded("testImageId", "testImage", "active");
    }

    @Test
    public void setGetId() {
        imageDesc.setId("newImageId");
        assertEquals("newImageId", imageDesc.getId());
    }

    @Test
    public void setGetName() {
        imageDesc.setName("newImageName");
        assertEquals("newImageName", imageDesc.getName());
    }

    @Test
    public void setGetStatus() {
        imageDesc.setStatus("fakeStatus");
        assertEquals("fakeStatus", imageDesc.getStatus());
    }

}
