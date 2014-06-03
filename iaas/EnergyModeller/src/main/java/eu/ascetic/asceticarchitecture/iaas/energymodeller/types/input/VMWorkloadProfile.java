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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.types.input;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.TimePeriod;

/**
 * This class represents a workload profile for a virtual machine.
 * @author Richard
 */
public class VMWorkloadProfile {
    
    /**
     * TODO this is very much a placeholder for actual information
     * that must be passed into the system to determine the workload
     * of a VM.
     */
    
    /**
     * Either null for ongoing long term jobs providing averages 
     * or a projected period of time for the deployment should be specified.
     */
    public TimePeriod duration;

    public void setDuration(TimePeriod duration) {
        this.duration = duration;
    }

    public TimePeriod getDuration() {
        return duration;
    }
    
    /**
     * This indicates if the workload represents a deployment
     * that has a projected end period or not.
     * @return 
     */
    public boolean isLongTermDeployment () {
        return duration == null;
    }
    
}
