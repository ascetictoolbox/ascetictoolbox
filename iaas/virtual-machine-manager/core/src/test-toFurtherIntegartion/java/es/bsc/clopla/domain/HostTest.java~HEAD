/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package es.bsc.demiurge.core.clopla.domain;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * @author Mario Macias (github.com/mariomac), David Ortiz (david.ortiz@bsc.es)
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
