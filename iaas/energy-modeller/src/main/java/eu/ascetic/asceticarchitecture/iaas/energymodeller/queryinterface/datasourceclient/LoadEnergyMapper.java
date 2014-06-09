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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient;

/**
 * This provides the general point of access for the energy modeller 
 * to the energy metrics taken and the load data taken. 
 * It provides synchronisation of the two data source types,
 * such that energy used can be correctly attributed to a given 
 * VM.
 * 
 * The granularity of this needs to be considered carefully, as does
 * the pace at which the values can change. If the instrument can
 * take averages over a period of time, even 5 secs, then we 
 * will get a better synchronisation with the load.
 * @author Richard
 */
public class LoadEnergyMapper {
    
    /**
     * TODO work on synchronising datasets for energy and load 
     */
    
}
