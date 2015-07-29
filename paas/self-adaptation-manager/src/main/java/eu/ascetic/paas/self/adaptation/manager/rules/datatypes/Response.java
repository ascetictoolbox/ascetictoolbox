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
package eu.ascetic.paas.self.adaptation.manager.rules.datatypes;

import eu.ascetic.paas.self.adaptation.manager.ActuatorInvoker;

/**
 * This class represents a valid response that the self-adaptation manager can
 * use in order to respond to the incoming events.
 * @author Richard Kavanagh
 */
public class Response implements Comparable<Response> {

    private final ActuatorInvoker actuator;
    private final EventData cause;
    private AdaptationType actionType;
    private String adapationDetails;
    private String vmId;
    private boolean performed = false;
    private boolean possibleToAdapt = true;
    
    public enum AdaptationType {
    ADD_VM, REMOVE_VM, ADD_CPU, REMOVE_CPU,
    ADD_MEMORY, REMOVE_MEMORY}

    /**
     * This creates a standard response object. It indicates which actuator
     * to use and which message to send to it.
     * @param actuator The actuator used to invoke the change.
     * @param cause A copy of the incoming event that caused the response to 
     * fire.
     * @param actionType The description of the type of action to take
     */
    public Response(ActuatorInvoker actuator, EventData cause, AdaptationType actionType) {
        this.actuator = actuator;
        this.cause = cause;
        this.actionType = actionType;
    }
    
    /**
     * This gets the time of arrival of the event that caused the response.
     * @return 
     */
    public long getTime() {
        return cause.getTime();
    }

    /**
     * This returns a copy of the event that was the original cause of the 
     * response to be created.
     * @return The event that caused the response (or at least the last in a 
     * sequence of events).
     */
    public EventData getCause() {
        return cause;
    }

    @Override
    public int compareTo(Response response) {
        //This sequences responses in cronlogical order.
        return Long.compare(this.getTime(), response.getTime());
    }   

    /**
     * @return the actionType
     */
    public AdaptationType getActionType() {
        return actionType;
    }

    /**
     * @param actionType the actionType to set
     */
    public void setActionType(AdaptationType actionType) {
        this.actionType = actionType;
    }

    /**
     * @return the adapationDetails
     */
    public String getAdapationDetails() {
        return adapationDetails;
    }

    /**
     * @param adapationDetails the adapationDetails to set
     */
    public void setAdapationDetails(String adapationDetails) {
        this.adapationDetails = adapationDetails;
    }
    
    /**
     * This indicates if on deciding to adapt if a possible solution was found.
     * @return the possibleToAdapt
     */
    public boolean isPossibleToAdapt() {
        return possibleToAdapt;
    }

    /**
     * This sets the flag to say if on deciding to adapt if a possible solution 
     * was able to be found.
     * @param possibleToAdapt the possibleToAdapt to set
     */
    public void setPossibleToAdapt(boolean possibleToAdapt) {
        this.possibleToAdapt = possibleToAdapt;
    }    
    
    /**
     * This indicates if the action associated with the response has been 
     * performed.
     * @return the performed
     */
    public boolean isPerformed() {
        return performed;
    }

    /**
     * This sets the flag to indicate if the action associated with this 
     * response has been performed.
     * @param performed the performed to set
     */
    public void setPerformed(boolean performed) {
        this.performed = performed;
    }    
    
    /**
     * This returns the deployment id associated with the event that 
     * caused the response.
     * @return 
     */
    public String getApplicationId() {
        return cause.getApplicationId();
    }
    
    /**
     * This returns the deployment id associated with the event that 
     * caused the response.
     * @return 
     */
    public String getDeploymentId() {
        return cause.getDeploymentId();
    }
    
    /**
     * This returns the VM id associated with the response. This is the VM that is
     * to be adapted. i.e. change size, delete etc
     * @return The vm id of the vm to be adapted.
     */
    public String getVmId() {
        return vmId;
    }

    /**
     * This sets the VM id associated with the response. This is the VM that is
     * to be adapted. i.e. change size, delete etc
     * @param vmId The vm id of the vm to be adapted.
     */
    public void setVmId(String vmId) {
        this.vmId = vmId;
    }
    
}
