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

package es.bsc.power_button_presser.models;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public class Vm {

    private final String id;
    private final int cpus;
    private final int ramMb;
    private final int diskGb;
    private final String hostName;

    /**
     * Class constructor.
     * @param id The id of the Vm.
     * @param cpus The number of CPUs.
     * @param ramMb The amount of RAM in MB.
     * @param diskGb The size of the disk in GB.
     * @param hostName The host where the VM is deployed
     */
    public Vm(String id, int cpus, int ramMb, int diskGb, String hostName) {
        validateConstructorParams(cpus, ramMb, diskGb);
        this.id = id;
        this.cpus = cpus;
        this.ramMb = ramMb;
        this.diskGb = diskGb;
        this.hostName = hostName;
    }
    
    public int getCpus() {
        return cpus;
    }

    public int getRamMb() {
        return ramMb;
    }

    public int getDiskGb() {
        return diskGb;
    }
    
    public String getHostname() {
        return hostName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Vm)) {
            return false;
        }
        Vm vm = (Vm) o;
        return id.equals(vm.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

    private void validateConstructorParams(int cpus, int ramMb, int diskGb) {
        Preconditions.checkArgument(cpus > 0, "Argument was %s but expected positive", cpus);
        Preconditions.checkArgument(ramMb > 0, "Argument was %s but expected positive", ramMb);
        Preconditions.checkArgument(diskGb > 0, "Argument was %s but expected positive", diskGb);
    }
    
}
