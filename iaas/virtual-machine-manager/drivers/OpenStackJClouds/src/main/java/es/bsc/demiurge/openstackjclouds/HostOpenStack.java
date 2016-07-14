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

package es.bsc.demiurge.openstackjclouds;

import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import es.bsc.demiurge.core.monitoring.hosts.Host;
import org.jclouds.openstack.nova.v2_0.domain.HostResourceUsage;
import org.jclouds.openstack.nova.v2_0.extensions.HostAdministrationApi;

/**
 * Status of a host of an OpenStack infrastructure.
 * 
 * @author Raimon Bosch (raimon.bosch@bsc.es)
 *
 */
public class HostOpenStack extends Host {
    private OpenStackJclouds openStackJclouds;

    public HostOpenStack(String name, OpenStackJclouds openStackJclouds) {
        super(name);
        this.openStackJclouds = openStackJclouds;
        refreshMonitoringInfo();
    }

    @Override
    public void refreshMonitoringInfo() {
        Optional<? extends HostAdministrationApi> hostAdminApi =
                openStackJclouds.getNovaApi().getHostAdministrationExtensionForZone(openStackJclouds.getZone());
        FluentIterable<? extends HostResourceUsage> hostResourcesInfo = hostAdminApi.get().listResourceUsage(hostname);
        
        HostResourceUsage totalRes = hostResourcesInfo.get(0);
        totalCpus = totalRes.getCpu();
        totalMemoryMb = (double) totalRes.getMemoryMb();
        totalDiskGb = (double) totalRes.getDiskGb();
        
        HostResourceUsage assignedRes = hostResourcesInfo.get(1);
        assignedCpus = (double) assignedRes.getCpu();
        assignedMemoryMb = (double) assignedRes.getMemoryMb();
        assignedDiskGb = (double) assignedRes.getDiskGb();
    }
}