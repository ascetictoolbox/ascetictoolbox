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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.usage;

/**
 * The aim of this class is to store information regarding the hosts performance
 * profile. It is a generic structure aiming to store arbitrary static
 * performance data, so that it can be directly compared to power consumption of
 * a host.
 *
 * It is to be recorded as a 2-tuple record indicating the type of record it is
 * i.e. "flops" and the value for a given host.
 *
 * @author Richard Kavanagh
 */
public class HostProfileData {

    private String type = "";
    private double value = 0.0;

    /**
     * This creates a new record for storing benchmark data of a host machine.
     */
    public HostProfileData() {
    }

    /**
     * This creates a new record for storing benchmark data of a host machine.
     *
     * @param type Adds a type to this benchmark record.
     * @param value Adds the value to this benchmark record.
     */
    public HostProfileData(String type, double value) {
        this.type = type;
        this.value = value;
    }

    /**
     * This gets the type of this datapoint. i.e. flops, bogomips, MIPS,
     * whetstone, dhrystone, linpack etc.
     *
     * @return The type of this datapoint.
     */
    public String getType() {
        return type;
    }

    /**
     * This sets the type of this datapoint. i.e. flops, bogomips, MIPS,
     * whetstone, dhrystone, linpack etc.
     *
     * @param type The type to set.
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Provides the value of the output of the benchmark, data point.
     *
     * @return The value of the datapoint.
     */
    public double getValue() {
        return value;
    }

    /**
     * Sets the value of the output of the benchmark, data point.
     *
     * @param value The value of the datapoint.
     */
    public void setValue(double value) {
        this.value = value;
    }

}
