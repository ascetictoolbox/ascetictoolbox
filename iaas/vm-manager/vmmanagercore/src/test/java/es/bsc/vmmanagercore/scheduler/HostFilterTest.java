package es.bsc.vmmanagercore.scheduler;

import es.bsc.vmmanagercore.model.DeploymentPlan;
import es.bsc.vmmanagercore.model.Vm;
import es.bsc.vmmanagercore.model.VmAssignmentToHost;
import es.bsc.vmmanagercore.monitoring.HostInfo;
import es.bsc.vmmanagercore.monitoring.HostInfoFake;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HostFilterTest {

    @Test
    public void filter() {
        // Create fake hosts {name, totalCpus, totalRamMb, totalDiskGb,
        // usedCpus, usedRamMb, usedDiskGb}
        ArrayList<HostInfo> hosts = new ArrayList<>();
        hosts.add(new HostInfoFake("host1", 4, 4096, 4, 1, 1024, 1)); // OK
        hosts.add(new HostInfoFake("host2", 4, 4096, 4, 3, 1024, 1)); // not enough CPUs
        hosts.add(new HostInfoFake("host3", 4, 4096, 4, 1, 3072, 1)); // not enough RAM
        hosts.add(new HostInfoFake("host4", 4, 4096, 4, 1, 1024, 3)); // not enough disk

        // Filter hosts and check that only host1 is in the result
        List<HostInfo> filteredHosts = HostFilter.filter(hosts, 2, 2048, 2);
        assertTrue(filteredHosts.size() == 1 && filteredHosts.get(0).getHostname().equals("host1"));
    }

    @Test
    public void getAllPossibleDeploymentPlans() {
        // Create fake hosts {name, totalCpus, totalRamMb, totalDiskGb,
        // usedCpus, usedRamMb, usedDiskGb}
        List<HostInfo> hosts = new ArrayList<>();
        hosts.add(new HostInfoFake("host1", 8, 8192, 8, 0, 0, 0));
        hosts.add(new HostInfoFake("host2", 4, 4096, 4, 0, 0, 0));
        hosts.add(new HostInfoFake("host3", 2, 2048, 2, 0, 0, 0));

        // Create VMs
        List<Vm> vms = new ArrayList<>();
        vms.add(new Vm("vm1", "image", 1, 1024, 1, null, ""));
        vms.add(new Vm("vm2", "image", 4, 4096, 4, null, ""));

        // vm1 could be deployed in host1, host2, and host3. vm2 could only be deployed in host1, and host2.
        // Therefore, there are six possibilities when the two VMs are deployed together: deploy both in host1,
        // deploy both in host2, deploy one in host1 and the other in host2, etc.
        // Out of those 6 possibilities there is one that is not possible: we cannot deploy vm1 and vm2 both in
        // host2.
        List<DeploymentPlan> deploymentPlans = new HostFilter().getAllPossibleDeploymentPlans(vms, hosts);
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