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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface;

/**
 * This provides the standard enumeratable types for queries within the energy
 * modeller framework.
 * @deprecated This was used as an initial idea for understanding how queries 
 * should be constructed.
 * @author Richard
 */
public class QueryTypeInformation {

    /**
     * Three types of query are envisaged, a query that looks at:
     *  how much energy a VM, or infrastructure has used.
     *  how much it is currently using.
     *  and a prediction of its future usage (via a model and the energy predictor).
     */
    public enum QueryType {
        HISTORIC, CURRENT, FUTURE
    };    
    
    /**
     * Granularity is seen as been at the:
     *  * VM level
     *  * For a Set of VMs (a deployment)
     *  * Whole infrastructure level.
     *  * Is set evaluation needed?
     * 
     * Host Energy Consumption
     * VM Level Consumption
     *  * 
     */
    public enum QueryGranularity {
        VM, VM_SET, WHOLE_INFRASTRUCTURE
    };
    
    public enum QueryDuration {
        SNAPSHOT, FIXED_SIZE, USER_SPECIFIED
    };        
    
}
