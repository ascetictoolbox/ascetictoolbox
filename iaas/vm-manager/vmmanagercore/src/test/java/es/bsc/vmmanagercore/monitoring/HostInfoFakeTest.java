package es.bsc.vmmanagercore.monitoring;

import es.bsc.vmmanagercore.model.ServerLoad;
import es.bsc.vmmanagercore.model.Vm;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 *
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class HostInfoFakeTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void constructorDoesNotAcceptNonPositiveTotalCpus() {
        exception.expect(IllegalArgumentException.class);
        new HostInfoFake("hostName", -1, 1024, 1, 0, 0, 0);
    }

    @Test
    public void constructorDoesNotAcceptNonPositiveTotalMemoryMb() {
        exception.expect(IllegalArgumentException.class);
        new HostInfoFake("hostName", 1, -1024, 1, 0, 0, 0);
    }

    @Test
    public void constructorDoesNotAcceptNonPositiveTotalDiskGb() {
        exception.expect(IllegalArgumentException.class);
        new HostInfoFake("hostName", 1, 1024, -1, 0, 0, 0);
    }

    @Test
    public void constructorDoesNotAcceptNegativeAssignedCpus() {
        exception.expect(IllegalArgumentException.class);
        new HostInfoFake("hostName", 1, 1024, 1, -1, 0, 0);
    }

    @Test
    public void constructorDoesNotAcceptNegativeAssignedMemoryMb() {
        exception.expect(IllegalArgumentException.class);
        new HostInfoFake("hostName", 1, 1024, 1, 0, -1024, 0);
    }

    @Test
    public void constructorDoesNotAcceptNegativeAssignedDiskGb() {
        exception.expect(IllegalArgumentException.class);
        new HostInfoFake("hostName", 1, 1024, 1, 0, 0, -1);
    }

    @Test
    public void constructorDoesNotAcceptMoreAssignedCpusThanTotal() {
        exception.expect(IllegalArgumentException.class);
        new HostInfoFake("hostName", 1, 1024, 1, 2, 0, 0);
    }

    @Test
    public void constructorDoesNotAcceptMoreAssignedMemoryThanTotal() {
        exception.expect(IllegalArgumentException.class);
        new HostInfoFake("hostName", 1, 1024, 1, 0, 2048, 0);
    }

    @Test
    public void constructorDoesNotAcceptMoreAssignedDiskThanTotal() {
        exception.expect(IllegalArgumentException.class);
        new HostInfoFake("hostName", 1, 1024, 1, 0, 0, 2);
    }

    @Test
    public void constructorWithHostName() {
        HostInfoFake hostInfo = new HostInfoFake("hostName");
        assertEquals("hostName", hostInfo.getHostname());
    }

    @Test
    public void hasEnoughResources() {
        HostInfoFake hostInfo = new HostInfoFake("hostName", 4, 4096, 4, 1, 1024, 1);

        //case where the host would be full
        assertTrue(hostInfo.hasEnoughResources(3, 3072, 3));

        //case where the host would still have some resources available
        assertTrue(hostInfo.hasEnoughResources(2, 1024, 1));
    }

    @Test
    public void doesNotHaveEnoughResources() {
        HostInfoFake hostInfo = new HostInfoFake("hostName", 4, 4096, 4, 2, 2048, 2);

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
    public void hasEnoughResourcesUsingReservations() {
        HostInfoFake hostInfo = new HostInfoFake("hostName", 4, 4096, 4, 1, 1024, 1);

        //reserve some resources
        hostInfo.setReservedCpus(1);
        hostInfo.setReservedMemoryMb(1024);
        hostInfo.setReservedDiskGb(1);

        //case where the host would be full
        assertTrue(hostInfo.hasEnoughResources(2, 2048, 2));

        //case where the host would still have some resources available
        assertTrue(hostInfo.hasEnoughResources(1, 1024, 1));
    }

    @Test
    public void doesNotHaveEnoughResourcesUsingReservations() {
        HostInfoFake hostInfo = new HostInfoFake("hostName", 4, 4096, 4, 1, 1024, 1);

        //reserve some resources
        hostInfo.setReservedCpus(1);
        hostInfo.setReservedMemoryMb(1024);
        hostInfo.setReservedDiskGb(1);

        //case where the host does not have enough CPUs available
        assertFalse(hostInfo.hasEnoughResources(3, 1024, 1));

        //case where the host does not have enough memory available
        assertFalse(hostInfo.hasEnoughResources(1, 3072, 1));

        //case where the host does not have enough disk available
        assertFalse(hostInfo.hasEnoughResources(1, 1024, 3));

        //case where the host does not have enough CPU, memory, nor disk
        assertFalse(hostInfo.hasEnoughResources(3, 3072, 3));
    }

    @Test
    public void hasEnoughResourcesForVms() {
        HostInfoFake hostInfo = new HostInfoFake("hostName", 4, 4096, 4, 1, 1024, 1);

        // Create list of VMs
        List<Vm> vms = new ArrayList<>();
        vms.add(new Vm("vm1", "image", 1, 1024, 1, null, ""));
        vms.add(new Vm("vm2", "image", 1, 1024, 1, null, ""));
        vms.add(new Vm("vm3", "image", 1, 1024, 1, null, ""));

        assertTrue(hostInfo.hasEnoughResourcesToDeployVms(vms));
    }

    @Test
    public void doesNotHaveEnoughResourcesForVms() {
        HostInfoFake hostInfo = new HostInfoFake("hostName", 4, 4096, 4, 1, 1024, 1);

        // Create list of VMs
        List<Vm> vms = new ArrayList<>();
        vms.add(new Vm("vm1", "image", 2, 1024, 1, null, ""));
        vms.add(new Vm("vm2", "image", 2, 1024, 1, null, ""));
        vms.add(new Vm("vm3", "image", 2, 1024, 1, null, ""));

        assertFalse(hostInfo.hasEnoughResourcesToDeployVms(vms));
    }

    @Test
    public void setGetHostName() {
        HostInfoFake hostInfo = new HostInfoFake("hostName", 4, 4096, 4, 2, 2048, 2);
        hostInfo.setHostname("newHostName");
        assertEquals("newHostName", hostInfo.getHostname());
    }

    @Test
    public void getTotalCpus() {
        HostInfoFake hostInfo = new HostInfoFake("hostName", 4, 4096, 4, 2, 2048, 2);
        assertTrue(hostInfo.getTotalCpus() == 4);
    }

    @Test
    public void getTotalMemoryMb() {
        HostInfoFake hostInfo = new HostInfoFake("hostName", 4, 4096, 4, 2, 2048, 2);
        assertTrue(hostInfo.getTotalMemoryMb() == 4096);
    }

    @Test
    public void getTotalDiskGb() {
        HostInfoFake hostInfo = new HostInfoFake("hostName", 4, 4096, 4, 2, 2048, 2);
        assertTrue(hostInfo.getTotalDiskGb() == 4);
    }

    @Test
    public void getAssignedCpus() {
        HostInfoFake hostInfo = new HostInfoFake("hostName", 4, 4096, 4, 2, 2048, 2);
        assertTrue(hostInfo.getAssignedCpus() == 2);
    }

    @Test
    public void getAssignedMemoryMb() {
        HostInfoFake hostInfo = new HostInfoFake("hostName", 4, 4096, 4, 2, 2048, 2);
        assertTrue(hostInfo.getAssignedMemoryMb() == 2048);
    }

    @Test
    public void getAssignedDiskGb() {
        HostInfoFake hostInfo = new HostInfoFake("hostName", 4, 4096, 4, 2, 2048, 2);
        assertTrue(hostInfo.getAssignedDiskGb() == 2);
    }

    @Test
    public void getFreeCpus() {
        HostInfoFake hostInfo = new HostInfoFake("hostName", 4, 4096, 4, 2, 2048, 2);

        //before a reservation
        assertTrue(hostInfo.getFreeCpus() == 2);

        //after a reservation
        hostInfo.setReservedCpus(1);
        assertTrue(hostInfo.getFreeCpus() == 1);
    }

    @Test
    public void getFreeMemoryMb() {
        HostInfoFake hostInfo = new HostInfoFake("hostName", 4, 4096, 4, 2, 2048, 2);

        //before a reservation
        assertTrue(hostInfo.getFreeMemoryMb() == 2048);

        //after a reservation
        hostInfo.setReservedMemoryMb(1024);
        assertTrue(hostInfo.getFreeMemoryMb() == 1024);
    }

    @Test
    public void getFreeDiskGb() {
        HostInfoFake hostInfo = new HostInfoFake("hostName", 4, 4096, 4, 2, 2048, 2);

        //before a reservation
        assertTrue(hostInfo.getFreeDiskGb() == 2);

        //after a reservation
        hostInfo.setReservedDiskGb(1);
        assertTrue(hostInfo.getFreeDiskGb() == 1);
    }

    @Test
    public void resetReserved() {
        HostInfoFake hostInfo = new HostInfoFake("hostName", 4, 4096, 4, 2, 2048, 2);

        //reserve some resources
        hostInfo.setReservedCpus(1);
        hostInfo.setReservedMemoryMb(1024);
        hostInfo.setReservedDiskGb(1);

        //reset the reservations
        hostInfo.resetReserved();

        //make sure that there are no reserved resources
        assertTrue(hostInfo.getReservedCpus() == 0);
        assertTrue(hostInfo.getReservedMemoryMb() == 0);
        assertTrue(hostInfo.getReservedDiskGb() == 0);
    }

    @Test
    public void setGetReservedCpus() {
        HostInfoFake hostInfo = new HostInfoFake("hostName", 4, 4096, 4, 2, 2048, 2);
        hostInfo.setReservedCpus(1);
        assertTrue(hostInfo.getReservedCpus() == 1);
    }

    @Test
    public void paramSetReservedCpusCannotBeNegative() {
        HostInfoFake hostInfo = new HostInfoFake("hostName", 4, 4096, 4, 2, 2048, 2);
        exception.expect(IllegalArgumentException.class);
        hostInfo.setReservedCpus(-1);
    }

    @Test
    public void setGetReservedMemoryMb() {
        HostInfoFake hostInfo = new HostInfoFake("hostName", 4, 4096, 4, 2, 2048, 2);
        hostInfo.setReservedMemoryMb(1024);
        assertTrue(hostInfo.getReservedMemoryMb() == 1024);
    }

    @Test
    public void paramSetReservedMemoryCannotBeNegative() {
        HostInfoFake hostInfo = new HostInfoFake("hostName", 4, 4096, 4, 2, 2048, 2);
        exception.expect(IllegalArgumentException.class);
        hostInfo.setReservedMemoryMb(-1024);
    }

    @Test
    public void setGetReservedDiskGb() {
        HostInfoFake hostInfo = new HostInfoFake("hostName", 4, 4096, 4, 2, 2048, 2);
        hostInfo.setReservedDiskGb(1);
        assertTrue(hostInfo.getReservedDiskGb() == 1);
    }

    @Test
    public void paramSetReservedDiskCannotBeNegative() {
        HostInfoFake hostInfo = new HostInfoFake("hostName", 4, 4096, 4, 2, 2048, 2);
        exception.expect(IllegalArgumentException.class);
        hostInfo.setReservedDiskGb(-1);
    }

    @Test
    public void getFutureLoadIfVMDeployedInHost() {
        HostInfoFake hostInfo = new HostInfoFake("hostName", 4, 4096, 4, 2, 2048, 2);
        ServerLoad futureLoad = hostInfo.getFutureLoadIfVMDeployed(new Vm("vm1", "image", 1, 1024, 1, null, ""));
        assert(futureLoad.getCpuLoad() == 0.75 && futureLoad.getRamLoad() == 0.75 && futureLoad.getDiskLoad() == 0.75);
    }

}
