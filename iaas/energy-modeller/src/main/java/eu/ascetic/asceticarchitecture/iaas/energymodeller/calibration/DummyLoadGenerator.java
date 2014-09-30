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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.calibration;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;

/**
 * This class sits in place of a mechanism to contact hosts and runs a
 * benchmarks test on them in order, to generate a profile of energy usage on
 * the given host.
 *
 * @author Richard
 */
public class DummyLoadGenerator implements LoadGenerator {

    private Host host;

    /**
     * This creates a dummy load generator. It is intended to sit in place of
     * the actual load generator. It generates no load itself.
     */
    public DummyLoadGenerator() {
    }

    @Override
    public void setHost(Host host) {
        this.host = host;
    }

    @Override
    public void generateCalibrationData(Host host) {
        /**
         * No code here this isn't intended to do anything.
         */
    }

    @Override
    public void run() {
        /**
         * Note the aim of starting a thread is so that the calibrator can take
         * measurements while the load generator is doing its work.
         */
        generateCalibrationData(host);
    }

    @Override
    public String getDomain() {
        return "";
    }

    @Override
    public void setDomain(String domain) {
    }

}
