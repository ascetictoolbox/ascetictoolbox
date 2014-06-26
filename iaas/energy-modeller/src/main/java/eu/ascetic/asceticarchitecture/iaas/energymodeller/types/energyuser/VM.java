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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser;

import javax.json.JsonObject;

/**
 * This class represents an energy user of the ASCETiC project and in particular
 * a VM that is to be deployed.
 * 
 * It is somehow similar in nature to:
 * @see es.bsc.vmmanagercore.model.Vm
 * @author Richard Kavanagh
 *
 */
public class VM extends EnergyUsageSource {

    private int cpus;
    private int ramMb;
    private int diskGb;
    /**
     *
     * E_i^v: is the "idle power consumption" of a VM which includes the 
     * incremental cost to the hypervisor and the energy use of the OS. 
     */
    private double powerConsumptionVMOverhead = 0.0;    

    /**
     * This creates a VM that represents a energy usage source.
     *
     * @param cpus The number of CPUs.
     * @param ramMb The amount of RAM in MB.
     * @param diskGb The size of the disk in GB.
     */
    public VM(int cpus, int ramMb, int diskGb) {
        this.cpus = cpus;
        this.ramMb = ramMb;
        this.diskGb = diskGb;
    }

    /**
     * A copy constructor, for a VM.
     * @param vm T
     */
    public VM(VM vm) {
        this.cpus = vm.cpus;
        this.ramMb = vm.ramMb;
        this.diskGb = vm.diskGb;        
    }
    
    

    /**
     * 
     * @param json 
     */
    public VM(JsonObject json) {
        this.cpus = json.getJsonNumber("cpus").intValue();
        this.ramMb = json.getJsonNumber("ramMb").intValue();
        this.diskGb = json.getJsonNumber("diskGb").intValue();
    }

    /**
     * 
     * @return 
     */
    public int getCpus() {
        return cpus;
    }

    /**
     * 
     * @param cpus 
     */
    public void setCpus(int cpus) {
        if (cpus <= 0) {
            throw new IllegalArgumentException("The number of cpus has to be greater than 0");
        }
        this.cpus = cpus;
    }

    /**
     * 
     * @return 
     */
    public int getRamMb() {
        return ramMb;
    }

    /**
     * 
     * @param ramMb 
     */
    public void setRamMb(int ramMb) {
        if (ramMb <= 0) {
            throw new IllegalArgumentException("The amount of memory has to be greater than 0");
        }
        this.ramMb = ramMb;
    }

    /**
     * 
     * @return 
     */
    public int getDiskGb() {
        return diskGb;
    }

    /**
     * 
     * @param diskGb 
     */
    public void setDiskGb(int diskGb) {
        if (diskGb <= 0) {
            throw new IllegalArgumentException("The amount of disk size has to be greater than 0");
        }
        this.diskGb = diskGb;
    }

    /**
     * @return the powerConsumptionVMOverhead
     */
    public double getPowerConsumptionVMOverhead() {
        return powerConsumptionVMOverhead;
    }    

    /**
     * @param powerConsumptionVMOverhead the powerConsumptionVMOverhead to set
     */
    public void setPowerConsumptionVMOverhead(double powerConsumptionVMOverhead) {
        if (powerConsumptionVMOverhead <= 0.0) {
            throw new IllegalArgumentException("The overhead due to hosting a VM must not be less than zero.");
        }
        this.powerConsumptionVMOverhead = powerConsumptionVMOverhead;
    } 
    
}