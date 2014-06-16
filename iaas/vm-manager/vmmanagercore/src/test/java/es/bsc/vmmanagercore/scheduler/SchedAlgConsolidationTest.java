package es.bsc.vmmanagercore.scheduler;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import es.bsc.vmmanagercore.model.Vm;
import es.bsc.vmmanagercore.monitoring.HostInfo;
import es.bsc.vmmanagercore.monitoring.HostInfoFake;

/**
 *
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class SchedAlgConsolidationTest {

    private SchedAlgConsolidation scheduler;

    @Before
    public void setUp() {
        scheduler = new SchedAlgConsolidation();
    }

    @Test
    public void oneHostHasMoreCpuLoad() {
        //create a fake host with total={cpus=4, memory=4GB, disk=8GB} and
        //used={cpus=1, memory=2GB, disk=4GB}
        HostInfoFake hostInfo1 = new HostInfoFake("host1", 4, 4096, 8, 1, 2048, 4);

        //create a fake host with total={cpus=2, memory=4GB, disk=8GB} and
        //used={cpus=1, memory=2GB, disk=4GB}
        HostInfoFake hostInfo2 = new HostInfoFake("host2", 2, 4096, 8, 1, 2048, 4);

        //build the array of hosts that will be passed to the schedule function
        ArrayList<HostInfo> hostsInfo = new ArrayList<>();
        hostsInfo.add(hostInfo1);
        hostsInfo.add(hostInfo2);

        //schedule a VM
        String hostChosen = scheduler.chooseHost(hostsInfo, getBasicVm());

        //after deploying the VM, host1 the CPU load of host1 would be 50% whereas the CPU load
        //of host2 would be 100%, therefore, host2 should be chosen
        assertEquals("host2", hostChosen);

    }

    @Test
    public void oneHostHasMoreMemory() {
        //create a fake host with total={cpus=4, memory=4GB, disk=8GB} and
        //used={cpus=1, memory=2GB, disk=4GB}
        HostInfoFake hostInfo1 = new HostInfoFake("host1", 4, 4096, 8, 1, 2048, 4);

        //create a fake host with total={cpus=4, memory=4GB, disk=8GB} and
        //used={cpus=1, memory=1GB, disk=4GB}
        HostInfoFake hostInfo2 = new HostInfoFake("host2", 4, 4096, 8, 1, 1024, 4);

        //build the array of hosts that will be passed to the schedule function
        ArrayList<HostInfo> hostsInfo = new ArrayList<>();
        hostsInfo.add(hostInfo1);
        hostsInfo.add(hostInfo2);

        //schedule a VM
        String hostChosen = scheduler.chooseHost(hostsInfo, getBasicVm());

        //after deploying the VM, the CPU load of the two hosts should be the same. Also,
        //the memory load of host1 should be 75% whereas the memory load of host2 should be 50%,
        //therefore, host1 should be chosen
        assertEquals("host1", hostChosen);
    }

    @Test
    public void oneHostHasMoreDisk() {
        //create a fake host with total={cpus=4, memory=4GB, disk=8GB} and
        //used={cpus=1, memory=1GB, disk=4GB}
        HostInfoFake hostInfo1 = new HostInfoFake("host1", 4, 4096, 8, 1, 1024, 4);

        //create a fake host with total={cpus=4, memory=4GB, disk=8GB} and
        //used={cpus=1, memory=1GB, disk=2GB}
        HostInfoFake hostInfo2 = new HostInfoFake("host2", 4, 4096, 8, 1, 1024, 2);

        //build the array of hosts that will be passed to the schedule function
        ArrayList<HostInfo> hostsInfo = new ArrayList<>();
        hostsInfo.add(hostInfo1);
        hostsInfo.add(hostInfo2);

        //schedule a VM
        String hostChosen = scheduler.chooseHost(hostsInfo, getBasicVm());

        //after deploying the VM, the CPU and memory load of the two hosts should be the same.
        //The disk load of host1 should be 75% whereas the disk load of host2 should be 50%,
        //therefore, host1 should be chosen
        assertEquals("host1", hostChosen);
    }

    @Test
    public void standardCaseWithThreeHosts() {
        //create a fake host with total={cpus=8, memory=4GB, disk=4GB} and
        //used={cpus=4, memory=1GB, disk=1GB}
        HostInfoFake hostInfo1 = new HostInfoFake("host1", 8, 4096, 4, 4, 1024, 1);

        //create a fake host with total={cpus=6, memory=4GB, disk=4GB} and
        //used={cpus=3, memory=1GB, disk=1GB}
        HostInfoFake hostInfo2 = new HostInfoFake("host2", 6, 4096, 4, 3, 1024, 1);

        //create a fake host with total={cpus=4, memory=4GB, disk=4GB} and
        //used={cpus=1, memory=1GB, disk=1GB}
        HostInfoFake hostInfo3 = new HostInfoFake("host3", 4, 4096, 4, 1, 1024, 1);

        //build the array of hosts that will be passed to the schedule function
        ArrayList<HostInfo> hostsInfo = new ArrayList<>();
        hostsInfo.add(hostInfo1);
        hostsInfo.add(hostInfo2);
        hostsInfo.add(hostInfo3);

        //schedule a VM
        String hostChosen = scheduler.chooseHost(hostsInfo, getBasicVm());

        //after deploying the VM, the CPU load of host1 should be 62.5%, the load of host2 should be
        //66.6%, the load of host3 should be 50%. Therefore, host2 should be chosen
        assertEquals("host2", hostChosen);
    }

    /**
     * Returns a VM with { CPUs = 1, RAM = 1GB, disk = 1GB }
     */
    private Vm getBasicVm() {
        return new Vm("TestVM1", "fakeImageId", 1, 1024, 1, null, "app1");
    }

}
