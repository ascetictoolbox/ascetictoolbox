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

import static org.junit.Assert.*;

/**
 *
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class SchedAlgDistributionTest {

    private static SchedAlgDistribution scheduler = new SchedAlgDistribution();

    @BeforeClass
    public static void setUp() { }


    @Test
    public void aPlanThatUsesMoreServersIsBetter() {
        // Create VMs and hosts
        List<Vm> vms = new ArrayList<>();
        vms.add(new Vm("vm1", "image", 2, 2048, 2, null, ""));
        vms.add(new Vm("vm2", "image", 1, 1024, 1, null, ""));
        vms.add(new Vm("vm3", "image", 1, 1024, 1, null, ""));
        List<Host> hosts = new ArrayList<>();
        hosts.add(new HostFake("host1", 4, 4096, 4, 1, 1024, 1));
        hosts.add(new HostFake("host2", 4, 4096, 4, 1, 1024, 1));
        hosts.add(new HostFake("host3", 4, 4096, 4, 1, 1024, 1));

        // Create deployment plans
        List<VmAssignmentToHost> assignmentsPlan1 = new ArrayList<>();
        assignmentsPlan1.add(new VmAssignmentToHost(vms.get(0), hosts.get(0))); // vm1 -> host1
        assignmentsPlan1.add(new VmAssignmentToHost(vms.get(1), hosts.get(1))); // vm2 -> host2
        assignmentsPlan1.add(new VmAssignmentToHost(vms.get(2), hosts.get(1))); // vm3 -> host2
        DeploymentPlan deploymentPlan1 = new DeploymentPlan(assignmentsPlan1);
        List<VmAssignmentToHost> assignmentsPlan2 = new ArrayList<>();
        assignmentsPlan2.add(new VmAssignmentToHost(vms.get(0), hosts.get(0))); // vm1 -> host1
        assignmentsPlan2.add(new VmAssignmentToHost(vms.get(1), hosts.get(1))); // vm2 -> host2
        assignmentsPlan2.add(new VmAssignmentToHost(vms.get(2), hosts.get(2))); // vm3 -> host3
        DeploymentPlan deploymentPlan2 = new DeploymentPlan(assignmentsPlan2);

        List<DeploymentPlan> deploymentPlans = new ArrayList<>();
        deploymentPlans.add(deploymentPlan1);
        deploymentPlans.add(deploymentPlan2);

        // DeploymentPlan1 uses 2 servers and DeploymentPlan2 uses 3 servers.
        // Therefore, DeploymentPlan2 is more distributed
        assertEquals(deploymentPlan2, scheduler.chooseBestDeploymentPlan(deploymentPlans, hosts));
    }

    @Test
    public void isBetterPlanReturnsTrueWhenLessStdDevCpuLoad() {
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

        // deploymentPlan1: cpu loads = 0.375 and 0.5
        // deploymentPlan2: cpu loads = 0.75 and 0.25
        // deploymentPlan1 is better (more distributed)
        assertEquals(deploymentPlan1, scheduler.chooseBestDeploymentPlan(deploymentPlans, hosts));
    }

    @Test
    public void isBetterPlanReturnsTrueWhenSameStDevCpuLoadAndLessStdDevRamLoad() {
        // Create VMs and hosts
        List<Vm> vms = new ArrayList<>();
        vms.add(new Vm("vm1", "image", 2, 2048, 1, null, ""));
        vms.add(new Vm("vm2", "image", 2, 1024, 1, null, ""));
        List<Host> hosts = new ArrayList<>();
        hosts.add(new HostFake("host1", 4, 4096, 8, 2, 2048, 1));
        hosts.add(new HostFake("host2", 4, 4096, 4, 2, 1024, 1));

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

        // deploymentPlan1: mem loads = 1 and 0.5
        // deploymentPlan2: mem loads = 0.75 and 0.75
        // deploymentPlan2 is better (more distributed)
        assertEquals(deploymentPlan2, scheduler.chooseBestDeploymentPlan(deploymentPlans, hosts));
    }

    @Test
    public void isBetterPlanReturnsTrueWhenSameStdDevCpuSameStdDevRamAndLessStdDevDiskLoad() {
        // Create VMs and hosts
        List<Vm> vms = new ArrayList<>();
        vms.add(new Vm("vm1", "image", 2, 1024, 2, null, ""));
        vms.add(new Vm("vm2", "image", 2, 1024, 1, null, ""));
        List<Host> hosts = new ArrayList<>();
        hosts.add(new HostFake("host1", 4, 4096, 4, 2, 1024, 2));
        hosts.add(new HostFake("host2", 4, 4096, 4, 2, 1024, 1));

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

        // deploymentPlan1: disk loads = 1 and 0.5
        // deploymentPlan2: disk loads = 0.75 and 0.75
        // deploymentPlan2 is better (more distributed)
        assertEquals(deploymentPlan2, scheduler.chooseBestDeploymentPlan(deploymentPlans, hosts));
    }

}
