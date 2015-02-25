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

package es.bsc.vmmanagercore.vmplacement;

import es.bsc.vmmanagercore.model.vms.VmDeployed;
import es.bsc.vmmanagercore.monitoring.hosts.Host;
import es.bsc.vmmanagercore.monitoring.hosts.HostFake;
import es.bsc.clopla.domain.Vm;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Tests for the VmDeployed class.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class OptaVmPlacementConversorTest {

    @Test
    public void getOptaVms() {
        List<VmDeployed> vmsDeployed = new ArrayList<>();
        vmsDeployed.add(new VmDeployed("vm", "fakeImage", 1, 1024, 1, "", "appId", "vmId", "172.16.8.1", "ACTIVE",
                new Date(), "host1"));
        Vm optaVm = OptaVmPlacementConversor.getOptaVms(vmsDeployed, new ArrayList<es.bsc.vmmanagercore.model.vms.Vm>(),
                new ArrayList<es.bsc.clopla.domain.Host>(), false).get(0);
        assertEquals(1, optaVm.getNcpus());
        assertEquals(1024, optaVm.getRamMb());
        assertEquals(1, optaVm.getDiskGb());
        assertEquals("appId", optaVm.getAppId());
        assertEquals("vmId", optaVm.getAlphaNumericId());
        assertNull(optaVm.getHost());
    }

    @Test
    public void getOptaHosts() {
        List<Host> hosts = new ArrayList<>();
        hosts.add(new HostFake("host1", 1, 1024, 1, 0, 0, 0));
        es.bsc.clopla.domain.Host optaHost = OptaVmPlacementConversor.getOptaHosts(hosts).get(0);
        assertEquals("host1", optaHost.getHostname());
        assertEquals(1, optaHost.getNcpus());
        assertEquals(1024.0, optaHost.getRamMb());
        assertEquals(1.0, optaHost.getDiskGb());
    }

}
