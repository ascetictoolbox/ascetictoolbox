package es.bsc.vmplacement.domain;

import org.junit.Before;
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

    private Host host;

    @Before
    public void setUp() {
        host = new Host((long) 1, "1", 4, 4096, 20); // 4 CPUs, 4GB RAM, 20GB disk
    }

    @Test
    public void getUsage() {
        List<Vm> vms = new ArrayList<>();
        Vm vm1 = new Vm((long) 1, 1, 1024, 1);
        vm1.setHost(host);
        Vm vm2 = new Vm((long) 2, 2, 2048, 2);
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
        Vm vm1 = new Vm((long) 1, 8, 8192, 1);
        vm1.setHost(host);
        Vm vm2 = new Vm((long) 2, 2, 2048, 2);
        vms.add(vm1);
        vms.add(vm2);
        assertEquals(-4, host.getOverCapacityScore(vms), 0.1); // -(8/4 + 8192/4096) = -4
    }

    @Test
    public void missingFixedVmsReturnsTrueWhenTheMissingVmIsNotAssignedToAnyHosts() {
        List<Vm> vms = new ArrayList<>();
        Vm vm1 = new Vm((long) 1, 8, 8192, 1);
        vms.add(vm1);

        host.addFixedVm(1);
        assertTrue(host.missingFixedVMs(vms));
    }

    @Test
    public void missingFixedVmsReturnsTrueWhenTheMissingVmIsAssignedToAnotherHost() {
        List<Vm> vms = new ArrayList<>();
        Vm vm1 = new Vm((long) 1, 8, 8192, 1);
        vm1.setHost(new Host((long) 2, "2", 8, 8192, 1));
        vms.add(vm1);

        host.addFixedVm(1);
        assertTrue(host.missingFixedVMs(vms));
    }

    @Test
    public void missingFixedVmsReturnsFalse() {
        List<Vm> vms = new ArrayList<>();
        Vm vm1 = new Vm((long) 1, 8, 8192, 1);
        vm1.setHost(host);
        vms.add(vm1);

        host.addFixedVm(1);
        assertFalse(host.missingFixedVMs(vms));
    }

}
