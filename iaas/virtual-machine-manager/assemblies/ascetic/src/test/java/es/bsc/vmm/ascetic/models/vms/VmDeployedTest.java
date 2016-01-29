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

package es.bsc.vmm.ascetic.models.vms;

import es.bsc.demiurge.core.models.vms.VmDeployed;
import org.junit.Test;

import java.util.Date;

import static junit.framework.TestCase.assertEquals;

/**
 * Tests for the VmDeployed class.
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class VmDeployedTest {

    private VmDeployed vmDeployed = new VmDeployed("vm", "fakeImage", 1, 1024, 1, 0, "", "", "vmId", "172.16.8.1",
            "ACTIVE", new Date(), "host1");

    @Test
    public void getId() {
        assertEquals("vmId", vmDeployed.getId());
    }

    @Test
    public void getIpAddress() {
        assertEquals("172.16.8.1", vmDeployed.getIpAddress());
    }

    @Test
    public void getState() {
        assertEquals("ACTIVE", vmDeployed.getState());
    }

    @Test
    public void getHostname() {
        assertEquals("host1", vmDeployed.getHostName());
    }

}