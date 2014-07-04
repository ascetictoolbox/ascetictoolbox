package es.bsc.vmmanagercore.scheduler;

import es.bsc.vmmanagercore.model.DeploymentPlan;
import es.bsc.vmmanagercore.model.Vm;
import es.bsc.vmmanagercore.model.VmAssignmentToHost;
import es.bsc.vmmanagercore.monitoring.HostInfo;
import es.bsc.vmmanagercore.monitoring.HostInfoFake;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
    public void oneHostHasLessCpuLoad() {
        //create a fake host with total={cpus=4, memory=4GB, disk=8GB} and
        //used={cpus=1, memory=2GB, disk=4GB}
        HostInfoFake hostInfo1 = new HostInfoFake("host1", 4, 4096, 8, 1, 2048, 4);

        //create a fake host with total={cpus=2, memory=4GB, disk=8GB} and
        //used={cpus=1, memory=2GB, disk=4GB}
        HostInfoFake hostInfo2 = new HostInfoFake("host2", 2, 4096, 8, 1, 2048, 4);

        //build the array of hosts that will be passed to the schedule function
        List<HostInfo> hostsInfo = new ArrayList<>();
        hostsInfo.add(hostInfo1);
        hostsInfo.add(hostInfo2);

        //schedule a VM
        String hostChosen = scheduler.chooseHost(hostsInfo, new Vm("TestVM1", "fakeId", 1, 1024, 1, null, "app1"));

        //after deploying the VM, host1 the CPU load of host1 would be 50% whereas the CPU load
        //of host2 would be 100%, therefore, host1 should be chosen
        assertEquals("host1", hostChosen);
    }

    @Test
    public void oneHostHasLessMemory() {
        //create a fake host with total={cpus=4, memory=4GB, disk=8GB} and
        //used={cpus=1, memory=2GB, disk=4GB}
        HostInfoFake hostInfo1 = new HostInfoFake("host1", 4, 4096, 8, 1, 2048, 4);

        //create a fake host with total={cpus=4, memory=4GB, disk=8GB} and
        //used={cpus=1, memory=1GB, disk=4GB}
        HostInfoFake hostInfo2 = new HostInfoFake("host2", 4, 4096, 8, 1, 1024, 4);

        //build the array of hosts that will be passed to the schedule function
        List<HostInfo> hostsInfo = new ArrayList<>();
        hostsInfo.add(hostInfo1);
        hostsInfo.add(hostInfo2);

        //schedule a VM
        String hostChosen = scheduler.chooseHost(hostsInfo, new Vm("TestVM1", "fakeId", 1, 1024, 1, null, "app1"));

        //after deploying the VM, the CPU load of the two hosts should be the same. Also,
        //the memory load of host1 should be 75% whereas the memory load of host2 should be 50%,
        //therefore, host2 should be chosen
        assertEquals("host2", hostChosen);
    }

    @Test
    public void oneHostHasLessDisk() {
        //create a fake host with total={cpus=4, memory=4GB, disk=8GB} and
        //used={cpus=1, memory=1GB, disk=4GB}
        HostInfoFake hostInfo1 = new HostInfoFake("host1", 4, 4096, 8, 1, 1024, 4);

        //create a fake host with total={cpus=4, memory=4GB, disk=8GB} and
        //used={cpus=1, memory=1GB, disk=2GB}
        HostInfoFake hostInfo2 = new HostInfoFake("host2", 4, 4096, 8, 1, 1024, 2);

        //build the array of hosts that will be passed to the schedule function
        List<HostInfo> hostsInfo = new ArrayList<>();
        hostsInfo.add(hostInfo1);
        hostsInfo.add(hostInfo2);

        //schedule a VM
        String hostChosen = scheduler.chooseHost(hostsInfo, new Vm("TestVM1", "fakeId", 1, 1024, 2, null, "app1"));

        //after deploying the VM, the CPU and memory load of the two hosts should be the same.
        //The disk load of host1 should be 75% whereas the disk load of host2 should be 50%,
        //therefore, host2 should be chosen
        assertEquals("host2", hostChosen);
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
        //used={cpus=3, memory=3GB, disk=3GB}
        HostInfoFake hostInfo3 = new HostInfoFake("host3", 4, 4096, 4, 3, 3072, 3);

        //build the array of hosts that will be passed to the schedule function
        List<HostInfo> hostsInfo = new ArrayList<>();
        hostsInfo.add(hostInfo1);
        hostsInfo.add(hostInfo2);
        hostsInfo.add(hostInfo3);

        //schedule a VM
        String hostChosen = scheduler.chooseHost(hostsInfo, new Vm("TestVM1", "fakeId", 1, 1024, 1, null, "app1"));

        //after deploying the VM, the CPU load of host1 should be 62.5%, the load of host2 should be
        //66.6%, the load of host3 should be 100%. Therefore, host1 should be chosen
        assertEquals("host1", hostChosen);
    }

    @Test
    public void returnTrueWhenLessStdDevCpuLoad() {
        // Create VMs and hosts
        List<Vm> vms = new ArrayList<>();
        vms.add(new Vm("vm1", "image", 2, 2048, 2, null, ""));
        vms.add(new Vm("vm2", "image", 1, 1024, 1, null, ""));
        List<HostInfo> hosts = new ArrayList<>();
        hosts.add(new HostInfoFake("host1", 8, 8192, 8, 1, 1024, 1));
        hosts.add(new HostInfoFake("host2", 4, 4096, 4, 1, 1024, 1));

        // Create deployment plans
        List<VmAssignmentToHost> assignmentsPlan1 = new ArrayList<>();
        assignmentsPlan1.add(new VmAssignmentToHost(vms.get(0), hosts.get(0))); // vm1 -> host1
        assignmentsPlan1.add(new VmAssignmentToHost(vms.get(1), hosts.get(1))); // vm2 -> host2
        DeploymentPlan deploymentPlan1 = new DeploymentPlan(assignmentsPlan1);
        List<VmAssignmentToHost> assignmentsPlan2 = new ArrayList<>();
        assignmentsPlan2.add(new VmAssignmentToHost(vms.get(0), hosts.get(1))); // vm1 -> host2
        assignmentsPlan2.add(new VmAssignmentToHost(vms.get(1), hosts.get(0))); // vm2 -> host1
        DeploymentPlan deploymentPlan2 = new DeploymentPlan(assignmentsPlan2);

        // deploymentPlan1: cpu loads = 0.375 and 0.5
        // deploymentPlan2: cpu loads = 0.75 and 0.25
        // deploymentPlan1 is better (more distributed)
        assertTrue(scheduler.isBetterDeploymentPlan(deploymentPlan1, deploymentPlan2, hosts));
    }

    /*
    @Test
    public void returnTrueWhenSameStDevCpuLoadAndLessStdDevRamLoad() {
        // TODO: implement me!
    }

    @Test
    public void returnTrueWhenSameStdDevCpuLoadSameStdDevRamLoadAndLessStdDevDiskLoad() {
        // TODO: implement me!
    }

    @Test
    public void returnFalseWhenIsWorseOption() {
        // TODO: implement me!
    }
    */
}
