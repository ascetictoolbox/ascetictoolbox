package es.bsc.vmmanagercore.scheduler;

import es.bsc.vmmanagercore.model.Vm;
import es.bsc.vmmanagercore.model.VmDeployed;
import es.bsc.vmmanagercore.monitoring.HostInfo;
import es.bsc.vmmanagercore.monitoring.HostInfoFake;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 *
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class SchedAlgGroupByAppTest {

    private static SchedAlgGroupByApp scheduler;
    private static ArrayList<VmDeployed> vmsDeployed = new ArrayList<>();
    private static ArrayList<HostInfo> hostsInfo = new ArrayList<>();


    private static void setUpHosts() {
        hostsInfo.add(new HostInfoFake("host1", 4, 4096, 8, 1, 2048, 4));
        hostsInfo.add(new HostInfoFake("host2", 2, 4096, 8, 1, 2048, 4));
    }

    // Set up 2 VMs of app1 in host1 and 1 VM of app2 in host2. Also, 1 VM of app2 in host2
    private static void setUpVmsDeployed() {
        vmsDeployed.add(new VmDeployed("vm1", "fakeId", 1, 1024, 1, null, "app1", "id1", "", "", new Date(), "host1"));
        vmsDeployed.add(new VmDeployed("vm2", "fakeId", 1, 1024, 1, null, "app1", "id2", "", "", new Date(), "host1"));
        vmsDeployed.add(new VmDeployed("vm3", "fakeId", 1, 1024, 1, null, "app1", "id3", "", "", new Date(), "host2"));
        vmsDeployed.add(new VmDeployed("vm4", "fakeId", 1, 1024, 1, null, "app2", "id4", "", "", new Date(), "host2"));
    }

    @BeforeClass
    public static void setUp() {
        setUpHosts();
        setUpVmsDeployed();
        scheduler = new SchedAlgGroupByApp(vmsDeployed);
    }

    @Test
    public void oneHostWithMoreVmsOfTheSameApp() {
        // Schedule a VM that belongs to app1
        String hostChosen = scheduler.chooseHost(hostsInfo, new Vm("TestVM1", "fakeId", 1, 1024, 1, null, "app1"));
        // The host chosen should be host1 because there are 2 VMs that belong to app1 there and only 1 in host2
        assertEquals("host1", hostChosen);

        // Schedule a Vm that belongs to app2
        hostChosen = scheduler.chooseHost(hostsInfo, new Vm("TestVM1", "fakeId", 1, 1024, 1, null, "app2"));
        // The host chosen should be host2 because there is 1 VM that belongs to app2 there and 0 in host1
        assertEquals("host2", hostChosen);
    }

}
