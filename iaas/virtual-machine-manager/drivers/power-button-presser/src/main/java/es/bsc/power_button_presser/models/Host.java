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

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public class Host {

    private final String hostname;
    private final int totalCpus;
    private final double totalMemoryMb;
    private final double totalDiskGb;
    private final double assignedCpus;
    private final double assignedMemoryMb;
    private final double assignedDiskGb;
    private final boolean turnedOff;

    public Host(String hostname, int totalCpus, double totalMemoryMb, double totalDiskGb, double assignedCpus,
                double assignedMemoryMb, double assignedDiskGb, boolean turnedOff) {
        this.hostname = hostname;
        this.totalCpus = totalCpus;
        this.totalMemoryMb = totalMemoryMb;
        this.totalDiskGb = totalDiskGb;
        this.assignedCpus = assignedCpus;
        this.assignedMemoryMb = assignedMemoryMb;
        this.assignedDiskGb = assignedDiskGb;
        this.turnedOff = turnedOff;
    }

    public String getHostname() {
        return hostname;
    }

    public int getTotalCpus() {
        return totalCpus;
    }

    public double getTotalMemoryMb() {
        return totalMemoryMb;
    }

    public double getTotalDiskGb() {
        return totalDiskGb;
    }

    public double getAssignedCpus() {
        return assignedCpus;
    }

    public double getAssignedMemoryMb() {
        return assignedMemoryMb;
    }

    public double getAssignedDiskGb() {
        return assignedDiskGb;
    }
    
    public boolean isOff() {
        return turnedOff;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
    
}
