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

package es.bsc.demiurge.core.models.hosts;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

/**
 * Load of a server (% of cpu, ram, and disk used).
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class ServerLoad {

    // All the loads have values in the [0,1] range
    private final double cpuLoad;
    private final double ramLoad;
    private final double diskLoad;

    /**
     * Class constructor.
     *
     * @param cpuLoad the CPU load
     * @param ramLoad the RAM load
     * @param diskLoad the disk load
     */
    public ServerLoad(double cpuLoad, double ramLoad, double diskLoad) {
        this.cpuLoad = cpuLoad;
        this.ramLoad = ramLoad;
        this.diskLoad = diskLoad;
    }

    public double getTotalOverload() {
        return getCpuOverload() + getRamOverload() + getDiskOverload();
    }

    public double getCpuLoad() {
        return cpuLoad;
    }

    public double getRamLoad() {
        return ramLoad;
    }

    public double getDiskLoad() {
        return diskLoad;
    }

    public double getUnusedCpuPerc() {
        return cpuLoad > 1 ? 0 : (1 - cpuLoad);
    }

    public double getUnusedRamPerc() {
        return ramLoad > 1 ? 0 : (1 - ramLoad);
    }

    public double getUnusedDiskPerc() {
        return diskLoad > 1 ? 0 : (1 - diskLoad);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

    private double getCpuOverload() {
        return cpuLoad > 1 ? cpuLoad - 1 : 0;
    }

    private double getRamOverload() {
        return ramLoad > 1 ? ramLoad - 1 : 0;
    }

    private double getDiskOverload() {
        return diskLoad > 1 ? diskLoad - 1 : 0;
    }

    // Note: important consideration. For now, I assume that if a server has load_cpu, load_ram, and load_disk
    // < 5%, then it is idle ("does not have any VMs").
    public boolean isIdle() {
        return cpuLoad < 0.05 && ramLoad < 0.05 && diskLoad < 0.05;
    }

}
