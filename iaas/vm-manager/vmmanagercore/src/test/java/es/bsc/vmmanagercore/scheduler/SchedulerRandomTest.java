package es.bsc.vmmanagercore.scheduler;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import es.bsc.vmmanagercore.model.Vm;
import es.bsc.vmmanagercore.monitoring.HostInfo;
import es.bsc.vmmanagercore.monitoring.HostInfoFake;

public class SchedulerRandomTest {

    private SchedulerRandom scheduler;

    @Before
    public void setUp() {
        scheduler = new SchedulerRandom();
    }

    @Test
    public void schedule() {
        // Build the array of hosts that will be passed to the schedule function (both have enough
        // resources available)
        ArrayList<HostInfo> hostsInfo = new ArrayList<>();
        hostsInfo.add(new HostInfoFake("host1", 4, 4096, 8, 1, 2048, 4));
        hostsInfo.add(new HostInfoFake("host2", 4, 4096, 8, 1, 2048, 2));

        // Create a VM with {cpus=1, memory=1GB, disk=1GB}
        ArrayList<Vm> vmDescriptions = new ArrayList<>();
        Vm vmDescription = new Vm("TestVM1", "fakeImageId", 1, 1024, 1, null, "app1");
        vmDescriptions.add(vmDescription);

        // Schedule the VM
        HashMap<Vm, String> scheduleResult = scheduler.schedule(vmDescriptions, hostsInfo);

        // We can only make sure that the VM was scheduled in one of the hosts
        assertTrue("host1".equals(scheduleResult.get(vmDescription)) ||
                "host2".equals(scheduleResult.get(vmDescription)));

    }

}
