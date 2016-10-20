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
package eu.ascetic.paas.self.adaptation.manager.rules.decisionengine;

import eu.ascetic.paas.self.adaptation.manager.rules.datatypes.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This ranks the VMs to create destroy etc based upon power consumption. It may
 * add and remove multiple VMs at once.
 *
 * @author Richard Kavanagh
 */
public class MultiVMPowerRankedDecisionEngine extends AbstractDecisionEngine {

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
            response.setAdaptationDetails("Unable find a VM to delete.");
            response.setPossibleToAdapt(false);
            return response;
        }
        if (vmIds.isEmpty()) {
            response.setAdaptationDetails("Could not find a VM to delete");
            response.setPossibleToAdapt(false);
        }
        double targetDifference = response.getCause().getDeviationBetweenRawAndGuarantee(true);
        double valueRemoved = 0.0;
        String vmsToRemove = ""; //i.e. VMs_TO_REMOVE= ....
        ArrayList<PowerVmMapping> vmsList = getVMPowerList(response, vmIds);
        PowerVmMapping toRemove = vmsList.get(vmsList.size() - 1);
        while (valueRemoved < targetDifference) {
            if (toRemove == null) {
                break; //exit when no more vms to delete
            }
            if (response.getVmId() == null || response.getVmId().isEmpty()) {
                response.setVmId(toRemove.getVm().getId() + "");
                valueRemoved = valueRemoved + toRemove.getPower();
                vmsToRemove = toRemove.getVm().getId() + "";
            } else {
                /**
                 * Multiple VMs to delete, in order to meet criteria, thus
                 * delete many VMs and format as a scale to N VMs adaptation type
                 */
                response.setActionType(Response.AdaptationType.SCALE_TO_N_VMS);

                response.setAdaptationDetails("VM_TYPE=" + toRemove.getVm().getOvfId() + ";VMs_TO_REMOVE=" + vmsToRemove);
                vmsToRemove = vmsToRemove + (vmsToRemove.isEmpty() ? "" : ",") + toRemove.getVm().getId();
            }
            //get the next VM to delete
            if (!vmsList.isEmpty()) {
                toRemove = vmsList.get(vmsList.size() - 1);
            } else {
                Logger.getLogger(MultiVMPowerRankedDecisionEngine.class.getName()).log(Level.INFO, "Reached the limit of how many VMs can be removed");
                break; //exit when no more vms to delete
            }
        }
        if (response.getActionType().equals(Response.AdaptationType.SCALE_TO_N_VMS)) {
            response.setVmId("");
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
        Logger.getLogger(MultiVMPowerRankedDecisionEngine.class.getName()).log(Level.INFO, "VMs Types available to add: {0}", vmOvfTypes.size());
        if (vmOvfTypes.isEmpty()) {
            response.setAdaptationDetails("Could not find a VM OVF type to add");
            response.setPossibleToAdapt(false);
            return response;
        }
        String vmTypeToAdd;
        if (vmOvfTypes.size() == 1) {
            //If there is one option only select it
            vmTypeToAdd = vmOvfTypes.get(0);
        } else {
            Collections.shuffle(vmOvfTypes);
            //Give preference to any VM type specified in the rule.
            String vmTypePreference = response.getAdaptationDetail("VM_TYPE");
            //Check that the preferential type can be added
            if (vmTypePreference != null && vmOvfTypes.contains(vmTypePreference)) {
                vmTypeToAdd = vmTypePreference;
            } else { //If no preference is given then pick the best alternative
                vmTypeToAdd = pickLowestAveragePower(response, vmOvfTypes);
            }
        }
        response.setAdaptationDetails(vmTypeToAdd);
        List<String> typesToAdd = getVmTypesToConsolidateHost(response, vmTypeToAdd);
        if (typesToAdd.isEmpty()) {
            response.setAdaptationDetails("No types to add. No solution was found");
            response.setPossibleToAdapt(false);
            return response;
        }          
        while (!getCanVmBeAdded(response, vmTypeToAdd, typesToAdd.size())) {
            //Remove excess new VMs i.e. breach other SLA Rules
            typesToAdd.remove(0);
            if (typesToAdd.isEmpty()) {
                response.setAdaptationDetails("Adding a VM would breach SLA criteria");
                response.setPossibleToAdapt(false);
                return response;
            }              
        }
      
        if (typesToAdd.size() == 1) {
            //Send the update as a simple add VM message
            response.setAdaptationDetails(vmTypeToAdd);
        } else { //Multiple VMs to add
            response.setVmId("");
            response.setActionType(Response.AdaptationType.SCALE_TO_N_VMS);
            response.setAdaptationDetails("VM_TYPE=" + vmTypeToAdd + ";VM_COUNT=" + typesToAdd.size());
        }
        return response;
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
        response.setPossibleToAdapt(false);
        response.setAdaptationDetails("Scaling down is not supported");
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
        response.setPossibleToAdapt(false);
        response.setAdaptationDetails("Scaling up is not supported");
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
