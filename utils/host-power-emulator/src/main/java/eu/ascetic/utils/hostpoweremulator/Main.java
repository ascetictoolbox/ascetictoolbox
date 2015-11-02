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
package eu.ascetic.utils.hostpoweremulator;

/**
 * This is the main entry point for the emulated watt meter. It holds the
 * decision point between emulating a single watt meter and emulating many.
 *
 * @author Richard Kavanagh
 */
public class Main {

    /**
     * This runs the emulation tool.
     *
     * @param args The first argument indicates the host to generate the host
     * power consumption data for, the second argument is optional and indicates
     * the host to clone calibration data from.
     */
    public static void main(String[] args) {
        if (args.length > 0) {
            HostPowerEmulator.main(args);
        } else {
            MultiHostPowerEmulator.main(args);
        }
    }

}
