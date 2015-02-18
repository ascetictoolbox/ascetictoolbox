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

package es.bsc.vmplacement.domain;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * @author David Ortiz (david.ortiz@bsc.es)
 */
public class HostTest {

    private Host host = new Host((long) 1, "1", 4, 4096, 20, false); // 4 CPUs, 4GB RAM, 20GB disk;

    @Test
    public void testGetters() {
        assertEquals("1", host.getHostname());
        assertEquals(4, host.getNcpus());
        assertEquals(4096, host.getRamMb(), 0.1);
        assertEquals(20, host.getDiskGb(), 0.1);
    }

    @Test
    public void getUsage() {
        List<Vm> vms = new ArrayList<>();
        Vm vm1 = new Vm.Builder((long) 1, 1, 1024, 1).build();
        vm1.setHost(host);
        Vm vm2 = new Vm.Builder((long) 2, 2, 2048, 2).build();
        vms.add(vm1);
        vms.add(vm2);

        HostUsage hostUsage = host.getUsage(vms);
        assertEquals(1, hostUsage.getNcpusUsed());
        assertEquals(1024, hostUsage.getRamMbUsed());
        assertEquals(1, hostUsage.getDiskGbUsed());
    }

    @Test
    public void getOverCapacityScore() {
        List<Vm> vms = new ArrayList<>();
        Vm vm1 = new Vm.Builder((long) 1, 8, 8192, 40).build();
        vm1.setHost(host);
        Vm vm2 = new Vm.Builder((long) 2, 2, 2048, 2).build();
        vms.add(vm1);
        vms.add(vm2);
        assertEquals(-6, host.getOverCapacityScore(vms), 0.1); // -(8/4 + 8192/4096 + 40/20) = -6
    }

    @Test
    public void missingFixedVmsReturnsTrueWhenTheMissingVmIsNotAssignedToAnyHosts() {
        List<Vm> vms = new ArrayList<>();
        Vm vm1 = new Vm.Builder((long) 1, 8, 8192, 1).build();
        vms.add(vm1);

        host.addFixedVm(1);
        assertTrue(host.missingFixedVMs(vms));
    }

    @Test
    public void missingFixedVmsReturnsTrueWhenTheMissingVmIsAssignedToAnotherHost() {
        List<Vm> vms = new ArrayList<>();
        Vm vm1 = new Vm.Builder((long) 1, 8, 8192, 1).build();
        vm1.setHost(new Host((long) 2, "2", 8, 8192, 1, false));
        vms.add(vm1);

        host.addFixedVm(1);
        assertTrue(host.missingFixedVMs(vms));
    }

    @Test
    public void missingFixedVmsReturnsFalse() {
        List<Vm> vms = new ArrayList<>();
        Vm vm1 = new Vm.Builder((long) 1, 8, 8192, 1).build();
        vm1.setHost(host);
        vms.add(vm1);

        host.addFixedVm(1);
        assertFalse(host.missingFixedVMs(vms));
    }

    @Test
    public void toStringTest() {
        assertEquals("Host - ID:1, cpus:4, ram:4096.0, disk:20.0", host.toString());
    }

}
