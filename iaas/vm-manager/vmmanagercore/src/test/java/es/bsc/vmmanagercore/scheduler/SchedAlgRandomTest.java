package es.bsc.vmmanagercore.scheduler;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import es.bsc.vmmanagercore.model.Vm;
import es.bsc.vmmanagercore.monitoring.HostInfo;
import es.bsc.vmmanagercore.monitoring.HostInfoFake;

public class SchedAlgRandomTest {

    private SchedAlgRandom scheduler;

    @Before
    public void setUp() {
        scheduler = new SchedAlgRandom();
    }

    @Test
    public void schedule() {
        // Build the array of hosts that will be passed to the schedule function (both have enough resources)
        ArrayList<HostInfo> hostsInfo = new ArrayList<>();
        hostsInfo.add(new HostInfoFake("host1", 4, 4096, 8, 1, 2048, 4));
        hostsInfo.add(new HostInfoFake("host2", 4, 4096, 8, 1, 2048, 2));

        // Create a VM with {cpus=1, memory=1GB, disk=1GB}
        Vm vm = new Vm("TestVM1", "fakeImageId", 1, 1024, 1, null, "app1");

        // Schedule the VM
        String selectedHost = scheduler.chooseHost(hostsInfo, vm);

        // We can only make sure that the VM was scheduled in one of the hosts
        assertTrue("host1".equals(selectedHost) || "host2".equals(selectedHost));
    }

}
