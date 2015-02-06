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

import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.ioutils.GenericLogger;
import eu.ascetic.ioutils.ResultsStore;
import java.io.File;

/**
 *
 * @author Richard
 */
public class HostPowerLogger extends GenericLogger<HostPowerLogger.Pair> {

    /**
     * This creates a new host power logger
     *
     * @param file The file to write the log out to.
     * @param overwrite If the file should be overwritten on starting the energy
     * modeller.
     */
    public HostPowerLogger(File file, boolean overwrite) {
        super(file, overwrite);
        saveFile.setDelimeter(" ");
    }

    @Override
    public void writeHeader(ResultsStore store) {
        store.setDelimeter(" ");
    }

    @Override
    public void writebody(Pair item, ResultsStore store) {
        store.setDelimeter(" ");
        store.add(item.host.getHostName());
            store.append("power");
            store.append(item.getPower());
    }

    /**
     * This binds a host energy record to VM load fraction information. Thus
     * allowing for the calculations to take place.
     */
    public class Pair {

        private final Host host;
        private final double power;

        /**
         * This creates a new pair object that links host energy records toVM
         * load fraction data.
         *
         * @param host The host energy record
         * @param power The estimated power usage of the host.
         */
        public Pair(Host host, double power) {
            this.host = host;
            this.power = power;
        }

        /**
         * The host that the power has been estimated for.
         *
         * @return the host The host
         */
        public Host getHost() {
            return host;
        }

        /**
         * The estimated power usage of a host.
         *
         * @return the power
         */
        public double getPower() {
            return power;
        }

    }

}
