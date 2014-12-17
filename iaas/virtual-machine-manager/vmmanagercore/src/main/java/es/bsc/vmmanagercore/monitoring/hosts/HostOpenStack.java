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

package es.bsc.vmmanagercore.monitoring.hosts;

import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import es.bsc.vmmanagercore.cloudmiddleware.openstack.OpenStackJclouds;
import org.jclouds.openstack.nova.v2_0.domain.HostResourceUsage;
import org.jclouds.openstack.nova.v2_0.extensions.HostAdministrationApi;

/**
 * Status of a host of an OpenStack infrastructure.
 * 
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class HostOpenStack extends Host {

    private OpenStackJclouds openStackJclouds;

    public HostOpenStack(String name, OpenStackJclouds openStackJclouds) {
        super(name);
        this.openStackJclouds = openStackJclouds;
        initTotalResources();
        initAssignedResources();
    }

    private void initTotalResources() {
        //get the host administration API
        Optional<? extends HostAdministrationApi> hostAdminApi =
                openStackJclouds.getNovaApi().getHostAdministrationExtensionForZone(openStackJclouds.getZone());

        //get the information about the host resources
        FluentIterable<? extends HostResourceUsage> hostResourcesInfo = hostAdminApi.get().listResourceUsage(hostname);

        //get the information about the total resources of the host
        HostResourceUsage totalRes = hostResourcesInfo.get(0);

        //assign total CPU, RAM, and disk
        totalCpus = totalRes.getCpu();
        totalMemoryMb = totalRes.getMemoryMb();
        totalDiskGb = totalRes.getDiskGb();
    }

    private void initAssignedResources() {
        assignedCpus = getAssignedCpus();
        assignedMemoryMb = getAssignedMemoryMb();
        assignedDiskGb = getAssignedDiskGb();
    }

    @Override
    public double getAssignedCpus() {
        //get the host administration API
        Optional<? extends HostAdministrationApi> hostAdminApi =
                openStackJclouds.getNovaApi().getHostAdministrationExtensionForZone(openStackJclouds.getZone());

        //get the information about the host resources
        FluentIterable<? extends HostResourceUsage> hostResourcesInfo = hostAdminApi.get().listResourceUsage(hostname);

        //get the assigned CPUs
        int assignedCpus = hostResourcesInfo.get(1).getCpu();

        //update the class attribute
        updateAssignedCpus(assignedCpus);

        return assignedCpus;
    }

    @Override
    public double getAssignedMemoryMb() {
        //get the host administration API
        Optional<? extends HostAdministrationApi> hostAdminApi =
                openStackJclouds.getNovaApi().getHostAdministrationExtensionForZone(openStackJclouds.getZone());

        //get the information about the host resources
        FluentIterable<? extends HostResourceUsage> hostResourcesInfo = hostAdminApi.get().listResourceUsage(hostname);

        //get the assigned memory
        int assignedMemoryMb = hostResourcesInfo.get(1).getMemoryMb();

        //update the class attribute
        updateAssignedMemoryMb(assignedMemoryMb);

        return assignedMemoryMb;
    }

    @Override
    public double getAssignedDiskGb() {
        //get the host administration API
        Optional<? extends HostAdministrationApi> hostAdminApi =
                openStackJclouds.getNovaApi().getHostAdministrationExtensionForZone(openStackJclouds.getZone());

        //get the information about the host resources
        FluentIterable<? extends HostResourceUsage> hostResourcesInfo = hostAdminApi.get().listResourceUsage(hostname);

        //get the assigned disk
        int assignedDiskGb = hostResourcesInfo.get(1).getDiskGb();

        //update the class attribute
        updateAssignedDiskGb(assignedDiskGb);

        return assignedDiskGb;
    }

    /**
     * @return number of available CPUs of the host
     */
    @Override
    public double getFreeCpus() {
        return totalCpus - getAssignedCpus();
    }

    /**
     * @return available memory of the host (in MB)
     */
    @Override
    public double getFreeMemoryMb() {
        return totalMemoryMb - getAssignedMemoryMb();
    }

    /**
     * @return available disk space of the host (in GB)
     */
    @Override
    public double getFreeDiskGb() {
        return totalDiskGb - getAssignedDiskGb();
    }

    @Override
    public void refreshMonitoringInfo() {
        initTotalResources();
        initAssignedResources();
    }

    public void setOpenStackJclouds(OpenStackJclouds openStackJclouds) {
        this.openStackJclouds = openStackJclouds;
    }

}
