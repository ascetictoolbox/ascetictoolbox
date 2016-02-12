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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser;

/**
 * This class stores the basic data for physical machine, that has been 
 * allocated to the use of the distributed file system (DFS). 
 * This represents a DFS host in the energy modeller.
 *
 * An important similar class is!
 *
 * @see eu.ascetic.monitoring.api.datamodel.host
 *
 * @author Richard Kavanagh
 */
public class FileStorageNode extends Host {

    /**
     * This creates a new instance of a DFS host
     *
     * @param id The host id
     * @param hostName The host name
     */    
    public FileStorageNode(int id, String hostName) {
        super(id, hostName);
    }
    
}
