package eu.ascetic.paas.applicationmanager.vmmanager.datamodel;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

/**
 * 
 * Copyright 2014 ATOS SPAIN S.A. 
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author David Rojo. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.rojoa@atos.net 
 *
 * Load of a server (% of cpu, ram, and disk used).
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class ServerLoad {

    // All the loads have values in the [0,1] range
    private double cpuLoad;
    private double ramLoad;
    private double diskLoad;

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

    public void setCpuLoad(double cpuLoad) {
        this.cpuLoad = cpuLoad;
    }

    public double getRamLoad() {
        return ramLoad;
    }

    public void setRamLoad(double ramLoad) {
        this.ramLoad = ramLoad;
    }

    public double getDiskLoad() {
        return diskLoad;
    }

    public void setDiskLoad(double diskLoad) {
        this.diskLoad = diskLoad;
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

}
