/**
 * Copyright 2016 University of Leeds
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
 * VM Load history records are intended to show the average utilisation of a VM
 * along with the standard deviation as a measure of its reliability as a
 * estimate of workload.
 *
 * @author Richard Kavanagh
 */
public class VmLoadHistoryRecord {

    private double utilisation;
    private double stdDev;

    /**
     * This creates an average cpu utilisation record.
     *
     * @param utilisation The utilisation recorded
     * @param stdDev The standard deviation as an indication of how good the
     * predictor is (low spread means more accurate estimates).
     */
    public VmLoadHistoryRecord(double utilisation, double stdDev) {
        this.utilisation = utilisation;
        this.stdDev = stdDev;
    }

    /**
     * This gets the average cpu utilisation data for this record.
     *
     * @return The cpu utilisation.
     */
    public double getUtilisation() {
        return utilisation;
    }

    /**
     * This obtains the population standard deviation and is aimed at showing
     * how good the average is an estimator of the load.
     *
     * @return the stdDev The standard deviation for all samples used in
     * calculating the average.
     */
    public double getStdDev() {
        return stdDev;
    }

    /**
     * This sets the average cpu utilisation data for this record.
     * @param utilisation the utilisation to set
     */
    public void setUtilisation(double utilisation) {
        this.utilisation = utilisation;
    }

    /*
     * This sets the population standard deviation and is aimed at showing
     * how good the average is an estimator of the load.
     * @param stdDev the stdDev to set
     */
    public void setStdDev(double stdDev) {
        this.stdDev = stdDev;
    }

}
