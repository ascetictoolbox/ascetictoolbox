package es.bsc.vmmanagercore.scheduler;

import es.bsc.vmmanagercore.model.SchedulingAlgorithm;
import es.bsc.vmmanagercore.model.Vm;
import es.bsc.vmmanagercore.model.VmDeployed;
import es.bsc.vmmanagercore.monitoring.HostInfo;
import es.bsc.vmmanagercore.monitoring.HostInfoFake;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.assertNotNull;

/**
 *
 *
 *  @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class SchedulerTest {

    private static Scheduler scheduler = new Scheduler(SchedulingAlgorithm.CONSOLIDATION, new ArrayList<VmDeployed>());

    @Test
    public void vmShouldBeDeployedEvenWhenThereAreNoHostsWithEnoughResources() {
        ArrayList<HostInfo> hosts = new ArrayList<>();
        hosts.add(new HostInfoFake("host1", 2, 2048, 2, 1, 2048, 2)); // Host with 100% load

        // VM to deploy
        ArrayList<Vm> vms = new ArrayList<>();
        Vm vm = new Vm("TestVM1", "fakeImageId", 1, 1024, 1, null, "app1");
        vms.add(vm);

        // Schedule and make sure that a host was assigned
        HashMap<Vm, String> selectedHost = scheduler.schedule(vms, hosts);
        assertNotNull(selectedHost.get(vm));
    }

}
