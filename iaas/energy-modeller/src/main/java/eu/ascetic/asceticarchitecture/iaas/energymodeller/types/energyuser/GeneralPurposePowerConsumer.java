/**
 * Copyright 2016 University of Leeds
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
 * allocated to the the general use of the data center. i.e. It could represent
 * a storage node or gateway node or network infrastructure etc.
 *
 * An important similar class is!
 *
 * @see eu.ascetic.monitoring.api.datamodel.host
 *
 * @author Richard Kavanagh
 */
public class GeneralPurposePowerConsumer extends Host {

    /**
     * This creates a new instance of a general whole data-center purposed host
     *
     * @param id The host id
     * @param hostName The host name
     */
    public GeneralPurposePowerConsumer(int id, String hostName) {
        super(id, hostName);
    }

    /**
     * This converts a host list into a general purpose node list. It filters all
     * none general purpose hosts from the list.
     *
     * @param hostList The host list to convert to general purpose.
     * @return The new general purpose host list.
     */
    public static Collection<GeneralPurposePowerConsumer> hostListToGeneralPurposeHostList(Collection<Host> hostList) {
        Collection<GeneralPurposePowerConsumer> answer = new ArrayList<>();
        for (Host current : hostList) {
            if (current.getClass().equals(GeneralPurposePowerConsumer.class)) {
                answer.add((GeneralPurposePowerConsumer) current);
            }
        }
        return answer;
    }  

    /**
     * This converts a host list into a general purpose node list. It filters all
     * none general purpose hosts from the list.
     *
     * @param hostList The host list to convert to general purpose.
     * @return The new general purpose host list.
     */
    public static List<GeneralPurposePowerConsumer> hostListToGeneralPurposeHostList(List<Host> hostList) {
        List<GeneralPurposePowerConsumer> answer = new ArrayList<>();
        for (Host current : hostList) {
            if (current.getClass().equals(GeneralPurposePowerConsumer.class)) {
                answer.add((GeneralPurposePowerConsumer) current);
            }
        }
        return answer;
    }    
    
    /**
     * This converts a general purpose list into a host list.
     *
     * @param generalPurposeHostList The general purpose list to convert to hosts.
     * @return The new host list.
     */
    public static List<Host> generalPurposeHostListToHostList(List<GeneralPurposePowerConsumer> generalPurposeHostList) {
        List<Host> answer = new ArrayList<>();
        answer.addAll(generalPurposeHostList);
        return answer;
    }
    
    /**
     * This converts a general purpose list into a host list.
     *
     * @param generalPurposeHostList The general purpose list to convert to hosts.
     * @return The new host list.
     */
    public static Collection<Host> generalPurposeHostListToHostList(Collection<GeneralPurposePowerConsumer> generalPurposeHostList) {
        Collection<Host> answer = new ArrayList<>();
        answer.addAll(generalPurposeHostList);
        return answer;
    }    

}
