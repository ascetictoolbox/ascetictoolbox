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
import eu.ascetic.paas.applicationmanager.model.VM;
import eu.ascetic.paas.self.adaptation.manager.ActuatorInvoker;
import eu.ascetic.paas.self.adaptation.manager.ovf.OVFUtils;
import eu.ascetic.paas.self.adaptation.manager.rules.datatypes.Response;
import eu.ascetic.utils.ovf.api.OvfDefinition;
import eu.ascetic.utils.ovf.api.ProductSection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        return getCanVmBeAdded(response, vmOvfType, 1);
    }

    /**
     * This tests to see if the power consumption limit will be breached or not
     * as well as the OVF boundaries.
     *
     * @param response The response type to check
     * @param vmOvfType The OVF type to add to
     * @param count theAmount of VMs to add
     * @return If the VM is permissible to add.
     */
    public boolean getCanVmBeAdded(Response response, String vmOvfType, int count) {
        if (actuator == null) {
            return false;
        }
        //average power of the VM type to add
        double averagePower = actuator.getAveragePowerUsage(response.getApplicationId(), response.getDeploymentId(), vmOvfType);
        Logger.getLogger(AbstractDecisionEngine.class.getName()).log(Level.INFO, "Avg power = {0}", averagePower);
        //The current total measured power consumption
        double totalMeasuredPower = actuator.getTotalPowerUsage(response.getApplicationId(), response.getDeploymentId());
        Logger.getLogger(AbstractDecisionEngine.class.getName()).log(Level.INFO, "Total power = {0}", totalMeasuredPower);
        averagePower = averagePower * count;
        List<String> vmOvfTypes = getActuator().getVmTypesAvailableToAdd(response.getApplicationId(),
                response.getDeploymentId());
        if (!vmOvfTypes.contains(vmOvfType)) {
            Logger.getLogger(AbstractDecisionEngine.class.getName()).log(Level.INFO, "VM type {0} isn't available to add", vmOvfType);
            for (String type : vmOvfTypes) {
                Logger.getLogger(AbstractDecisionEngine.class.getName()).log(Level.INFO, "VM type: {0} may be added.", type);
            }
            if (vmOvfTypes.isEmpty()) {
                Logger.getLogger(AbstractDecisionEngine.class.getName()).log(Level.INFO, "No VM types were available to add.");
            }
            return false;
        }
        if (averagePower == 0 || totalMeasuredPower == 0) {
            //Skip if the measured power values don't make any sense.
            Logger.getLogger(AbstractDecisionEngine.class.getName()).log(Level.WARNING, "Measured Power Fault: Average Power = {0} Total Power = {1}", new Object[]{averagePower, totalMeasuredPower});
            return enoughSpaceForVM(response, vmOvfType);
        }
        String applicationID = response.getApplicationId();
        String deploymentID = response.getDeploymentId();
        SLALimits limits = actuator.getSlaLimits(applicationID, deploymentID);
        if (limits != null && limits.getPower() != null) {
            Logger.getLogger(AbstractDecisionEngine.class.getName()).log(Level.INFO, "New power = {0}", totalMeasuredPower + averagePower);
            Logger.getLogger(AbstractDecisionEngine.class.getName()).log(Level.INFO, "Limit of power = {0}", limits.getPower());
            if (totalMeasuredPower + averagePower > Double.parseDouble(limits.getPower())) {
                return false;
            }
        }
        //TODO compare any further standard guarantees here that make sense
        //TODO cost??

        return enoughSpaceForVM(response, vmOvfType);
    }

    /**
     * This determines if the provider has enough space to create the new VM.
     *
     * @param response The response type to check
     * @param vmOvfType The OVF type to add to
     * @return If there is enough space for the new VM.
     */
    public boolean enoughSpaceForVM(Response response, String vmOvfType) {
        return enoughSpaceForVM(response, vmOvfType, 1);
    }

    /**
     * This determines if the provider has enough space to create the new VM.
     *
     * @param response The response type to check
     * @param vmOvfType The OVF type to add to
     * @param vmCount The count of how many VMs are to add
     * @return If there is enough space for the new VM.
     */
    public boolean enoughSpaceForVM(Response response, String vmOvfType, int vmCount) {
        VmRequirements requirements = OVFUtils.getVMRequirementsFromOvfType(response.getCause().getOvf(), vmOvfType);
        if (requirements == null) {
            return true;
        }
        List<Slot> slots = actuator.getSlots(requirements);
        if (slots.size() < vmCount) {
            Logger.getLogger(AbstractDecisionEngine.class.getName()).log(Level.INFO, "Reporting not enough space for VMs");
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
     * General Utility functions
     */
    /**
     * This gets the lowest power consuming VM type it may for example be used
     * to add another instance of this type.
     *
     * @param response The response type to get the vm type for.
     * @param vmOvfTypes The list of VM types of to search through (i.e.
     * available VM types to add).
     * @return The VM type with the lowest average power consumption
     */
    public String pickLowestAveragePower(Response response, List<String> vmOvfTypes) {
        response.setAdaptationDetails(vmOvfTypes.get(0));
        if (vmOvfTypes.isEmpty()) {
            return "";
        }
        String lowestAvgPowerType = vmOvfTypes.get(0);
        double lowestAvgPower = Double.MAX_VALUE;
        for (String currentVmType : vmOvfTypes) {
            double currentTypesAvgPower = getActuator().getAveragePowerUsage(response.getApplicationId(), response.getDeploymentId(),
                    currentVmType);
            if (currentTypesAvgPower == 0) {
                Logger.getLogger(AbstractDecisionEngine.class.getName()).log(Level.INFO,
                        "The calculation of the lowest average power of a VM type saw a zero value for the type: {0}", currentVmType);
            }
            if ((currentTypesAvgPower < lowestAvgPower) && currentTypesAvgPower > 0) {
                lowestAvgPowerType = currentVmType;
                lowestAvgPower = currentTypesAvgPower;
            }
        }
        return lowestAvgPowerType;
    }

    /**
     * This gets the highest powered VM to remove from the application
     * deployment.
     *
     * @param response The response object to perform the test for.
     * @param vmIds The VMids that are to be tested (i.e. ones that could be
     * removed for example).
     * @return The VmId to remove.
     */
    public Integer getHighestPoweredVM(Response response, List<Integer> vmIds) {
        Integer answer = null;
        double answerPower = 0;
        String vmType = response.getAdaptationDetail("VM_TYPE");
        if (vmIds.isEmpty()) {
            Logger.getLogger(AbstractDecisionEngine.class.getName()).log(Level.WARNING,
                    "No VMs were able to be deleted");
            return answer;
        }
        for (Integer vmId : vmIds) {
            double currentValue = getActuator().getPowerUsageVM(response.getApplicationId(), response.getDeploymentId(), "" + vmId);
            if (currentValue == 0) {
                Logger.getLogger(AbstractDecisionEngine.class.getName()).log(Level.WARNING,
                        "The calculation of the highest powered VM saw a zero value for VM: {0}", vmId);
            }
            VM vm = getActuator().getVM(response.getApplicationId(), response.getDeploymentId(), vmId + "");
            if (currentValue > answerPower && (vmType == null || vm.getOvfId().equals(vmType))) {
                answer = vmId;
                answerPower = currentValue;
            }
        }
        if (answer == null) {
            Logger.getLogger(AbstractDecisionEngine.class.getName()).log(Level.WARNING,
                    "No VM had the highest power thus defaulting to the first in the list");            
            vmIds.get(0);
        }
        return answer;
    }

    /**
     * This gets the list of all vm objects from a given vmId and provides the
     * power consumption, ready for ranking
     *
     * @param response The response object to perform the test for.
     * @param vmIds The VMids that are to be tested (i.e. ones that could be
     * removed for example).
     * @return The list of VMs and there power consumption
     */
    public ArrayList<PowerVmMapping> getVMPowerList(Response response, List<Integer> vmIds) {
        ArrayList<PowerVmMapping> answer = new ArrayList<>();
        for (Integer vmId : vmIds) {
            double power = getActuator().getPowerUsageVM(response.getApplicationId(), response.getDeploymentId(), "" + vmId);
            VM vm = getActuator().getVM(response.getApplicationId(), response.getDeploymentId(), vmId + "");
            answer.add(new PowerVmMapping(power, vm));
        }
        Collections.sort(answer);
        return answer;
    }

    /**
     * This maps a power measurement to a VM.
     */
    public class PowerVmMapping implements Comparable<PowerVmMapping> {

        private final Double power;
        private final VM vm;

        public PowerVmMapping(double power, VM vm) {
            this.power = power;
            this.vm = vm;
        }

        public Double getPower() {
            return power;
        }

        public VM getVm() {
            return vm;
        }

        @Override
        public int compareTo(PowerVmMapping o) {
            return this.power.compareTo(o.power);
        }

    }

    /**
     * This looks at the host with the most power consumption and looks how to
     * fill it with the VmTypes that are available in the OVF
     *
     * @param response The response object to perform the analysis for
     * @param vmOvfType The Vm type that is to be added
     * @return The vm types to add to a given host, with repeats as needed to
     * instantiate multiple VMs.
     */
    protected List<String> getVmTypesToConsolidateHost(Response response, String vmOvfType) {
        List<String> answer = new ArrayList<>();
        VmRequirements requirements = OVFUtils.getVMRequirementsFromOvfType(response.getCause().getOvf(), vmOvfType);
        if (requirements == null) {
            return answer;
        }
        List<Slot> slots = actuator.getSlots(requirements);
        if (slots.isEmpty()) {
            Logger.getLogger(AbstractDecisionEngine.class.getName()).log(Level.SEVERE, "Error finding free slots available");
            return answer;
        }
        int count = getHostSlotCountWithFewestSlots(slots);
        int maxVms = getCountOfVMsPossibleToAdd(response, vmOvfType);
        Logger.getLogger(AbstractDecisionEngine.class.getName()).log(Level.INFO, "Count = {0} MaxVMs = {1}", new Object[]{count, maxVms});
        for (int i = 0; i < count; i++) {
            if (i >= maxVms) {
                return answer;
            }
            answer.add(vmOvfType);
        }
        return answer;
    }

    /**
     * This indicates how many VMs can be added of a given type
     *
     * @param response The response object to do the calculation for
     * @param vmOvfType The ovf type to do the calculation for
     * @return The count of VMs that can be added of a given type
     */
    protected int getCountOfVMsPossibleToAdd(Response response, String vmOvfType) {
        OvfDefinition ovf = response.getCause().getOvf();
        ProductSection details = OVFUtils.getProductionSectionFromOvfType(ovf,
                response.getApplicationId());
        int currentCount = getActuator().getVmCountOfGivenType(response.getApplicationId(),
                response.getDeploymentId(),
                vmOvfType);
        try {
            int upperBound = details.getUpperBound();
            int answer = upperBound - currentCount;
            return (answer < 0 ? 0 : answer); //The answer must be positive
        } catch (NullPointerException ex) {
            /**
             * This is thrown in the event the upper bound is missing. Thus it
             * is always + 1 of the current value.
             */
            return currentCount + 1;
        }
    }

    /**
     * This gets the count of VMs slots from the host with the fewest slots
     * available.
     *
     * @param slots The slots that are currently available to use.
     * @return The host with the fewest slots available
     */
    private Integer getHostSlotCountWithFewestSlots(List<Slot> slots) {
        Integer score = Integer.MAX_VALUE;
        HashMap<String, Integer> hostSlots = slotCounter(slots);
        for (Map.Entry<String, Integer> hostSlot : hostSlots.entrySet()) {
            //Ensure this is a none full host with the lowest amount of free slots
            if (hostSlot.getValue() < score && hostSlot.getValue() != 0) {
                score = hostSlot.getValue();
            }
        }
        return score;
    }

    /**
     * This counts the amount of slots that are available on each physical host
     *
     * @param slots The slots available
     * @return The mapping between hosts and slots available.
     */
    private HashMap<String, Integer> slotCounter(List<Slot> slots) {
        HashMap<String, Integer> answer = new HashMap<>();
        for (Slot slot : slots) {
            String hostname = slot.getHostname();
            if (answer.containsKey(slot.getHostname())) {
                answer.put(hostname, answer.get(hostname) + 1);
            } else {
                answer.put(hostname, 1);
            }
        }
        return answer;
    }

}
