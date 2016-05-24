/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.bsc.vmmclient.models;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author raimon
 */
public final class SelfAdaptationAction {
    
    private VmPlacement[] deploymentPlan = null;
    private Map<String, Map<String, String>> slamRequirements = null;
    private String slamMessage = null;
    private String exception = null;
    private Boolean success = false;
    private Boolean assignVmsToHosts = true; //in general, we pre-assign all deployed VMS to its host
    private Map<String, Boolean> vmIdsToReassign = null; //you can mark several VMs for reassignation by not pre-assigning them
    
    public SelfAdaptationAction() {
        this.assignVmsToHosts = true;
    }
    
    public SelfAdaptationAction(Boolean assignVmsToHosts) {
        this.assignVmsToHosts = assignVmsToHosts;
    }
    
    public SelfAdaptationAction(String slamMessage) {
        this.assignVmsToHosts = true;
        this.slamMessage = slamMessage;
        //readRequirementsFromViolationMessages(slamMessage);
    }
    
    public SelfAdaptationAction(String slamMessage, Boolean assignVmsToHosts) {
        this.assignVmsToHosts = assignVmsToHosts;
        this.slamMessage = slamMessage;
        //readRequirementsFromViolationMessages(slamMessage);
    }

    /**
     * @return the recommendedPlan
     */
    public VmPlacement[] getDeploymentPlan() {
        return deploymentPlan;
    }

    /**
     * @return the slamRequirements
     */
    public Map<String, Map<String, String>> getSlamRequirements() {
        return slamRequirements;
    }

    /**
     * @return the exception
     */
    public String getException() {
        return exception;
    }

    /**
     * @return the success
     */
    public boolean isSuccess() {
        return getSuccess();
    }

    /**
     * @param deploymentPlan the deploymentPlan to set
     */
    public void setDeploymentPlan(VmPlacement[] deploymentPlan) {
        this.deploymentPlan = deploymentPlan;
        this.success = true;
    }

    /**
     * @param slamRequirements the slamRequirements to set
     */
    public void setSlamRequirements(Map<String, Map<String, String>> slamRequirements) {
        this.slamRequirements = slamRequirements;
    }

    /**
     * @return the slaMessage
     */
    public String getSlamMessage() {
        return slamMessage;
    }

    /**
     * @param slaMessage the slaMessage to set
     */
    public void setSlamMessage(String slaMessage) {
        this.slamMessage = slaMessage;
    }

    /**
     * @param exception the exception to set
     */
    public void setException(Exception exception) {
        this.success = false;
        this.exception = exception.getLocalizedMessage();
    }

    /**
     * @param success the success to set
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    /**
     * Checks if self-adaptation action has new requirements to apply.
     * @return a boolean
     */
    public boolean hasSlamRequirements() {
        return slamRequirements != null && slamRequirements.size() > 0;
    }

    /**
     * @return the success
     */
    public Boolean getSuccess() {
        return success;
    }

    /**
     * @return the assignVmToHosts
     */
    public Boolean assignVmsToHosts() {
        return assignVmsToHosts;
    }

    /**
     * @param assignVmsToHosts the assignVmToHosts to set
     */
    public void setAssignVmsToHosts(Boolean assignVmsToHosts) {
        this.assignVmsToHosts = assignVmsToHosts;
    }
    
    /**
     * Adds a new vmId to the list of VMs to self-adapt.
     * @param vmId 
     */
    public void addVmIdToReassign(String vmId) {
        if(vmIdsToReassign == null){ vmIdsToReassign = new HashMap<>(); }
        vmIdsToReassign.put(vmId, true);
    }
    
    /**
     * Checks if a specific VM should be self adapted or not.
     * 
     * @param vmId
     * @return 
     */
    public boolean shouldVmBeReassigned(String vmId) {
        return vmIdsToReassign != null && vmIdsToReassign.containsKey(vmId);
    }
}