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
 * This class represents the utilisation history of a VM that is based upon the
 * time since the VM booted.
 *
 * @author Richard Kavanagh
 */
public class VmLoadHistoryBootRecord extends VmLoadHistoryRecord {

    /**
     * This holds the fields: SELECT start_idx, sum_cpu_load_val /
     * count_cpu_load_val as average, STDDEV_POP(cpu_load) as standardDev
     *
     * the utilisation factor value is generic and could be for any given
     * device, not just the CPU.
     */
    private final int index;

    /**
     *
     * @param index The index value that represents the time from boot.
     * @param utilisation The average utilisation induced.
     * @param stdDev The standard deviation, used to give a notion of spread
     * from the average (lower is better)
     */
    public VmLoadHistoryBootRecord(int index, double utilisation, double stdDev) {
        super(utilisation,stdDev);
        this.index = index;
    }

    /**
     * This gets the index value that represents the time from boot.
     *
     * @return The index value for the discrete time block
     */
    public int getIndex() {
        return index;
    }

    /**
     * This gets the average utilisation induced during a discrete time block
     * that represents the time from boot.
     *
     * @return The average utilisation value for the discrete time block
     */
    @Override
    public double getUtilisation() {
        return super.getUtilisation();
    }

    /**
     * This obtains the population standard deviation and is aimed at showing
     * how good the average is an estimator of the utilisation.
     *
     * @return the stdDev The standard deviation for all samples used in
     * calculating the average.
     */
    @Override
    public double getStdDev() {
        return super.getStdDev();
    }

}
