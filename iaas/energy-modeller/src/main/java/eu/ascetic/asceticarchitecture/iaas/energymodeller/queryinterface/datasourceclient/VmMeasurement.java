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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VmDeployed;

/**
 * This represents a single snapshot of the data from a data source.
 *
 * @author Richard
 */
public class VmMeasurement extends Measurement {

    private VmDeployed vm;

    /**
     * This creates a vm measurement.
     *
     * @param vm The vm the measurement is for
     */
    public VmMeasurement(VmDeployed vm) {
        this.vm = vm;
    }

    /**
     * This creates a vm measurement.
     *
     * @param vm The vm the measurement is for
     * @param clock The time when the measurement was taken
     */
    public VmMeasurement(VmDeployed vm, long clock) {
        this.vm = vm;
        setClock(clock);
    }

    /**
     * This gets the VM that this measurement is for
     * @return The deployed Vm that this measurement is for
     */
    public VmDeployed getVm() {
        return vm;
    }

    /**
     * This sets the VM that this measurement is for
     * @param vm The deployed Vm that this measurement is for
     */
    public void setVm(VmDeployed vm) {
        this.vm = vm;
    }

    @Override
    public String toString() {
        return vm.toString() + " Time: " + getClock() + " Metric Count: " + getMetricCount() + " Clock Diff: " + getMaximumClockDifference();
    }

}
