/**
 * Copyright 2014 University of Leeds
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser;

/**
 * This class stores the energy values for a physical machine and is used to map
 * a machine to a VM.
 *
 * @author Richard
 */
public class Host extends EnergyUsageSource {

    public String id = "";
    /**
     * E_i^0: is the "idle power consumption" in Watts (with zero number of VMs
     * running) E_i^c: power consumption of a CPU cycle (or instruction) E_i^m:
     * power consumption of a memory access E_i^d: power consumption of a disk
     * access E_i^n: power consumption of a network access E_i^v: is the "idle
     * power consumption" of a VM which includes the incremental cost to the
     * hypervisor and the energy use of the OS.
     */
    public double idlePowerConsumption = 0.0; //i.e. 27.1w for an idle laptop.
    /**
     * Is the granularity too fine below? i.e. per cycle is a little much i.e.
     * hard to determine and the value will be really really small.
     */
    public double powerConsumptionCPUCycle = 0.0;
    /**
     * An idea of power consumption scale:
     * http://www.xbitlabs.com/articles/memory/display/ddr3_13.html
     * http://superuser.com/questions/40113/does-installing-larger-ram-means-consuming-more-energy
     * http://www.tomshardware.com/reviews/power-saving-guide,1611-4.html
     */
    public double powerConsumptionMemoryAccess = 0.0;
    public double powerConsumptionDiskAccess = 0.0;
    public double powerConsumptionNetworkAccess = 0.0;
}
