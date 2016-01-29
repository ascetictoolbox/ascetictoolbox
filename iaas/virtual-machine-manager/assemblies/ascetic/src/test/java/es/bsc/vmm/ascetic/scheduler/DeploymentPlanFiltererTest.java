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

import es.bsc.demiurge.core.models.scheduling.DeploymentPlan;
import es.bsc.demiurge.core.models.scheduling.VmAssignmentToHost;
import es.bsc.demiurge.core.models.vms.Vm;
import es.bsc.demiurge.core.monitoring.hosts.Host;
import es.bsc.demiurge.fake.HostFake;
import es.bsc.demiurge.core.scheduler.DeploymentPlanFilterer;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * Tests for the DeploymentPlanFilterer class.
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class DeploymentPlanFiltererTest {

    /**
     * This method tests a simple case with two VMs and two hosts.
     */
    @Test
    public void filterDeploymentPlans() {
        // Create the VMs
        Vm vm1 = new Vm("vm1", "fakeImage", 1, 1024, 1, "", "");
        Vm vm2 = new Vm("vm2", "fakeImage", 2, 2048, 2, "", "");

        // Create the Hosts
        Host host1 = new HostFake("host1", 2, 2048, 2, 0, 0, 0);
        Host host2 = new HostFake("host2", 2, 2048, 2, 0, 0, 0);

        // Create the VM assignations to hosts
        VmAssignmentToHost vm1ToHost1 = new VmAssignmentToHost(vm1, host1);
        VmAssignmentToHost vm1ToHost2 = new VmAssignmentToHost(vm1, host2);
        VmAssignmentToHost vm2ToHost1 = new VmAssignmentToHost(vm2, host1);
        VmAssignmentToHost vm2ToHost2 = new VmAssignmentToHost(vm2, host2);

        // Create the deployment plans
        List<DeploymentPlan> deploymentPlans = new ArrayList<>();
        DeploymentPlan dp1 = new DeploymentPlan(getAssignmentsList(vm1ToHost1, vm2ToHost1));
        deploymentPlans.add(dp1);
        DeploymentPlan dp2 = new DeploymentPlan(getAssignmentsList(vm1ToHost1, vm2ToHost2));
        deploymentPlans.add(dp2);
        DeploymentPlan dp3 = new DeploymentPlan(getAssignmentsList(vm1ToHost2, vm2ToHost1));
        deploymentPlans.add(dp3);
        DeploymentPlan dp4 = new DeploymentPlan(getAssignmentsList(vm1ToHost2, vm2ToHost2));
        deploymentPlans.add(dp4);

        // Filter the deployment plans
        List<DeploymentPlan> filteredDeploymentPlans = DeploymentPlanFilterer.filterDeploymentPlans(deploymentPlans);

        // Make sure that the plans were filtered correctly
        assertTrue(filteredDeploymentPlans.contains(dp2) && filteredDeploymentPlans.contains(dp3));
        assertTrue(!filteredDeploymentPlans.contains(dp1) && !filteredDeploymentPlans.contains(dp4));
    }

    @Test
    public void filterDeploymentPlansThatUseHost() {
        // Create the VMs
        Vm vm1 = new Vm("vm1", "fakeImage", 1, 1024, 1, "", "");
        Vm vm2 = new Vm("vm2", "fakeImage", 2, 2048, 2, "", "");

        // Create the Hosts
        Host host1 = new HostFake("host1", 2, 2048, 2, 0, 0, 0);
        Host host2 = new HostFake("host2", 2, 2048, 2, 0, 0, 0);

        // Create the VM assignations to hosts
        VmAssignmentToHost vm1ToHost1 = new VmAssignmentToHost(vm1, host1);
        VmAssignmentToHost vm2ToHost1 = new VmAssignmentToHost(vm2, host1);
        VmAssignmentToHost vm2ToHost2 = new VmAssignmentToHost(vm2, host2);

        // Create the deployment plans
        List<DeploymentPlan> deploymentPlans = new ArrayList<>();
        DeploymentPlan dp1 = new DeploymentPlan(getAssignmentsList(vm1ToHost1, vm2ToHost1));
        deploymentPlans.add(dp1);
        DeploymentPlan dp2 = new DeploymentPlan(getAssignmentsList(vm1ToHost1, vm2ToHost2));
        deploymentPlans.add(dp2);

        // Filter the deployment plans containing "host2"
        List<DeploymentPlan> planWithoutHost2 =
                DeploymentPlanFilterer.filterDeploymentPlansThatUseHost(deploymentPlans, "host2");

        assertTrue(planWithoutHost2.contains(dp1));
        assertFalse(planWithoutHost2.contains(dp2));
    }

    /**
     * Returns a list of VM assignments to hosts from two assignments.
     *
     * @param assignment1 the first VM assignation to host
     * @param assignment2 the second VM assignation to host
     * @return the list
     */
    private List<VmAssignmentToHost> getAssignmentsList(VmAssignmentToHost assignment1,
                                                        VmAssignmentToHost assignment2) {
        List<VmAssignmentToHost> result = new ArrayList<>();
        result.add(assignment1);
        result.add(assignment2);
        return result;
    }

}
