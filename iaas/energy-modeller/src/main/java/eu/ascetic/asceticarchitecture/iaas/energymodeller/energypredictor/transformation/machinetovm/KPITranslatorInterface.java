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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.transformation.machinetovm;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM;

/**
 * Translates the energy used by a host physical machine into 
 * a VMs Energy usage. Thus translating from one KPI set to another. 
 * @author Richard
 */
public interface KPITranslatorInterface {
    
    /**
     * TODO: Work in progress define interface!!!
     * The translation will have to take into account the
     * metrics of the host and translate them into a targetVMs energy
     * usage.
     * @param targetHost
     * @param targetVM
     * @return 
     */
    public double getEnergyUsage(Host targetHost, VM targetVM);
    
}
