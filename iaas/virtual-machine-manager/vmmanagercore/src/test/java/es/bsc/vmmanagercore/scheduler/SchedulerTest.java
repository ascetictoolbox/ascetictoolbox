package es.bsc.vmmanagercore.scheduler;

import es.bsc.vmmanagercore.model.*;
import es.bsc.vmmanagercore.monitoring.HostFake;
import es.bsc.vmmanagercore.monitoring.Host;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 *
 *
 *  @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class SchedulerTest {

    private static Scheduler scheduler = new Scheduler(SchedulingAlgorithm.CONSOLIDATION, new ArrayList<VmDeployed>());

    @Test
    public void vmShouldBeDeployedEvenWhenThereAreNoHostsWithEnoughResources() {
        List<Host> hosts = new ArrayList<>();
        hosts.add(new HostFake("host1", 2, 2048, 2, 1, 2048, 2)); // Host with 100% load

        // VM to deploy
        List<Vm> vms = new ArrayList<>();
        Vm vm = new Vm("TestVM1", "fakeImageId", 1, 1024, 1, null, "app1");
        vms.add(vm);

        // Schedule and make sure that a host was assigned
        Map<Vm, String> selectedHost = scheduler.schedule(vms, hosts);
        assertNotNull(selectedHost.get(vm));
    }

    @Test
    public void calculateStDevCpuLoad() {
        Collection<ServerLoad> serversLoads = new ArrayList<>();
        serversLoads.add(new ServerLoad(0.3, 0.3, 0.3));
        serversLoads.add(new ServerLoad(0.6, 0.6, 0.6));
        assertEquals(0.15, Scheduler.calculateStDevCpuLoad(serversLoads), 0.01);
    }

    @Test
    public void calculateStDevMemoryLoad() {
        Collection<ServerLoad> serversLoads = new ArrayList<>();
        serversLoads.add(new ServerLoad(0.2, 0.2, 0.2));
        serversLoads.add(new ServerLoad(0.4, 0.4, 0.4));
        assertEquals(0.1, Scheduler.calculateStDevMemLoad(serversLoads), 0.01);
    }

    @Test
    public void calculateStDevDiskLoad() {
        Collection<ServerLoad> serversLoads = new ArrayList<>();
        serversLoads.add(new ServerLoad(0.2, 0.2, 0.2));
        serversLoads.add(new ServerLoad(0.6, 0.6, 0.6));
        assertEquals(0.2, Scheduler.calculateStDevDiskLoad(serversLoads), 0.01);
    }

    @Test
    public void getServersLoadAfterDeploymentPlan() {
        // Create VMs and hosts
        List<Vm> vms = new ArrayList<>();
        vms.add(new Vm("vm1", "image", 2, 1024, 1, null, ""));
        vms.add(new Vm("vm2", "image", 1, 1024, 3, null, ""));
        vms.add(new Vm("vm3", "image", 1, 1024, 2, null, ""));
        List<Host> hosts = new ArrayList<>();
        hosts.add(new HostFake("host1", 8, 8192, 8, 1, 1024, 1));
        hosts.add(new HostFake("host2", 4, 4096, 4, 1, 1024, 1));

        // Create deployment plan
        List<VmAssignmentToHost> vmAssignments = new ArrayList<>();
        vmAssignments.add(new VmAssignmentToHost(vms.get(0), hosts.get(0)));
        vmAssignments.add(new VmAssignmentToHost(vms.get(1), hosts.get(1)));
        vmAssignments.add(new VmAssignmentToHost(vms.get(2), hosts.get(0)));
        DeploymentPlan deploymentPlan = new DeploymentPlan(vmAssignments);

        // Make sure that the server loads after deployment are correct
        Map<String, ServerLoad> serversLoad =
                Scheduler.getServersLoadsAfterDeploymentPlanExecuted(deploymentPlan, hosts);
        assertTrue(serversLoad.get("host1").getCpuLoad() == 0.5);
        assertTrue(serversLoad.get("host1").getRamLoad() == 0.375);
        assertTrue(serversLoad.get("host1").getDiskLoad() == 0.5);
        assertTrue(serversLoad.get("host2").getCpuLoad() == 0.5);
        assertTrue(serversLoad.get("host2").getRamLoad() == 0.5);
        assertTrue(serversLoad.get("host2").getDiskLoad() == 1);
    }

}
