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

package es.bsc.vmmclient.models;

import com.google.common.base.MoreObjects;

public class Node {

    private final String hostname;
    private final int totalCpus;
    private final double totalMemoryMb;
    private final double totalDiskGb;
    private final double assignedCpus;
    private final double assignedMemoryMb;
    private final double assignedDiskGb;
    private final double currentPower;

    public Node(String hostname, int totalCpus, double totalMemoryMb, double totalDiskGb, double assignedCpus,
                double assignedMemoryMb, double assignedDiskGb, double currentPower) {
        this.hostname = hostname;
        this.totalCpus = totalCpus;
        this.totalMemoryMb = totalMemoryMb;
        this.totalDiskGb = totalDiskGb;
        this.assignedCpus = assignedCpus;
        this.assignedMemoryMb = assignedMemoryMb;
        this.assignedDiskGb = assignedDiskGb;
        this.currentPower = currentPower;
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

    public double getCurrentPower() {
        return currentPower;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("hostname", hostname)
                .add("totalCpus", totalCpus)
                .add("totalMemoryMb", totalMemoryMb)
                .add("totalDiskGb", totalDiskGb)
                .add("assignedCpus", assignedCpus)
                .add("assignedMemoryMb", assignedMemoryMb)
                .add("assignedDiskGb", assignedDiskGb)
                .add("currentPower", currentPower)
                .toString();
    }
}
