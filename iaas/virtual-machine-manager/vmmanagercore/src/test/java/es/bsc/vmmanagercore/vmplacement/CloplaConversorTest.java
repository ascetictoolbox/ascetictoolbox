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

import es.bsc.clopla.domain.Vm;
import es.bsc.vmmanagercore.models.vms.VmDeployed;
import es.bsc.vmmanagercore.monitoring.hosts.Host;
import es.bsc.vmmanagercore.monitoring.hosts.HostFake;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Tests for the CloplaConversor class.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class CloplaConversorTest {

    @Test
    public void getCloplaVms() {
        List<VmDeployed> vmsDeployed = new ArrayList<>();
        vmsDeployed.add(new VmDeployed("vm", "fakeImage", 1, 1024, 1, 0, "", "appId", "vmId", "172.16.8.1", "ACTIVE",
                new Date(), "host1"));
        Vm cloplaVm = CloplaConversor.getCloplaVms(vmsDeployed, new ArrayList<es.bsc.vmmanagercore.models.vms.Vm>(),
                new ArrayList<es.bsc.clopla.domain.Host>(), false).get(0);
        assertEquals(1, cloplaVm.getNcpus());
        assertEquals(1024, cloplaVm.getRamMb());
        assertEquals(1, cloplaVm.getDiskGb());
        assertEquals("appId", cloplaVm.getAppId());
        assertEquals("vmId", cloplaVm.getAlphaNumericId());
        assertNull(cloplaVm.getHost());
    }

    @Test
    public void getCloplaHosts() {
        List<Host> hosts = new ArrayList<>();
        hosts.add(new HostFake("host1", 1, 1024, 1, 0, 0, 0));
        es.bsc.clopla.domain.Host cloplaHost = CloplaConversor.getCloplaHosts(hosts).get(0);
        assertEquals("host1", cloplaHost.getHostname());
        assertEquals(1, cloplaHost.getNcpus());
        assertEquals(1024.0, cloplaHost.getRamMb());
        assertEquals(1.0, cloplaHost.getDiskGb());
    }

}
