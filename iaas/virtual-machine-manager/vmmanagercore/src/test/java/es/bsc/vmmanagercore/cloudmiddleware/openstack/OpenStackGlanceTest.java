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

package es.bsc.vmmanagercore.cloudmiddleware.openstack;

import es.bsc.vmmanagercore.configuration.VmManagerConfiguration;
import es.bsc.vmmanagercore.model.images.ImageToUpload;
import org.junit.BeforeClass;
import org.junit.Test;

public class OpenStackGlanceTest {

    private static OpenStackGlance glance;

    @BeforeClass
    public static void setUpClass() {
        VmManagerConfiguration conf = VmManagerConfiguration.getInstance();
        glance = new OpenStackGlance(new OpenStackCredentials(conf.openStackIP, conf.keyStonePort, conf.keyStoneTenant,
                conf.keyStoneUser, conf.keyStonePassword, conf.glancePort, conf.keyStoneTenantId));
    }

    //This test only checks that the create and delete operations do not raise exceptions.
    @Test
    public void canCreateAndDelete() {
        ImageToUpload imageToUpload = new ImageToUpload("testImage",
                VmManagerConfiguration.getInstance().testingImageUrl);
        String imageId = glance.createImageFromUrl(imageToUpload);
        glance.deleteImage(imageId);
    }

}
