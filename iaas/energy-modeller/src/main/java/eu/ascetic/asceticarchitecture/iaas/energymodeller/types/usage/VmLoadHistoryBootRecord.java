/**
 * Copyright 2015 University of Leeds
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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage;

/**
 * This class represents the load history of a VM that is based upon the time
 * since the VM booted.
 *
 * @author Richard Kavanagh
 */
public class VmLoadHistoryBootRecord {

    /**
     * This holds the fields: SELECT start_idx, sum_cpu_load_val /
     * count_cpu_load_val as average
     *
     * the load factor value is generic and could be for any given device, not
     * just the CPU.
     */
    
    private final int index;
    private final double load;

    /**
     * 
     * @param index The index value that represents the time from boot.
     * @param load The average load induced. 
     */
    public VmLoadHistoryBootRecord(int index, double load) {
        this.index = index;
        this.load = load;
    }

    /**
     * This gets the index value that represents the time from boot.
     * @return The index value for the discrete time block
     */
    public int getIndex() {
        return index;
    }

    /**
     * This gets the average load induced during a discrete time block that 
     * represents the time from boot.
     * @return The average load value for the discrete time block
     */
    public double getLoad() {
        return load;
    }

}
