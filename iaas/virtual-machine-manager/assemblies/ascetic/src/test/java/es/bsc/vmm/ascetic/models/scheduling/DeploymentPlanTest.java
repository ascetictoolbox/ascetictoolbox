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

package es.bsc.vmm.ascetic.models.scheduling;

import es.bsc.demiurge.core.models.scheduling.DeploymentPlan;
import es.bsc.demiurge.core.models.scheduling.VmAssignmentToHost;
import es.bsc.demiurge.core.models.vms.Vm;
import es.bsc.demiurge.core.monitoring.hosts.Host;
import es.bsc.demiurge.fake.HostFake;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;


/**
 * Tests for the DeploymentPlan class.
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class DeploymentPlanTest {

    private Vm vm1 = new Vm("vm1", "fakeImage", 1, 1024, 1, "", "");
    private Vm vm2 = new Vm("vm2", "fakeImage", 2, 2048, 2, "", "");

    private Host host1 = new HostFake("host1", 2, 2048, 2, 0, 0, 0);
    private Host host2 = new HostFake("host2", 2, 2048, 2, 0, 0, 0);

    @Test
    public void canBeAppliedReturnsTrueWhenPlanIsFeasible() {
        DeploymentPlan deploymentPlan = new DeploymentPlan(getAssignmentsList(new VmAssignmentToHost(vm1, host1),
                new VmAssignmentToHost(vm2, host2)));
        assertTrue(deploymentPlan.canBeApplied());
    }

    @Test
    public void canBeAppliedReturnsFalseWhenPlanIsNotFeasible() {
        DeploymentPlan deploymentPlan = new DeploymentPlan(getAssignmentsList(new VmAssignmentToHost(vm1, host1),
                new VmAssignmentToHost(vm2, host1)));
        assertFalse(deploymentPlan.canBeApplied());
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
