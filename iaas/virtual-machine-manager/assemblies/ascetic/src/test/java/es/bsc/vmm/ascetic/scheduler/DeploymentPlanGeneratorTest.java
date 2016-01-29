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
import es.bsc.demiurge.core.scheduler.DeploymentPlanGenerator;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DeploymentPlanGeneratorTest {

    @Test
    public void getAllPossibleDeploymentPlans() {
        // Create fake hosts {name, totalCpus, totalRamMb, totalDiskGb,
        // usedCpus, usedRamMb, usedDiskGb}
        List<Host> hosts = new ArrayList<>();
        hosts.add(new HostFake("host1", 8, 8192, 8, 0, 0, 0));
        hosts.add(new HostFake("host2", 4, 4096, 4, 0, 0, 0));
        hosts.add(new HostFake("host3", 2, 2048, 2, 0, 0, 0));

        // Create VMs
        List<Vm> vms = new ArrayList<>();
        vms.add(new Vm("vm1", "image", 1, 1024, 1, null, ""));
        vms.add(new Vm("vm2", "image", 4, 4096, 4, null, ""));

        // vm1 could be deployed in host1, host2, and host3. vm2 could only be deployed in host1, and host2.
        // Therefore, there are six possibilities when the two VMs are deployed together: deploy both in host1,
        // deploy both in host2, deploy one in host1 and the other in host2, etc.
        // Out of those 6 possibilities there is one that is not possible: we cannot deploy vm1 and vm2 both in
        // host2.
        List<DeploymentPlan> deploymentPlans = new DeploymentPlanGenerator().getPossibleDeploymentPlans(vms, hosts);
        assertTrue(deploymentPlans.size() == 5);
        for (DeploymentPlan deploymentPlan: deploymentPlans) {
            boolean vm1AssignedToHost2 = false;
            boolean vm2AssignedToHost2 = false;
            for (VmAssignmentToHost vmAssignmentToHost: deploymentPlan.getVmsAssignationsToHosts()) {
                if (vmAssignmentToHost.getVm().getName().equals("vm1") &&
                        vmAssignmentToHost.getHost().getHostname().equals("host2")) {
                    vm1AssignedToHost2 = true;
                }
                else if (vmAssignmentToHost.getVm().getName().equals("vm2") &&
                        vmAssignmentToHost.getHost().getHostname().equals("host2")) {
                    vm2AssignedToHost2 = true;
                }
            }
            assertFalse(vm1AssignedToHost2 && vm2AssignedToHost2);
        }
    }

}