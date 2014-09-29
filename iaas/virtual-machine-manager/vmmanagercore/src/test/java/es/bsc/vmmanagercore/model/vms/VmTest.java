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

package es.bsc.vmmanagercore.model.vms;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

/**
 *
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class VmTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private Vm vmDesc;

    @Before
    public void setUp() {
        vmDesc = new Vm("TestVm", "fakeImageId", 1, 1024, 2, null, "app1");
    }

    @Test
    public void setGetInstanceName() {
        vmDesc.setInstanceName("newInstanceName");
        assertEquals("newInstanceName", vmDesc.getName());
    }

    @Test
    public void setGetImageId() {
        vmDesc.setImage("newImageId");
        assertEquals("newImageId", vmDesc.getImage());
    }

    @Test
    public void setGetCpus() {
        vmDesc.setCpus(2);
        assertTrue(vmDesc.getCpus() == 2);
    }

    @Test
    public void paramSetCpusHasToBePositive() {
        exception.expect(IllegalArgumentException.class);
        vmDesc.setCpus(-1);
    }

    @Test
    public void setGetRamMb() {
        vmDesc.setRamMb(2048);
        assertTrue(vmDesc.getRamMb() == 2048);
    }

    @Test
    public void paramSetRamMbHasToBePositive() {
        exception.expect(IllegalArgumentException.class);
        vmDesc.setRamMb(-2);
    }

    @Test
    public void setGetDiskGb() {
        vmDesc.setDiskGb(4);
        assertTrue(vmDesc.getDiskGb() == 4);
    }

    @Test
    public void paramSetDiskGbHasToBePositive() {
        exception.expect(IllegalArgumentException.class);
        vmDesc.setDiskGb(-1);
    }

    // Not all VMs will have an init script associated, so we need
    // to make sure that the attribute can be set to null
    @Test
    public void initScriptCanBeNull() {
        vmDesc.setInitScript(null);
        assertNull(vmDesc.getInitScript());
    }

    @Test
    public void initScriptPathHasToBeValid() {
        exception.expect(IllegalArgumentException.class);
        vmDesc.setInitScript("fakePathForInitScript");
    }

}