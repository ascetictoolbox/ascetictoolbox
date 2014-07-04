package es.bsc.vmmanagercore.scheduler;

import es.bsc.vmmanagercore.model.DeploymentPlan;
import es.bsc.vmmanagercore.model.Vm;
import es.bsc.vmmanagercore.model.VmAssignmentToHost;
import es.bsc.vmmanagercore.monitoring.HostFake;
import es.bsc.vmmanagercore.monitoring.Host;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class SchedAlgRandomTest {

    private static SchedAlgRandom scheduler;

    @BeforeClass
    public static void setUp() {
        scheduler = new SchedAlgRandom();
    }

    @Test
    public void schedule() {
        // Build the array of hosts that will be passed to the schedule function (both have enough resources)
        List<Host> hostsInfo = new ArrayList<>();
        hostsInfo.add(new HostFake("host1", 4, 4096, 8, 1, 2048, 4));
        hostsInfo.add(new HostFake("host2", 4, 4096, 8, 1, 2048, 2));

        // Create a VM with {cpus=1, memory=1GB, disk=1GB}
        Vm vm = new Vm("TestVM1", "fakeImageId", 1, 1024, 1, null, "app1");

        // Schedule the VM
        String selectedHost = scheduler.chooseHost(hostsInfo, vm);

        // We can only make sure that the VM was scheduled in one of the hosts
        assertTrue("host1".equals(selectedHost) || "host2".equals(selectedHost));
    }

    @Test
    public void isBetterDeploymentPlan() {
        // Create VMs and hosts
        List<Vm> vms = new ArrayList<>();
        vms.add(new Vm("vm1", "image", 2, 2048, 2, null, ""));
        vms.add(new Vm("vm2", "image", 1, 1024, 1, null, ""));
        List<Host> hosts = new ArrayList<>();
        hosts.add(new HostFake("host1", 8, 8192, 8, 1, 1024, 1));
        hosts.add(new HostFake("host2", 4, 4096, 4, 1, 1024, 1));

        // Create deployment plans
        List<VmAssignmentToHost> assignmentsPlan1 = new ArrayList<>();
        assignmentsPlan1.add(new VmAssignmentToHost(vms.get(0), hosts.get(0))); // vm1 -> host1
        assignmentsPlan1.add(new VmAssignmentToHost(vms.get(1), hosts.get(1))); // vm2 -> host2
        DeploymentPlan deploymentPlan1 = new DeploymentPlan(assignmentsPlan1);
        List<VmAssignmentToHost> assignmentsPlan2 = new ArrayList<>();
        assignmentsPlan2.add(new VmAssignmentToHost(vms.get(0), hosts.get(1))); // vm1 -> host2
        assignmentsPlan2.add(new VmAssignmentToHost(vms.get(1), hosts.get(0))); // vm2 -> host1
        DeploymentPlan deploymentPlan2 = new DeploymentPlan(assignmentsPlan2);

        // The result is random, so the only thing that we can check is that the function does not throw
        // an exception
        scheduler.isBetterDeploymentPlan(deploymentPlan2, deploymentPlan1, hosts);
    }

}
