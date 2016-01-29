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

package es.bsc.vmm.ascetic.models.images;

import es.bsc.demiurge.core.models.images.ImageUploaded;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
* Tests for the ImageUploaded class.
*
* @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
*
*/
public class ImageUploadedTest {

    private ImageUploaded imageDesc;

    @Before
    public void setUp() {
        imageDesc = new ImageUploaded("testImageId", "testImage", "active");
    }

    @Test
    public void getId() {
        assertEquals("testImageId", imageDesc.getId());
    }

    @Test
    public void getName() {
        assertEquals("testImage", imageDesc.getName());
    }

    @Test
    public void getStatus() {
        assertEquals("active", imageDesc.getStatus());
    }

}
