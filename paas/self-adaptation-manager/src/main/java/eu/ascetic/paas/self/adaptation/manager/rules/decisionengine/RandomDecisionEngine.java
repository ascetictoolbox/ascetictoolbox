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
package eu.ascetic.paas.self.adaptation.manager.rules.decisionengine;

import eu.ascetic.paas.self.adaptation.manager.rules.datatypes.Response;
import java.util.Collections;
import java.util.List;

/**
 * The aim of this class is to decide given an event that has been assessed what
 * the magnitude of an adaptation should be used will be. It may also have to
 * decide to which VM this adaptation should occur.
 *
 * The random decision engine will pick VMs and VM types to adapt randomly
 * without any further guidance from outside data sources.
 *
 * @author Richard Kavanagh
 */
public class RandomDecisionEngine extends AbstractDecisionEngine {

    @Override
    public Response decide(Response response) {
        if (response.getActionType().equals(Response.AdaptationType.ADD_VM)) {
            response = addVM(response);
        } else if (response.getActionType().equals(Response.AdaptationType.REMOVE_VM)) {
            response = deleteVM(response);
        } else if (response.getActionType().equals(Response.AdaptationType.INFLATE_VM)) {
            response = scaleUp(response);
        } else if (response.getActionType().equals(Response.AdaptationType.DEFLATE_VM)) {
            response = scaleDown(response);
        } else if (response.getActionType().equals(Response.AdaptationType.SCALE_TO_N_VMS)) {
            response = scaleToNVms(response);
        }
        return response;
    }

    /**
     * The decision logic for adding a VM.
     *
     * @param response The response to finalise details for.
     * @return The finalised response object
     */
    public Response deleteVM(Response response) {
        if (getActuator() == null) {
            response.setAdaptationDetails("Unable to find actuator.");
            response.setPossibleToAdapt(false);
            return response;
        }
        List<Integer> vmIds = getActuator().getVmIdsAvailableToRemove(response.getApplicationId(), response.getDeploymentId());
        if (!vmIds.isEmpty()) {
            Collections.shuffle(vmIds);
            response.setVmId(vmIds.get(0) + "");
            return response;
        } else {
            response.setAdaptationDetails("Could not find a VM to delete");
            response.setPossibleToAdapt(false);
        }
        return response;
    }

    /**
     * The decision logic for adding a VM.
     *
     * @param response The response to finalise details for.
     * @return The finalised response object
     */
    public Response addVM(Response response) {
        if (getActuator() == null) {
            response.setAdaptationDetails("Unable to find actuator.");
            response.setPossibleToAdapt(false);
            return response;
        }
        List<String> vmOvfTypes = getActuator().getVmTypesAvailableToAdd(response.getApplicationId(), response.getDeploymentId());
        if (!vmOvfTypes.isEmpty()) {
            Collections.shuffle(vmOvfTypes);
            response.setAdaptationDetails(vmOvfTypes.get(0));
            if (getCanVmBeAdded(response, vmOvfTypes.get(0))) {
                return response;
            } else {
                response.setAdaptationDetails("Adding a VM would breach SLA criteria");
                response.setPossibleToAdapt(false);
                return response;
            }
        } else {
            response.setAdaptationDetails("Could not find a VM OVF type to add");
            response.setPossibleToAdapt(false);
            return response;
        }
    }

    /**
     * The decision logic for scaling down.
     *
     * @param response The response to finalise details for.
     * @return The finalised response object
     */
    public Response scaleDown(Response response) {
        if (getActuator() == null) {
            response.setAdaptationDetails("Unable to find actuator.");
            response.setPossibleToAdapt(false);
            return response;
        }
        //TODO complete logic here
        //Decide on which VM to scale down in size.
        return response;
    }

    /**
     * The decision logic for scaling up.
     *
     * @param response The response to finalise details for.
     * @return The finalised response object
     */
    public Response scaleUp(Response response) {
        if (getActuator() == null) {
            response.setAdaptationDetails("Unable to find actuator.");
            response.setPossibleToAdapt(false);
            return response;
        }
        //TODO complete logic here
        //Decide on which VM to scale up in size.
        return response;
    }

    /**
     * This generates the list of VMs to remove
     *
     * @param vmsPossibleToRemove The list of VMs that could be removed
     * @param count The amount of VMs needing to go
     * @return The string for the command to remove the VMs
     */
    @Override
    protected String getVmsToRemove(List<Integer> vmsPossibleToRemove, int count) {
        String answer = "";
        Collections.shuffle(vmsPossibleToRemove);
        for (int i = 0; i < count; i++) {
            Integer vmid = vmsPossibleToRemove.get(0);
            answer = answer + (i == 0 ? "" : ",") + vmid;
            vmsPossibleToRemove.remove(i);
        }
        return answer;
    }

}
