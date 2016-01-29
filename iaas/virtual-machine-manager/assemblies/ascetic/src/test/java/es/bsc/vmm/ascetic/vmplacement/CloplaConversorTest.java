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

package es.bsc.vmm.ascetic.vmplacement;

import es.bsc.demiurge.core.clopla.domain.Vm;

import es.bsc.demiurge.core.models.vms.VmDeployed;
import es.bsc.demiurge.core.monitoring.hosts.Host;
import es.bsc.demiurge.fake.HostFake;
import es.bsc.demiurge.core.vmplacement.CloplaConversor;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Tests for the CloplaConversor class.
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class CloplaConversorTest {

	private CloplaConversor cc = new CloplaConversor();

    @Test
    public void getCloplaVms() {
        List<VmDeployed> vmsDeployed = new ArrayList<>();
        vmsDeployed.add(new VmDeployed("vm", "fakeImage", 1, 1024, 1, 0, "", "appId", "vmId", "172.16.8.1", "ACTIVE",
                new Date(), "host1"));

        Vm cloplaVm = cc.getCloplaVms(vmsDeployed, new ArrayList<es.bsc.demiurge.core.models.vms.Vm>(),
                new ArrayList<es.bsc.demiurge.core.clopla.domain.Host>(), false).get(0);
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
        es.bsc.demiurge.core.clopla.domain.Host cloplaHost = cc.getCloplaHosts(hosts).get(0);
        assertEquals("host1", cloplaHost.getHostname());
        assertEquals(1, cloplaHost.getNcpus());
        assertEquals(1024.0, cloplaHost.getRamMb());
        assertEquals(1.0, cloplaHost.getDiskGb());
    }

}
