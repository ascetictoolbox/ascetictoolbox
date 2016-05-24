/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.bsc.demiurge.core.models.scheduling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 *
 * @author raimon
 */
public final class SelfAdaptationAction {
    static Logger log = LogManager.getLogger(SelfAdaptationAction.class);
    
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
        readRequirementsFromViolationMessages(slamMessage);
    }
    
    public SelfAdaptationAction(String slamMessage, Boolean assignVmsToHosts) {
        this.assignVmsToHosts = assignVmsToHosts;
        this.slamMessage = slamMessage;
        readRequirementsFromViolationMessages(slamMessage);
    }
     
    private String getNextXmlTag(String xml, String startTag, String endTag) {
        int start = xml.indexOf(startTag);
        int end = xml.indexOf(endTag);
        if(start == -1 || end == -1){ return null; }
        return xml.substring(start, end + endTag.length());
    }
     
    private List<JSONObject> xmlToJson(String xml, String startTag, String endTag) {
        String nextBetween = "";
        List<JSONObject> results = new ArrayList<>();
        while( (nextBetween = getNextXmlTag(xml, startTag, endTag)) != null ){
            JSONObject xmlJSONObj = org.json.XML.toJSONObject(nextBetween);
            results.add(xmlJSONObj);
            xml = xml.substring(nextBetween.length()); //moving pointer to next tag
        }
        return results;
    }
     
    private Map<String, String> getRequirementsFromSlaTerm(String slaAgreementTerm, String guaranteedValue) {
        Map<String, String> vmRequirements = new HashMap<>();
        String cpuRequirements = "";
        String diskRequirements = "";
        if(slaAgreementTerm.equals("hw_platform")){
            String hardware_requirements[] = guaranteedValue.split(";");
            if(hardware_requirements.length >= 1){
               cpuRequirements = hardware_requirements[0];
            }
            if(hardware_requirements.length >= 2){
               diskRequirements = hardware_requirements[1];
            }
        }

        if(!cpuRequirements.equals("")){
            String cpu_requirements[] = cpuRequirements.split("/");
            if(cpu_requirements.length >= 1){ vmRequirements.put("processor_architecture", cpu_requirements[0]); }
            if(cpu_requirements.length >= 2){ vmRequirements.put("processor_brand", cpu_requirements[1]); }
            if(cpu_requirements.length >= 3){ vmRequirements.put("processor_model", cpu_requirements[2]); }
        }

        if(!diskRequirements.equals("")){
            String disk_requirements[] = diskRequirements.split("/");
            if(disk_requirements.length >= 1){ vmRequirements.put("disk_type", disk_requirements[0]); }
        }

        return vmRequirements;
    }
    
    /**
     * Reads an xml with ViolationMessages and detects the different 
     * vm_requirements that will be needed for self-adaptation.
     * 
     * @param xml
     */
    public void readRequirementsFromViolationMessages(String xml){
        Map<String, Map<String, String>> newRequirements = new HashMap<>();
         for(JSONObject xmlJSONObj : xmlToJson(xml, "<ViolationMessage", "</ViolationMessage>")){
            String vm_id = xmlJSONObj
                    .getJSONObject("ViolationMessage")
                    .getString("vmId");
            String slaAgreementTerm = xmlJSONObj
                    .getJSONObject("ViolationMessage")
                    .getJSONObject("alert")
                    .getString("slaAgreementTerm");
            String guaranteedValue = xmlJSONObj
                    .getJSONObject("ViolationMessage")
                    .getJSONObject("alert")
                    .getJSONObject("slaGuaranteedState")
                    .getString("guaranteedValue");
            String operator = xmlJSONObj
                    .getJSONObject("ViolationMessage")
                    .getJSONObject("alert")
                    .getJSONObject("slaGuaranteedState")
                    .getString("operator");

            log.info("NEW REQUIREMENT: vmId = " + vm_id + 
                    "; slaAgreementTerm = " + slaAgreementTerm + 
                    "; guaranteedValue = " + guaranteedValue + 
                    "; operator = " + operator);
            newRequirements.put(vm_id, getRequirementsFromSlaTerm(slaAgreementTerm, guaranteedValue));
         }
         log.info("ALL REQUIREMENTS: " + newRequirements.toString());
         
         this.slamRequirements = newRequirements;
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
