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
 * This takes an existing host and calibrates the energy model, for it so that 
 * predictions can be made regarding its energy usage.
 * @author Richard
 */
public class Calibrator {

    private LoadGenerator generator = new DefaultLoadGenerator();
    
    /**
     * This finds the upper and lower bounds of a host.
     * @param host The host to train
     * @return The newly trained/updated host
     */
    public Host calibrateHostEnergyData(Host host) {
        host = readLowestboundForHost(host);
        host = generator.generateCalibrationData(host);
        return host;
    }
    
    /**
     * This looks at the historic log values for a host and finds the lowest
     * value ever read. The aim is to find the minimum bound for how much energy
     * a host uses. This value will then need sharing out among VMs as it is the
     * amount of energy used for the host being switched on.
     * @param host The host to train
     * @return The updated host
     */
    private Host readLowestboundForHost(Host host){
        //TODO Code goes here for training a host!
        /**
         * This is separated from the reset of the calibration procedure as it 
         * can be called again without having to induce load on the host.
         */
        return host;
    }

    /**
     * This sets the load generator used by the calibration module.
     * @param generator the generator to set
     */
    public void setGenerator(DefaultLoadGenerator generator) {
        this.generator = generator;
    }
    
}
