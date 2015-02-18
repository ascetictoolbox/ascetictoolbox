package es.bsc.vmplacement.domain;

import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

public class VmTest {
    
    @Test
    public void isInTheSameHostWorksWhenVmsInSameHost() {
        Host host = new Host((long) 1, "1", 4, 4096, 4, false);
        Vm vm1 = new Vm.Builder((long) 1, 1, 1024, 1).build();
        Vm vm2 = new Vm.Builder((long) 1, 1, 1024, 1).build();
        vm1.setHost(host);
        vm2.setHost(host);
        assertTrue(vm1.isInTheSameHost(vm2));
    }
    
    @Test
    public void isInTheSameHostWorksWhenVmsAreInDifferentHosts() {
        Host host1 = new Host((long) 1, "1", 4, 4096, 4, false);
        Host host2 = new Host((long) 2, "2", 2, 2048, 2, false);
        Vm vm1 = new Vm.Builder((long) 1, 1, 1024, 1).build();
        Vm vm2 = new Vm.Builder((long) 1, 1, 1024, 1).build();
        vm1.setHost(host1);
        vm2.setHost(host2);
        assertFalse(vm1.isInTheSameHost(vm2));
    }
    
}
