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

import es.bsc.vmmanagercore.monitoring.hosts.Host;

import java.util.HashMap;
import java.util.Map;

/**
 * CloplaHost factory.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class CloplaHostFactory {

    // Suppress default constructor for non-instantiability
    private CloplaHostFactory() {
        throw new AssertionError();
    }

    // hosts already created
    private static final Map<String, es.bsc.clopla.domain.Host> cloplaHosts = new HashMap<>();

    private static Long cloplaHostId = (long) 0;

    public static es.bsc.clopla.domain.Host getCloplaHost(Host host) {
        es.bsc.clopla.domain.Host result = cloplaHosts.get(host.getHostname());
        if (result != null) {
            return result;
        }
        result = new es.bsc.clopla.domain.Host(cloplaHostId, host.getHostname(), host.getTotalCpus(),
                host.getTotalMemoryMb(), host.getTotalDiskGb(), !host.isOn());
        ++cloplaHostId;
        cloplaHosts.put(host.getHostname(), result);
        return result;
    }

}
