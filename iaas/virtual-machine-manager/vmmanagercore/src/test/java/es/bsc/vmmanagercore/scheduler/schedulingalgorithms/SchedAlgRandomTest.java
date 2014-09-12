package es.bsc.vmmanagercore.scheduler.schedulingalgorithms;

import es.bsc.vmmanagercore.model.DeploymentPlan;
import es.bsc.vmmanagercore.model.Vm;
import es.bsc.vmmanagercore.model.VmAssignmentToHost;
import es.bsc.vmmanagercore.monitoring.Host;
import es.bsc.vmmanagercore.monitoring.HostFake;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class SchedAlgRandomTest {

    private static SchedAlgRandom scheduler;

    @BeforeClass
    public static void setUp() {
        scheduler = new SchedAlgRandom();
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

        List<DeploymentPlan> deploymentPlans = new ArrayList<>();
        deploymentPlans.add(deploymentPlan1);
        deploymentPlans.add(deploymentPlan2);

        // The result is random, so the only thing that we can check is that the function does not throw
        // an exception
        //scheduler.isBetterDeploymentPlan(deploymentPlan2, deploymentPlan1, hosts);
        scheduler.chooseBestDeploymentPlan(deploymentPlans, hosts);
    }

}
