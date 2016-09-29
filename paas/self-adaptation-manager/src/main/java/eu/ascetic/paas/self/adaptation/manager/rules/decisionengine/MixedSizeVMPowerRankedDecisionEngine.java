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

import es.bsc.vmmclient.models.Node;
import es.bsc.vmmclient.models.Slot;
import es.bsc.vmmclient.models.VmRequirements;
import eu.ascetic.paas.applicationmanager.model.VM;
import eu.ascetic.paas.self.adaptation.manager.ovf.OVFUtils;
import eu.ascetic.paas.self.adaptation.manager.rules.datatypes.Response;
import eu.ascetic.paas.self.adaptation.manager.rules.datatypes.SlotSolution;
import eu.ascetic.paas.self.adaptation.manager.utils.SlotAwareDeployer;
import eu.ascetic.utils.ovf.api.OvfDefinition;
import eu.ascetic.utils.ovf.api.VirtualSystem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * This ranks the VMs to create destroy etc based upon power consumption. It may
 * add and remove multiple VMs at once.
 *
 * @author Richard Kavanagh
 */
public class MixedSizeVMPowerRankedDecisionEngine extends AbstractDecisionEngine {

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
        TreeMap<Double, VM> vmsList = sortVMPowerList(getVMPowerList(response, vmIds));
        Map.Entry<Double, VM> toRemove = vmsList.lastEntry();
        while (valueRemoved < targetDifference) {
            if (toRemove == null) {
                break; //exit when no more vms to delete
            }
            if (response.getVmId() == null || response.getVmId().isEmpty()) {
                response.setVmId(toRemove.getValue().getId() + "");
                valueRemoved = valueRemoved + toRemove.getKey();
                vmsToRemove = toRemove.getValue().getId() + "";
            } else {
                /**
                 * Multiple VMs to delete, in order to meet criteria, thus
                 * delete many VMs and format as a scale to N VMs adaptation
                 * type
                 */
                response.setActionType(Response.AdaptationType.SCALE_TO_N_VMS);

                response.setAdaptationDetails("VM_TYPE=" + toRemove.getValue().getOvfId() + ";VMs_TO_REMOVE=" + vmsToRemove);
                vmsToRemove = vmsToRemove + (vmsToRemove.isEmpty() ? "" : ",") + toRemove.getValue().getId();
            }
            //get the next VM to delete
            vmsList.lowerEntry(valueRemoved);
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
        SlotAwareDeployer deployer = new SlotAwareDeployer();
        if (getActuator() == null) {
            response.setAdaptationDetails("Unable to find actuator.");
            response.setPossibleToAdapt(false);
            return response;
        }
        List<String> vmOvfTypes = getActuator().getVmTypesAvailableToAdd(response.getApplicationId(), response.getDeploymentId());
        if (vmOvfTypes.isEmpty()) {
            response.setAdaptationDetails("Could not find a VM OVF type to add");
            response.setPossibleToAdapt(false);
            return response;
        }
        String vmTypePreference = response.getAdaptationDetail("VM_TYPE");
        String vmTypeToAdd;
        //Check that the preferential type can be added
        if (vmTypePreference != null && vmOvfTypes.contains(vmTypePreference)) {
            vmTypeToAdd = vmTypePreference;
        } else { //If no preference is given then pick the best alternative
            vmTypeToAdd = pickLowestAveragePower(response, vmOvfTypes);
        }
        //Construct Host Information
        List<Slot> slots = getActuator().getSlots();
        if (slots.isEmpty()) {
            response.setAdaptationDetails("Adding a VM would breach SLA criteria");
            response.setPossibleToAdapt(false);
            return response;
        }
        Map<String, Node> hosts = new HashMap<>();
        List<Node> nodes = getActuator().getProviderHostInfo();
        for (Node node : nodes) {
            hosts.put(node.getHostname(), node);    
        }
        //Construct VM information
        String ovfStr = getActuator().getOvf(response.getApplicationId(), response.getDeploymentId());
        OvfDefinition ovf = OVFUtils.getOvfDefinition(ovfStr);
        VirtualSystem system = OVFUtils.getVMFromOvfType(ovf, vmTypeToAdd);
        VmRequirements reqs = OVFUtils.getVMRequirementsFromOvfType(ovf, vmTypeToAdd);
        List<String> typesToAdd = new ArrayList<>();
        List<String> typeSizesToAdd = new ArrayList<>();
        
        int minCpus = system.getVirtualHardwareSection().getMinNumberOfVirtualCPUs();
        int maxCpus = system.getVirtualHardwareSection().getMaxNumberOfVirtualCPUs();
        /**
         * TODO consider a better way of deciding the target number of CPUs to add.
         * The goal is rather arbritrary but needs to fill up a given host, thus
         * making things as efficient cent as possible.
         */
        List<SlotSolution> solutions = deployer.getSlotsSortedByConsolidationScore(slots, 
                hosts, 
                reqs.getCpus() * 4, // Add upto 4 x the curent cpu default. 
                minCpus, 
                maxCpus, 
                reqs.getRamMb(), 
                reqs.getDiskGb());
        if (solutions.isEmpty()) {
            response.setAdaptationDetails("No Slot solution was found");
            response.setPossibleToAdapt(false);
            return response;
        }
        List<Slot> winningSolution = solutions.get(0).getSlots();
        for (Slot vmToPlace : winningSolution) {
            typesToAdd.add(vmTypePreference);
            typeSizesToAdd.add(vmToPlace.getFreeCpus() + "");
        }
        while (!typesToAdd.isEmpty() && !getCanVmBeAdded(response, vmTypePreference, typesToAdd.size())) {
            //Remove excess new VMs i.e. breach other SLA Rules
            typesToAdd.remove(0);
            typeSizesToAdd.remove(0);
        }
        if (typesToAdd.isEmpty()) {
            response.setAdaptationDetails("Adding a VM would breach SLA criteria");
            response.setPossibleToAdapt(false);
            return response;
        }
        String typesToAddSize = "";
        int count = 0;
        for (String size : typeSizesToAdd) {
            typesToAddSize = typesToAddSize + (count == 0 ? "" : ",") + size;
            count = count + 1;
        }
        typesToAddSize = ";VM_SIZE=" + typesToAddSize;
        if (typesToAdd.size() == 1) {
            //Send the update as a simple add VM message
            response.setAdaptationDetails(vmTypeToAdd);
        } else { //Multiple VMs to add
            response.setVmId("");
            response.setActionType(Response.AdaptationType.SCALE_TO_N_VMS);
            response.setAdaptationDetails("VM_TYPE=" + vmTypeToAdd + ";VM_COUNT=" + typesToAdd.size() + typesToAddSize);
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
