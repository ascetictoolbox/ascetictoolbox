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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.transformation.machinetovm;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM;

/**
 * This is part of a set of standard tools for manipulating data in the energy
 * modeller. This class provides services that translates real machine's energy
 * usage into a VMs energy usage. Make Override-able in order to switch out
 * rules for this process
 *
 * @author Richard
 */
public class DefaultKPITranslator implements KPITranslatorInterface {

    public DefaultKPITranslator() {
    }

    @Override
    public double getEnergyUsage(Host targetHost, VM targetVM) {
        /**
         * TODO: Need to get the count of VMs and evenly divide energy by VM. A
         * more complicated later version can be added at a later date.
         */
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
