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
import es.bsc.demiurge.core.scheduler.schedulingalgorithms.SchedAlgRandom;
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
        scheduler.chooseBestDeploymentPlan(new ArrayList<VmDeployed>(),null,deploymentPlans, hosts, "testId");
    }

}
