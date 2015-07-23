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
    private AdaptationType actionType; //Make Enumeration
    private String adapationDetails;
    
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
     * This returns the deployment id associated with the event that 
     * caused the response.
     * @return 
     */
    public String getApplicationId() {
        return ""; //TODO
    }
    
    /**
     * This returns the deployment id associated with the event that 
     * caused the response.
     * @return 
     */
    public String getDeploymentId() {
        return ""; //TODO
    }
    
    /**
     * This returns the VM id associated with the event. This is the VM that is
     * to be adapted. i.e. change size, delete etc
     * @return 
     */
    public String getVMId() {
        return ""; //TODO
    }
    
}
