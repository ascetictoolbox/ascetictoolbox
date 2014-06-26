/**
 *  Copyright 2014 University of Leeds
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package eu.ascetic.asceticarchitecture.iaas.energymodeller.types.input;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.TimePeriod;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VmDeployed;

/**
 * This class represents a record of a workload for a virtual machine 
 * that has already or that is currently been executed.
 * 
 * The structure is as follows:
 * 
 * Historic_Workload = <TimePeriod, VM_REF, VM_CPU_USAGE, 
 * VM_Network, Disk_IO, Memory_IO??>
 * 
 * @author Richard
 */
public class VMWorkload {
    
    private TimePeriod duration;
    private VmDeployed virtualMachine;
    /**
     * TODO The exact type of these values may need refining.
     */
    private double cpuUsage;
    private double networkUsage;
    private double diskUsage;
    private double memoryUsage;

    /**
     * @return the duration
     */
    public TimePeriod getDuration() {
        return duration;
    }

    /**
     * @param duration the duration to set
     */
    public void setDuration(TimePeriod duration) {
        this.duration = duration;
    }

    /**
     * @return the virtualMachine
     */
    public VmDeployed getVirtualMachine() {
        return virtualMachine;
    }

    /**
     * @param virtualMachine the virtualMachine to set
     */
    public void setVirtualMachine(VmDeployed virtualMachine) {
        this.virtualMachine = virtualMachine;
    }

    /**
     * @return the cpuUsage
     */
    public double getCpuUsage() {
        return cpuUsage;
    }

    /**
     * @param cpuUsage the cpuUsage to set
     */
    public void setCpuUsage(double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    /**
     * @return the networkUsage
     */
    public double getNetworkUsage() {
        return networkUsage;
    }

    /**
     * @param networkUsage the networkUsage to set
     */
    public void setNetworkUsage(double networkUsage) {
        this.networkUsage = networkUsage;
    }

    /**
     * @return the diskUsage
     */
    public double getDiskUsage() {
        return diskUsage;
    }

    /**
     * @param diskUsage the diskUsage to set
     */
    public void setDiskUsage(double diskUsage) {
        this.diskUsage = diskUsage;
    }

    /**
     * @return the memoryUsage
     */
    public double getMemoryUsage() {
        return memoryUsage;
    }

    /**
     * @param memoryUsage the memoryUsage to set
     */
    public void setMemoryUsage(double memoryUsage) {
        this.memoryUsage = memoryUsage;
    }   
    
}
