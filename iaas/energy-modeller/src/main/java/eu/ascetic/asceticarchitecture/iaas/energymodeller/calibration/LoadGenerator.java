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
 * The aim of this interface is to act as a means of generating load on a 
 * specified host and to gain energy related data from it.
 * @author Richard
 */
public interface LoadGenerator extends Runnable {

    /**
     * This sets the host for the load generator to execute against.
     * @param host The host to generate the load against, asynchronously once the
     * run method is called.
     */
    public void setHost(Host host);
    
    /**
     * This is a means to get other information such as port numbers, names of 
     * web services etc that compliments information such as the host name.
     * @return This returns the domain information.
     */
    public String getDomain();
    
    /**
     * This is a means to set other information such as port numbers, names of 
     * web services etc that compliments information such as the host name.
     * @param domain The domain information to set
     */
    public void setDomain(String domain);
    
    /**
     * This for a given host commands the load generator to perform its benchmarking.
     * @param host The host to generate energy calibration data for.
     */
    public void generateCalibrationData(Host host);
    
}
