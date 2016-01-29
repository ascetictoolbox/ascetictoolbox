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

import es.bsc.demiurge.core.models.vms.Vm;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

/**
 * Tests for the Vm class.
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
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
    public void getInstanceName() {
        assertEquals("TestVm", vmDesc.getName());
    }

    @Test
    public void getImageId() {
        assertEquals("fakeImageId", vmDesc.getImage());
    }

    @Test
    public void getCpus() {
        assertTrue(vmDesc.getCpus() == 1);
    }

    @Test
    public void paramSetCpusHasToBePositive() {
        exception.expect(IllegalArgumentException.class);
        vmDesc = new Vm("TestVm", "fakeImageId", -1, 1024, 2, null, "app1");
    }

    @Test
    public void getRamMb() {
        assertTrue(vmDesc.getRamMb() == 1024);
    }

    @Test
    public void paramSetRamMbHasToBePositive() {
        exception.expect(IllegalArgumentException.class);
        vmDesc = new Vm("TestVm", "fakeImageId", 1, -1024, 2, null, "app1");
    }

    @Test
    public void getDiskGb() {
        assertTrue(vmDesc.getDiskGb() == 2);
    }

    @Test
    public void paramSetDiskGbHasToBePositive() {
        exception.expect(IllegalArgumentException.class);
        vmDesc = new Vm("TestVm", "fakeImageId", 1, 1024, -2, null, "app1");
    }

    // Not all VMs will have an loadConfiguration script associated, so we need
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
