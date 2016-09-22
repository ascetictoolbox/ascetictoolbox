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

import es.bsc.vmmclient.models.Slot;
import es.bsc.vmmclient.models.VmRequirements;
import eu.ascetic.paas.applicationmanager.model.SLALimits;
import eu.ascetic.paas.self.adaptation.manager.ActuatorInvoker;
import eu.ascetic.paas.self.adaptation.manager.ovf.OVFUtils;
import eu.ascetic.paas.self.adaptation.manager.rules.datatypes.Response;
import eu.ascetic.utils.ovf.api.OvfDefinition;
import eu.ascetic.utils.ovf.api.ProductSection;
import java.util.List;

/**
 * The aim of this class is to decide given an event that has been assessed what
 * the magnitude of an adaptation should be used will be. It may also have to
 * decide to which VM this adaptation should occur.
 *
 * @author Richard Kavanagh
 */
public abstract class AbstractDecisionEngine implements DecisionEngine {

    /**
     * The actuator is to be used as an information source, with a set of
     * standard questions that can be asked about how adaptation may occur. This
     * avoids the decision engine having to have interface specific code i.e.
     * Rest or ActiveMQ, it also prevents having to maintain multiple
     * connections for different purposes.
     */
    private ActuatorInvoker actuator;

    public AbstractDecisionEngine() {
    }

    @Override
    public void setActuator(ActuatorInvoker actuator) {
        this.actuator = actuator;
    }

    @Override
    public ActuatorInvoker getActuator() {
        return actuator;
    }

    /**
     * This tests to see if the power consumption limit will be breached or not
     * as well as the OVF boundaries.
     *
     * @param response The response type to check
     * @param vmOvfType The OVF type to add to
     * @return If the VM is permissible to add.
     */
    public boolean getCanVmBeAdded(Response response, String vmOvfType) {
        if (actuator == null) {
            return false;
        }
        double averagePower = actuator.getAveragePowerUsage(response.getApplicationId(), response.getDeploymentId(), vmOvfType);
        double totalMeasuredPower = actuator.getTotalPowerUsage(response.getApplicationId(), response.getDeploymentId());
        List<String> vmOvfTypes = getActuator().getVmTypesAvailableToAdd(response.getApplicationId(),
                response.getDeploymentId());
        if (!vmOvfTypes.contains(vmOvfType)) {
            return false;
        }
        String applicationID = response.getApplicationId();
        String deploymentID = response.getDeploymentId();
        SLALimits limits = actuator.getSlaLimits(applicationID, deploymentID);
        if (limits != null && limits.getPower() != null) {
            if (totalMeasuredPower + averagePower > Double.parseDouble(limits.getPower())) {
                return false;
            }
        }
        //TODO compare any further standard gurantees here that make sense
        //TODO cost??

        return enoughSpaceForVM(response, vmOvfType);
    }

    /**
     * This determines if the provider has enough space to create the new VM.
     * @param response The response type to check
     * @param vmOvfType The OVF type to add to
     * @return If there is enough space for the new  VM.
     */
    public boolean enoughSpaceForVM(Response response, String vmOvfType) {
        VmRequirements requirements = OVFUtils.getVMRequirementsFromOvfType(response.getCause().getOvf(), vmOvfType);
        if (requirements == null) {
            return true;
        }
        List<Slot> slots = actuator.getSlots(requirements);
        if (slots.isEmpty()) {
            return false;
        }
        return true;
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
    protected abstract String getVmsToRemove(List<Integer> vmsPossibleToRemove, int count);

    /**
     * 
     * @param totalCpus
     * @param totalMem
     * @param vmTypesAvailableToAdd 
     */
    protected void getConsolidationSlots(int totalCpus, int totalMem, List<String> vmTypesAvailableToAdd) {
    
    }
    
}
