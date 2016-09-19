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
import es.bsc.vmmclient.models.Slot;
import es.bsc.vmmclient.models.VmRequirements;
import eu.ascetic.paas.applicationmanager.model.SLALimits;
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
     * This gets the ovf of a given deployment.
     *
     * @param applicationId The application ID
     * @param deploymentId The deployment ID
     * @return The ovf that describes a given deployment. If the OVF can't be
     * reported by this actuator then null is returned instead.
     */
    public abstract String getOvf(String applicationId, String deploymentId);
    
    /**
     * This gets a VM given its application, deployment and VM ids.
     *
     * @param application The application ID
     * @param deployment The deployment ID
     * @param vmID The VM id
     * @return The VM given the id values specified.
     */
    public VM getVM(String application, String deployment, String vmID);
    
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
     * @param applicationId
     * @param deploymentId
     * @param type The ovf Id of the type of VMs to look for
     * @return The amount of VMs which have a given OVF id
     */
    public int getVmCountOfGivenType(String applicationId, String deploymentId, String type);

    /**
     * This gets the power usage of a VM.
     *
     * @param applicationId The application the VM is part of
     * @param deploymentId The id of the deployment instance of the VM
     * @param vmID The id of the VM to get the measurement for
     * @return The power usage of a named VM. 
     */
    public double getPowerUsageVM(String applicationId, String deploymentId, String vmID);

    /**
     * This gets the power usage of a VM.
     *
     * @param applicationId The application the VM is part of
     * @param deploymentId The id of the deployment instance of the VM
     * @param vmType The id of the VM to get the measurement for
     * @return The power usage of a named VM. 
     */
    public double getAveragePowerUsage(String applicationId, String deploymentId, String vmType);    

    /**
     * This gets the power usage of a VM.
     *
     * @param applicationId The application the VM is part of
     * @param deploymentId The id of the deployment instance of the VM
     * @return The power usage of the named application. 
     */
    public double getTotalPowerUsage(String applicationId, String deploymentId);      
    
    /**
     *  This obtains information regarding the SLA limits of an application
     * that is to be actuated against
     * @param applicationId The application id
     * @param deploymentId The deployment id
     * @return 
     */
    public SLALimits getSlaLimits(String applicationId, String deploymentId);
    
    /**
     * This checks to see how many free slots are available for a VM of a given
     * size.
     * @param requirements The VMs requirements
     * @return The amount of free slots available.
     */
    public List<Slot> getSlots(VmRequirements requirements);
    
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
     * This forces a renegotiation of the SLA terms of a deployment
     * @param applicationId The id of the application that is to be renegotiated
     * @param deploymentId The id of the deployment of the application
     */
    public void renegotiate(String applicationId, String deploymentId);
    
    /**
     * This deletes all VMs of an application
     *
     * @param applicationId The application the VM is part of
     * @param deploymentId The id of the deployment instance of the VM
     */
    public void hardShutdown(String applicationId, String deploymentId);

    /**
     * This scales a VM type to a set amount of VMs
     *
     * @param applicationId The application the VM is part of
     * @param deploymentId The id of the deployment instance of the VM
     * @param response The response to actuator for
     */
    public void horizontallyScaleToNVms(String applicationId, String deploymentId, Response response);

    /**
     * This scales up a named VM. VM types are expected to be in a co-ordinated
     * series, thus allowing a +1 or -1 notion of direction and scaling to be
     * used.
     *
     * @param application The application the VM is part of
     * @param deployment The id of the deployment instance of the VM
     * @param vmID The id of the VM to delete
     */
    public void scaleUpVM(String application, String deployment, String vmID);

    /**
     * This scales down a named VM. VM types are expected to be in a
     * co-ordinated series, thus allowing a +1 or -1 notion of direction and
     * scaling to be used.
     *
     * @param application The application the VM is part of
     * @param deployment The id of the deployment instance of the VM
     * @param vmID The id of the VM to delete
     */
    public void scaleDownVM(String application, String deployment, String vmID);

    /**
     * This causes the actuator to invoke a given action
     *
     * @param response
     */
    public void actuate(Response response);

}
