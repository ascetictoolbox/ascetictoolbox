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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;

/**
 *
 * @author Richard
 */
public class DefaultLoadGeneratorTest {

    public DefaultLoadGeneratorTest() {
    }

    /**
     * Test of generateCalibrationData method, of class DefaultLoadGenerator.
     */
    @Test
    public void testGenerateCalibrationData() {
        System.out.println("generateCalibrationData");
        Host host = new Host(10105, "asok09");
        DefaultLoadGenerator instance = new DefaultLoadGenerator();
        instance.generateCalibrationData(host);
        while (instance.isRunning(host)) {
            System.out.println("is Running: " + instance.isRunning(host));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(DefaultLoadGeneratorTest.class.getName()).log(Level.SEVERE, "The data gatherer was interupted.", ex);
            }
        }

    }

}
