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

import eu.ascetic.asceticarchitecture.iaas.energymodeller.calibration.client.CalibrationLoadGenerator;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.calibration.client.CalibrationLoadGenerator_Service;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class contacts hosts and runs a benchmark test on them in order, to
 * generate a profile of energy usage on the given host.
 *
 * @author Richard
 */
public class DefaultLoadGenerator implements LoadGenerator {

    private boolean running = true;

    /**
     * This takes a host and contacts it, generates a heavy load. The aim is
     * then to use this load to perform training on.
     *
     * @param host The host to train/update
     */
    @Override
    @SuppressWarnings({"BroadCatchBlock", "TooBroadCatch"})
    public void generateCalibrationData(Host host) {
        //Generate Load
        try { //http://localhost:8080/EnergyModellerCalibrationTool/CalibrationLoadGenerator?WSDL
            URL url = new URL("http://" + host.getHostName() + ":8080/EnergyModellerCalibrationTool/CalibrationLoadGenerator?WSDL");
            CalibrationLoadGenerator_Service service = new CalibrationLoadGenerator_Service(url);
            CalibrationLoadGenerator port = service.getCalibrationLoadGeneratorPort();
            port.induceLoad();
        } catch (Exception ex) {
            Logger.getLogger(DefaultLoadGenerator.class.getName()).log(Level.SEVERE, "The load generator had an error.", ex);
            running = false;
        }

    }

    /**
     * This stops the load generator from running.
     */
    public void stop() {
        running = false;
    }

    @Override
    @SuppressWarnings("empty-statement")
    public void run() {
        /**
         * Note the aim of starting a thread is so that the calibrator can take
         * measurements while the load generator is doing its work.
         */
        while (running);
    }

}
