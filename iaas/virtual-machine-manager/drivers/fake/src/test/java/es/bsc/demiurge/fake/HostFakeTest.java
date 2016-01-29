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

package es.bsc.demiurge.fake;


import es.bsc.demiurge.core.models.hosts.ServerLoad;
import es.bsc.demiurge.core.models.vms.Vm;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 *
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class HostFakeTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void constructorDoesNotAcceptNonPositiveTotalCpus() {
        exception.expect(IllegalArgumentException.class);
        new HostFake("hostName", -1, 1024, 1, 0, 0, 0);
    }

    @Test
    public void constructorDoesNotAcceptNonPositiveTotalMemoryMb() {
        exception.expect(IllegalArgumentException.class);
        new HostFake("hostName", 1, -1024, 1, 0, 0, 0);
    }

    @Test
    public void constructorDoesNotAcceptNonPositiveTotalDiskGb() {
        exception.expect(IllegalArgumentException.class);
        new HostFake("hostName", 1, 1024, -1, 0, 0, 0);
    }

    @Test
    public void constructorDoesNotAcceptNegativeAssignedCpus() {
        exception.expect(IllegalArgumentException.class);
        new HostFake("hostName", 1, 1024, 1, -1, 0, 0);
    }

    @Test
    public void constructorDoesNotAcceptNegativeAssignedMemoryMb() {
        exception.expect(IllegalArgumentException.class);
        new HostFake("hostName", 1, 1024, 1, 0, -1024, 0);
    }

    @Test
    public void constructorDoesNotAcceptNegativeAssignedDiskGb() {
        exception.expect(IllegalArgumentException.class);
        new HostFake("hostName", 1, 1024, 1, 0, 0, -1);
    }

    @Test
    public void constructorWithHostName() {
        HostFake hostInfo = new HostFake("hostName");
        assertEquals("hostName", hostInfo.getHostname());
    }

    @Test
    public void hasEnoughResources() {
        HostFake hostInfo = new HostFake("hostName", 4, 4096, 4, 1, 1024, 1);

        //case where the host would be full
        assertTrue(hostInfo.hasEnoughResources(3, 3072, 3));

        //case where the host would still have some resources available
        assertTrue(hostInfo.hasEnoughResources(2, 1024, 1));
    }

    @Test
    public void doesNotHaveEnoughResources() {
        HostFake hostInfo = new HostFake("hostName", 4, 4096, 4, 2, 2048, 2);

        //case where the host does not have enough CPUs available
        assertFalse(hostInfo.hasEnoughResources(4, 1024, 1));

        //case where the host does not have enough memory available
        assertFalse(hostInfo.hasEnoughResources(1, 4096, 1));

        //case where the host does not have enough disk available
        assertFalse(hostInfo.hasEnoughResources(1, 1024, 4));

        //case where the host does not have enough CPU, memory, nor disk
        assertFalse(hostInfo.hasEnoughResources(4, 4096, 4));
    }

    @Test
    public void hasEnoughResourcesForVms() {
        HostFake hostInfo = new HostFake("hostName", 4, 4096, 4, 1, 1024, 1);

        // Create list of VMs
        List<Vm> vms = new ArrayList<>();
        vms.add(new Vm("vm1", "image", 1, 1024, 1, null, ""));
        vms.add(new Vm("vm2", "image", 1, 1024, 1, null, ""));
        vms.add(new Vm("vm3", "image", 1, 1024, 1, null, ""));

        assertTrue(hostInfo.hasEnoughResourcesToDeployVms(vms));
    }

    @Test
    public void doesNotHaveEnoughResourcesForVms() {
        HostFake hostInfo = new HostFake("hostName", 4, 4096, 4, 1, 1024, 1);

        // Create list of VMs
        List<Vm> vms = new ArrayList<>();
        vms.add(new Vm("vm1", "image", 2, 1024, 1, null, ""));
        vms.add(new Vm("vm2", "image", 2, 1024, 1, null, ""));
        vms.add(new Vm("vm3", "image", 2, 1024, 1, null, ""));

        assertFalse(hostInfo.hasEnoughResourcesToDeployVms(vms));
    }

    @Test
    public void getTotalCpus() {
        HostFake hostInfo = new HostFake("hostName", 4, 4096, 4, 2, 2048, 2);
        assertTrue(hostInfo.getTotalCpus() == 4);
    }

    @Test
    public void getTotalMemoryMb() {
        HostFake hostInfo = new HostFake("hostName", 4, 4096, 4, 2, 2048, 2);
        assertTrue(hostInfo.getTotalMemoryMb() == 4096);
    }

    @Test
    public void getTotalDiskGb() {
        HostFake hostInfo = new HostFake("hostName", 4, 4096, 4, 2, 2048, 2);
        assertTrue(hostInfo.getTotalDiskGb() == 4);
    }

    @Test
    public void getAssignedCpus() {
        HostFake hostInfo = new HostFake("hostName", 4, 4096, 4, 2, 2048, 2);
        assertTrue(hostInfo.getAssignedCpus() == 2);
    }

    @Test
    public void getAssignedMemoryMb() {
        HostFake hostInfo = new HostFake("hostName", 4, 4096, 4, 2, 2048, 2);
        assertTrue(hostInfo.getAssignedMemoryMb() == 2048);
    }

    @Test
    public void getAssignedDiskGb() {
        HostFake hostInfo = new HostFake("hostName", 4, 4096, 4, 2, 2048, 2);
        assertTrue(hostInfo.getAssignedDiskGb() == 2);
    }

    @Test
    public void getFutureLoadIfVMDeployedInHost() {
        HostFake hostInfo = new HostFake("hostName", 4, 4096, 4, 2, 2048, 2);
        ServerLoad futureLoad = hostInfo.getFutureLoadIfVMDeployed(new Vm("vm1", "image", 1, 1024, 1, null, ""));
        Assert.assertEquals(0.75, futureLoad.getCpuLoad(), 0.1);
        Assert.assertEquals(0.75, futureLoad.getRamLoad(), 0.1);
        Assert.assertEquals(0.75, futureLoad.getDiskLoad(), 0.1);
    }

    @Test
    public void getServerLoad() {
        ServerLoad serverLoad = new HostFake("hostName", 4, 4096, 4, 1, 2048, 3).getServerLoad();
        Assert.assertEquals(0.25, serverLoad.getCpuLoad(), 0);
        Assert.assertEquals(0.5, serverLoad.getRamLoad(), 0);
        Assert.assertEquals(0.75, serverLoad.getDiskLoad(), 0);
    }

}
