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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.calibration;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;

/**
 * This class is contacts hosts and runs a benchmark test on them in order, to
 * generate a profile of energy usage on the given host.
 * @author Richard
 */
public class DefaultLoadGenerator implements LoadGenerator {

    /**
     * This takes a host and contacts it, generates a heavy load and updates the
     * host data.
     * @param host The host to train/update
     * @return The updated host
     */
    @Override
    public Host generateCalibrationData(Host host) {
        //TODO Code goes here for training a host!
        return host;
    }
    
}
