package es.bsc.vmmanagercore.scheduler;

import es.bsc.vmmanagercore.model.DeploymentPlan;
import es.bsc.vmmanagercore.model.Vm;
import es.bsc.vmmanagercore.model.VmAssignmentToHost;
import es.bsc.vmmanagercore.model.VmDeployed;
import es.bsc.vmmanagercore.monitoring.HostFake;
import es.bsc.vmmanagercore.monitoring.Host;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class SchedAlgGroupByAppTest {

    private static SchedAlgGroupByApp scheduler;
    private static List<VmDeployed> vmsDeployed = new ArrayList<>();
    private static List<Host> hosts = new ArrayList<>();

    private static void setUpHosts() {
        hosts.add(new HostFake("host1", 4, 4096, 8, 1, 2048, 4));
        hosts.add(new HostFake("host2", 2, 4096, 8, 1, 2048, 4));
    }

    // Set up 2 VMs of app1 in host1 and 1 VM of app2 in host1. Also, 1 VM of app2 in host2
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
        String hostChosen = scheduler.chooseHost(hosts, new Vm("TestVM1", "fakeId", 1, 1024, 1, null, "app1"));
        // The host chosen should be host1 because there are 2 VMs that belong to app1 there and only 1 in host2
        assertEquals("host1", hostChosen);

        // Schedule a Vm that belongs to app2
        hostChosen = scheduler.chooseHost(hosts, new Vm("TestVM1", "fakeId", 1, 1024, 1, null, "app2"));
        // The host chosen should be host2 because there is 1 VM that belongs to app2 there and 0 in host1
        assertEquals("host2", hostChosen);
    }

    @Test
    public void isBetterDeploymentPlan() {
        // Create VMs and hosts
        List<Vm> vms = new ArrayList<>();
        vms.add(new Vm("vm1", "image", 2, 2048, 2, null, "app1"));
        vms.add(new Vm("vm2", "image", 1, 1024, 1, null, "app2"));

        // Create deployment plans
        List<VmAssignmentToHost> assignmentsPlan1 = new ArrayList<>();
        assignmentsPlan1.add(new VmAssignmentToHost(vms.get(0), hosts.get(0))); // vm1 -> host1
        assignmentsPlan1.add(new VmAssignmentToHost(vms.get(1), hosts.get(1))); // vm2 -> host2
        DeploymentPlan deploymentPlan1 = new DeploymentPlan(assignmentsPlan1);
        List<VmAssignmentToHost> assignmentsPlan2 = new ArrayList<>();
        assignmentsPlan2.add(new VmAssignmentToHost(vms.get(0), hosts.get(1))); // vm1 -> host2
        assignmentsPlan2.add(new VmAssignmentToHost(vms.get(1), hosts.get(0))); // vm2 -> host1
        DeploymentPlan deploymentPlan2 = new DeploymentPlan(assignmentsPlan2);

        // With deployment plan 1, 3 of the existing VMs will be paired with the new ones.
        // With deployment plan 2, only one of the existing VMs will be paired with the new ones.
        assertTrue(scheduler.isBetterDeploymentPlan(deploymentPlan1, deploymentPlan2, hosts));
        assertFalse(scheduler.isBetterDeploymentPlan(deploymentPlan2, deploymentPlan1, hosts));
    }

}
