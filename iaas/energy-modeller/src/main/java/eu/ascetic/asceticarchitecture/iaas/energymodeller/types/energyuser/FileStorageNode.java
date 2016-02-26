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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class stores the basic data for physical machine, that has been
 * allocated to the use of the distributed file system (DFS). This represents a
 * DFS host in the energy modeller.
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

    /**
     * This converts a host list into a file storage node list. It filters all
     * none file storage hosts from the list.
     *
     * @param hostList The host list to convert to file storage.
     * @return The new File storage list.
     */
    public static Collection<FileStorageNode> hostListToFileStorageList(Collection<Host> hostList) {
        Collection<FileStorageNode> answer = new ArrayList<>();
        for (Host current : hostList) {
            if (current.getClass().equals(FileStorageNode.class)) {
                answer.add((FileStorageNode) current);
            }
        }
        return answer;
    }  

    /**
     * This converts a host list into a file storage node list. It filters all
     * none file storage hosts from the list.
     *
     * @param hostList The host list to convert to file storage.
     * @return The new File storage list.
     */
    public static List<FileStorageNode> hostListToFileStorageList(List<Host> hostList) {
        List<FileStorageNode> answer = new ArrayList<>();
        for (Host current : hostList) {
            if (current.getClass().equals(FileStorageNode.class)) {
                answer.add((FileStorageNode) current);
            }
        }
        return answer;
    }    
    
    /**
     * This converts a file storage list into a host list.
     *
     * @param fileList The file storage list to convert to hosts.
     * @return The new host list.
     */
    public static List<Host> fileStorageListToHostList(List<FileStorageNode> fileList) {
        List<Host> answer = new ArrayList<>();
        answer.addAll(fileList);
        return answer;
    }
    
    /**
     * This converts a file storage list into a host list.
     *
     * @param fileList The file storage list to convert to hosts.
     * @return The new host list.
     */
    public static Collection<Host> fileStorageListToHostList(Collection<FileStorageNode> fileList) {
        Collection<Host> answer = new ArrayList<>();
        answer.addAll(fileList);
        return answer;
    }    

}
