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

import com.google.common.base.Preconditions;
import es.bsc.vmmanagercore.model.vms.Vm;

/**
 * Information about the status of a fake host.
 * This class is useful to perform tests without configuring real hosts.
 * 
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class HostFake extends Host {

    /**
     * Class constructor
     * @param hostname host name
     */
    public HostFake(String hostname) {
        super(hostname);
    }

    /**
     * Class constructor
     * @param hostname host name
     * @param totalCpus total number of CPUs of the host
     * @param totalMemoryMb total memory of the host (in MB)
     * @param totalDiskGb total disk space of the host (in GB)
     * @param assignedCpus assigned CPUs of the host
     * @param assignedMemoryMb assigned memory of the host (in MB)
     * @param assignedDiskGb assigned disk space of the host (in GB)
     */
    public HostFake(String hostname, int totalCpus, int totalMemoryMb, int totalDiskGb, double assignedCpus,
                    int assignedMemoryMb, int assignedDiskGb) {
        super(hostname);
        checkConstructorParams(totalCpus, totalMemoryMb, totalDiskGb, assignedCpus, assignedMemoryMb, assignedDiskGb);
        this.totalCpus = totalCpus;
        this.totalMemoryMb = totalMemoryMb;
        this.totalDiskGb = totalDiskGb;
        this.assignedCpus = assignedCpus;
        this.assignedMemoryMb = assignedMemoryMb;
        this.assignedDiskGb = assignedDiskGb;
    }

    //TODO: I think this method should be in the class HostInfo.
    private void checkConstructorParams(int totalCpus, int totalMemoryMb, int totalDiskGb, double assignedCpus,
            int assignedMemoryMb, int assignedDiskGb) {
        Preconditions.checkArgument(totalCpus > 0, "The number of total cpus has to be greater than 0");
        Preconditions.checkArgument(totalMemoryMb > 0, "The total memory has to be greater than 0");
        Preconditions.checkArgument(totalDiskGb > 0, "The total disk size has to be greater than 0");
        Preconditions.checkArgument(assignedCpus >= 0, "The number of assigned cpus cannot be negative");
        Preconditions.checkArgument(assignedMemoryMb >= 0, "The amount of assigned memory cannot be negative");
        Preconditions.checkArgument(assignedDiskGb >= 0, "The amount of assigned disk cannot be negative");
    }

    @Override
    public void refreshMonitoringInfo() {
        // Do nothing.
    }

    public void updateAssignedResourcesAfterVmDeployed(Vm vm) {
        assignedCpus += vm.getCpus();
        assignedMemoryMb += vm.getRamMb();
        assignedDiskGb += vm.getDiskGb();
    }

    public void updateAssignedResourcesAfterVmDeleted(Vm vm) {
        assignedCpus -= vm.getCpus();
        assignedMemoryMb -= vm.getRamMb();
        assignedDiskGb -= vm.getDiskGb();
    }

}
