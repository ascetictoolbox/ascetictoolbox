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

import eu.ascetic.paas.self.adaptation.manager.ovf.OVFUtils;
import eu.ascetic.paas.self.adaptation.manager.rules.datatypes.Response;
import eu.ascetic.utils.ovf.api.OvfDefinition;
import eu.ascetic.utils.ovf.api.ProductSection;
import java.util.Collections;
import java.util.List;

/**
 * The aim of this class is to decide given an event that has been assessed what
 * the magnitude of an adaptation should be used will be. It may also have to
 * decide to which VM this adaptation should occur.
 *
 * The last vm created decision engine will pick VMs and VM types to adapt
 * randomly and when deleting VMs it will pick the VM which was created last. It
 * uses no outside data source to guide this decision.
 *
 * @author Richard Kavanagh
 */
public class LastVMCreatedDecisionEngine extends AbstractDecisionEngine {

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
     * The decision logic for deleting a VM. It removes the last VM to be
     * created (i.e. highest VM ID first).
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
        if (vmIds == null) {
            System.out.println("Internal Error list of deleteable VM Ids equals null.");
            response.setPossibleToAdapt(false);
            return response;
        }
        if (!vmIds.isEmpty()) {
            Collections.sort(vmIds);
            Collections.reverse(vmIds);
            //Remove the last VM to be created from the list of possible VMs
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
        return response;
    }

    /**
     * The decision logic for horizontal scaling to a given target value.
     *
     * @param response The response to finalise details for.
     * @return The finalised response object
     */
    public Response scaleToNVms(Response response) {
        if (getActuator() == null) {
            response.setAdaptationDetails("Unable to find actuator.");
            response.setPossibleToAdapt(false);
            return response;
        }
        String appId = response.getApplicationId();
        String deploymentId = response.getDeploymentId();
        String vmType = response.getAdaptationDetail("VM_TYPE");
        int currentVmCount = getActuator().getVmCountOfGivenType(appId, deploymentId, vmType);
        int targetCount = Integer.parseInt(response.getAdaptationDetail("VM_COUNT"));
        int difference = targetCount - currentVmCount;
        OvfDefinition ovf = response.getCause().getOvf();
        ProductSection details = OVFUtils.getProductionSectionFromOvfType(ovf, appId);
        if (difference == 0) {
            response.setPerformed(true);
            response.setPossibleToAdapt(false);
            return response;
        }
        if (ovf != null && details != null) {
            if (targetCount < details.getLowerBound() || targetCount > details.getUpperBound()) {
                response.setPerformed(true);
                response.setPossibleToAdapt(false);
                response.setAdaptationDetails("Unable to adapt, the target was out of acceptable bounds");
                return response;
            }
        }
        if (difference > 0) { //add VMs
            response.setAdaptationDetails("VM_TYPE=" + vmType + ";VM_COUNT=" + difference);
        } else { //less that zero so remove VMs
            List<Integer> vmsPossibleToRemove = getActuator().getVmIdsAvailableToRemove(appId, deploymentId);
            //Note: the 0 - difference is intended to make the number positive
            response.setAdaptationDetails("VM_TYPE=" + vmType + ";VMs_TO_REMOVE=" + getVmsToRemove(vmsPossibleToRemove, 0 - difference));
        }
        return response;
    }

    /**
     * This generates the list of VMs to remove
     *
     * @param vmsPossibleToRemove The list of VMs that could be removed
     * @param count The amount of VMs needing to go
     * @return The string for the command to remove the VMs
     */
    private String getVmsToRemove(List<Integer> vmsPossibleToRemove, int count) {
        String answer = "";
        Collections.sort(vmsPossibleToRemove);
        Collections.reverse(vmsPossibleToRemove);
        //Remove the last VM to be created from the list of possible VMs
        for (int i = 0; i < count; i++) {
            Integer vmid = vmsPossibleToRemove.get(i);
            answer = answer + (i == 0 ? "" : ",") + vmid;
        }
        return answer;
    }

}
