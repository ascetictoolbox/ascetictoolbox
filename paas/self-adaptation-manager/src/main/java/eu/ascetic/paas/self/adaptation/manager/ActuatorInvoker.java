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
package eu.ascetic.paas.self.adaptation.manager;

import eu.ascetic.paas.applicationmanager.model.VM;
import eu.ascetic.paas.self.adaptation.manager.rules.datatypes.Response;
import java.util.List;

/**
 * This is the actuator interface for the self adaption manager.
 *
 * @author Richard Kavanagh
 */
public interface ActuatorInvoker {

    /**
     * This lists which VMs can be added to a deployment in order to make it
     * scale.
     *
     * @param applicationId The application ID
     * @param deploymentId The deployment ID
     * @return The OVF ids that can be used to scale the named deployment
     */
    public abstract List<String> getVmTypesAvailableToAdd(String applicationId, String deploymentId);

    /**
     * This lists which VMs can be added to a deployment in order to make it
     * scale.
     *
     * @param applicationId The application ID
     * @param deploymentId The deployment ID
     * @return The VM ids that can be used to down size the named deployment
     */
    public abstract List<Integer> getVmIdsAvailableToRemove(String applicationId, String deploymentId);

    /**
     * This lists which VMs can be added to a deployment in order to make it
     * scale.
     *
     * @param applicationId The application ID
     * @param deploymentId The deployment ID
     * @return The OVF ids that can be used to down size the named deployment
     */
    public abstract List<String> getVmTypesAvailableToRemove(String applicationId, String deploymentId);    
    
    /**
     * This counts how many VMs have a given deployment type in a set of VMs
     *
     * @param vms The vms to look count
     * @param type The ovf Id of the type of VMs to look for
     * @return The amount of VMs which have a given OVF id
     */
    public int getVMsOfGivenType(List<VM> vms, String type);
    
    /**
     * This adds a vm of a given ovf type to named deployment.
     *
     * @param applicationId The application ID
     * @param deploymentId The deployment ID
     * @param ovfId The OVF id that indicates which VM type to instantiate
     */
    public void addVM(String applicationId, String deploymentId, String ovfId);

    /**
     * This deletes a VM
     *
     * @param application The application the VM is part of
     * @param deployment The id of the deployment instance of the VM
     * @param vmID The id of the VM to delete
     */
    public void deleteVM(String application, String deployment, String vmID);    

    /**
     * This causes the actuator to invoke a given action
     * @param response 
     */
    public void actuate(Response response);
    
}
