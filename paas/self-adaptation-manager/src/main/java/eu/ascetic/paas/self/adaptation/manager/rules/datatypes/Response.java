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
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a valid response that the self-adaptation manager can
 * use in order to respond to the incoming events.
 *
 * @author Richard Kavanagh
 */
public class Response implements Comparable<Response> {

    private final ActuatorInvoker actuator;
    private final EventData cause;
    private AdaptationType actionType;
    private String adaptationDetails;
    private String vmId;
    private boolean performed = false;
    private boolean possibleToAdapt = true;
    private static final Map<String, Response.AdaptationType> ADAPTATION_TYPE_MAPPING
            = new HashMap<>();

    static {
        ADAPTATION_TYPE_MAPPING.put("ADD_VM", Response.AdaptationType.ADD_VM);
        ADAPTATION_TYPE_MAPPING.put("INFLATE_VM", Response.AdaptationType.INFLATE_VM);
        ADAPTATION_TYPE_MAPPING.put("ADD_CPU", Response.AdaptationType.ADD_CPU);
        ADAPTATION_TYPE_MAPPING.put("ADD_MEMORY", Response.AdaptationType.ADD_MEMORY);
        ADAPTATION_TYPE_MAPPING.put("REMOVE_VM", Response.AdaptationType.REMOVE_VM);
        ADAPTATION_TYPE_MAPPING.put("DEFLATE_VM", Response.AdaptationType.DEFLATE_VM);
        ADAPTATION_TYPE_MAPPING.put("REMOVE_CPU", Response.AdaptationType.REMOVE_CPU);
        ADAPTATION_TYPE_MAPPING.put("REMOVE_MEMORY", Response.AdaptationType.ADD_VM);
        ADAPTATION_TYPE_MAPPING.put("SHUTDOWN_APP", Response.AdaptationType.SHUTDOWN_APP);
        ADAPTATION_TYPE_MAPPING.put("HARD_SHUTDOWN_APP", Response.AdaptationType.HARD_SHUTDOWN_APP);
        ADAPTATION_TYPE_MAPPING.put("REQUEST_VM_CONSOLIDATION", Response.AdaptationType.REQUEST_VM_CONSOLIDATION);
        ADAPTATION_TYPE_MAPPING.put("SCALE_TO_N_VMS", Response.AdaptationType.SCALE_TO_N_VMS);
        ADAPTATION_TYPE_MAPPING.put("RENEGOTIATE", Response.AdaptationType.RENEGOTIATE);
        ADAPTATION_TYPE_MAPPING.put(null, null);
        ADAPTATION_TYPE_MAPPING.put("", null);
    }

    /**
     * Adaptation types are the forms of adaptation that can be made to occur.
     */
    public enum AdaptationType {

        ADD_VM, REMOVE_VM, INFLATE_VM, DEFLATE_VM,
        ADD_CPU, REMOVE_CPU, ADD_MEMORY, REMOVE_MEMORY, SHUTDOWN_APP,
        HARD_SHUTDOWN_APP, REQUEST_VM_CONSOLIDATION, SCALE_TO_N_VMS, RENEGOTIATE
    }

    private enum AdaptationDetailKeys {

        VM_TYPE, VM_COUNT
    }

    /**
     * This creates a standard response object. It indicates which actuator to
     * use and which message to send to it.
     *
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
     * This provides the mapping between the string representation of a response
     * type and the adaptation type.
     *
     * @param responseType The name of the rule.
     * @return The Adaptation type required.
     */
    public static Response.AdaptationType getAdaptationType(String responseType) {
        Response.AdaptationType answer = ADAPTATION_TYPE_MAPPING.get(responseType);
        if (answer == null) {
            if (responseType.contains("SCALE_TO_")) {
                return AdaptationType.SCALE_TO_N_VMS;
            }
        }
        return answer;
    }

    /**
     * This gets the time of arrival of the event that caused the response.
     *
     * @return
     */
    public long getTime() {
        return cause.getTime();
    }

    /**
     * This returns a copy of the event that was the original cause of the
     * response to be created.
     *
     * @return The event that caused the response (or at least the last in a
     * sequence of events).
     */
    public EventData getCause() {
        return cause;
    }

    @Override
    public int compareTo(Response response) {
        //This sequences responses in cronological order.
        return Long.compare(this.getTime(), response.getTime());
    }

    /**
     * This returns the type of adaptation that the response specifies to give
     * to the event.
     *
     * @return the type of action to perform to respond to the event
     */
    public AdaptationType getActionType() {
        return actionType;
    }

    /**
     * This sets the type of adaptation that the response specifies to give to
     * the event.
     *
     * @param actionType the actionType to set
     */
    public void setActionType(AdaptationType actionType) {
        this.actionType = actionType;
    }

    /**
     * This returns additional information about the adaptation.
     *
     * @return the adaptationDetails
     */
    public String getAdaptationDetails() {
        return adaptationDetails;
    }

    /**
     * Given the key value of the adaption detail this returns its value.
     *
     * @param key The key name for the actuation parameter
     * @return The value of the adaptation detail else null.
     */
    public String getAdaptationDetail(String key) {
        String[] args = adaptationDetails.split(";");
        for (String arg : args) {
            if (arg.split("=")[0].equals(key)) {
                return arg.split("=")[1].trim();
            }
        }
        return null;
    }

    /**
     * This sets additional information about the adaptation, that might be
     * needed.
     *
     * @param adaptationDetails the adaptationDetails to set
     */
    public void setAdaptationDetails(String adaptationDetails) {
        this.adaptationDetails = adaptationDetails;
    }

    /**
     * This indicates if on deciding to adapt if a possible solution was found.
     *
     * @return the possibleToAdapt
     */
    public boolean isPossibleToAdapt() {
        return possibleToAdapt;
    }

    /**
     * This sets the flag to say if on deciding to adapt if a possible solution
     * was able to be found.
     *
     * @param possibleToAdapt the possibleToAdapt to set
     */
    public void setPossibleToAdapt(boolean possibleToAdapt) {
        this.possibleToAdapt = possibleToAdapt;
    }

    /**
     * This indicates if the action associated with the response has been
     * performed.
     *
     * @return the performed
     */
    public boolean isPerformed() {
        return performed;
    }

    /**
     * This sets the flag to indicate if the action associated with this
     * response has been performed.
     *
     * @param performed the performed to set
     */
    public void setPerformed(boolean performed) {
        this.performed = performed;
    }

    /**
     * This indicates if the action associated with the response has been
     * completed. i.e. its either been performed or is un-actionable.
     *
     * @return the performed
     */
    public boolean isComplete() {
        return performed || !possibleToAdapt;
    }

    /**
     * This returns the deployment id associated with the event that caused the
     * response.
     *
     * @return
     */
    public String getApplicationId() {
        return cause.getApplicationId();
    }

    /**
     * This returns the deployment id associated with the event that caused the
     * response.
     *
     * @return
     */
    public String getDeploymentId() {
        return cause.getDeploymentId();
    }

    /**
     * This returns the VM id associated with the response. This is the VM that
     * is to be adapted. i.e. change size, delete etc
     *
     * @return The vm id of the vm to be adapted.
     */
    public String getVmId() {
        return vmId;
    }

    /**
     * This sets the VM id associated with the response. This is the VM that is
     * to be adapted. i.e. change size, delete etc
     *
     * @param vmId The vm id of the vm to be adapted.
     */
    public void setVmId(String vmId) {
        this.vmId = vmId;
    }

}
