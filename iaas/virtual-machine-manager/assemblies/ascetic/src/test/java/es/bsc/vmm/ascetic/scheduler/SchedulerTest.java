/**
 Copyright (C) 2013-2014  Barcelona Supercomputing Center

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package es.bsc.vmm.ascetic.scheduler;

import es.bsc.demiurge.core.models.hosts.ServerLoad;
import es.bsc.demiurge.core.models.scheduling.DeploymentPlan;
import es.bsc.demiurge.core.models.scheduling.VmAssignmentToHost;
import es.bsc.demiurge.core.models.vms.Vm;
import es.bsc.demiurge.core.monitoring.hosts.Host;
import es.bsc.demiurge.core.scheduler.Scheduler;
import es.bsc.demiurge.fake.HostFake;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 *  Tests for the Scheduler class.
 *
 *  @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class SchedulerTest {

    @Test
    public void calculateStDevCpuLoad() {
        Collection<ServerLoad> serversLoads = new ArrayList<>();
        serversLoads.add(new ServerLoad(0.3, 0.3, 0.3));
        serversLoads.add(new ServerLoad(0.6, 0.6, 0.6));
        Assert.assertEquals(0.15, Scheduler.calculateStDevCpuLoad(serversLoads), 0.01);
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
        assertEquals(0.5, serversLoad.get("host1").getCpuLoad(), 0.01);
        assertEquals(0.375, serversLoad.get("host1").getRamLoad(), 0.01);
        assertEquals(0.5, serversLoad.get("host1").getDiskLoad(), 0.01);
        assertEquals(0.5, serversLoad.get("host2").getCpuLoad(), 0.01);
        assertEquals(0.5, serversLoad.get("host2").getRamLoad(), 0.01);
        assertEquals(1, serversLoad.get("host2").getDiskLoad(), 0.01);
    }

}
