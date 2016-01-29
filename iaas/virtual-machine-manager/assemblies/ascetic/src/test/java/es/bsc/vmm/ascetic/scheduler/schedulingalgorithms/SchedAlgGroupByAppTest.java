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

package es.bsc.vmm.ascetic.scheduler.schedulingalgorithms;

import es.bsc.demiurge.core.models.scheduling.DeploymentPlan;
import es.bsc.demiurge.core.models.scheduling.VmAssignmentToHost;
import es.bsc.demiurge.core.models.vms.Vm;
import es.bsc.demiurge.core.models.vms.VmDeployed;
import es.bsc.demiurge.core.monitoring.hosts.Host;
import es.bsc.demiurge.fake.HostFake;
import es.bsc.demiurge.core.scheduler.schedulingalgorithms.SchedAlgGroupByApp;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 *
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
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
        vmsDeployed.add(new VmDeployed("vm1", "fakeId", 1, 1024, 1, 0, null, 
                "app1", "id1", "", "", new Date(), "host1"));
        vmsDeployed.add(new VmDeployed("vm2", "fakeId", 1, 1024, 1, 0, null, 
                "app1", "id2", "", "", new Date(), "host1"));
        vmsDeployed.add(new VmDeployed("vm3", "fakeId", 1, 1024, 1, 0, null, 
                "app1", "id3", "", "", new Date(), "host2"));
        vmsDeployed.add(new VmDeployed("vm4", "fakeId", 1, 1024, 1, 0, null, 
                "app2", "id4", "", "", new Date(), "host2"));
    }

    @BeforeClass
    public static void setUp() {
        setUpHosts();
        setUpVmsDeployed();
        scheduler = new SchedAlgGroupByApp();
    }

    @Test
    public void isBetterDeploymentPlan() {
        // Create VMs
        List<Vm> vms = new ArrayList<>();
        vms.add(new Vm("vm1", "image", 2, 2048, 2, null, "app1"));
        vms.add(new Vm("vm2", "image", 1, 1024, 1, null, "app2"));
        vms.add(new Vm("vm3", "image", 1, 1024, 1, null, "app1"));

        // Create deployment plans
        List<VmAssignmentToHost> assignmentsPlan1 = new ArrayList<>();
        assignmentsPlan1.add(new VmAssignmentToHost(vms.get(0), hosts.get(0))); // vm1 -> host1
        assignmentsPlan1.add(new VmAssignmentToHost(vms.get(1), hosts.get(1))); // vm2 -> host2
        assignmentsPlan1.add(new VmAssignmentToHost(vms.get(2), hosts.get(0))); // vm3 -> host1
        DeploymentPlan deploymentPlan1 = new DeploymentPlan(assignmentsPlan1);
        List<VmAssignmentToHost> assignmentsPlan2 = new ArrayList<>();
        assignmentsPlan2.add(new VmAssignmentToHost(vms.get(0), hosts.get(1))); // vm1 -> host2
        assignmentsPlan2.add(new VmAssignmentToHost(vms.get(1), hosts.get(0))); // vm2 -> host1
        assignmentsPlan2.add(new VmAssignmentToHost(vms.get(2), hosts.get(1))); // vm3 -> host2
        DeploymentPlan deploymentPlan2 = new DeploymentPlan(assignmentsPlan2);

        List<DeploymentPlan> deploymentPlans = new ArrayList<>();
        deploymentPlans.add(deploymentPlan1);
        deploymentPlans.add(deploymentPlan2);

        assertEquals(deploymentPlan1, scheduler.chooseBestDeploymentPlan(vmsDeployed,null,deploymentPlans, hosts, "testId"));
    }

}
