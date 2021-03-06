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

import eu.ascetic.utils.ovf.api.OvfDefinition;
import eu.ascetic.utils.ovf.api.ProductSection;
import eu.ascetic.utils.ovf.api.VirtualSystem;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is the firing criteria for a threshold based event assessor. It contains
 * the mappings between an an agreement term and the rule that is required to be
 * fired.
 *
 * @author Richard Kavanagh
 */
public class FiringCriteria {

    private String agreementTerm;
    private EventData.Operator operator;
    private EventData.Type type;
    private Response.AdaptationType responseType;
    private Double minMagnitude = null;
    private Double maxMagnitude = null;
    private String parameters = "";

    public FiringCriteria() {
    }

    /**
     * This create a new firing criteria.
     *
     * @param agreementTerm The type of guarantee used.
     * @param operator If it is above or below the threshold value
     * @param responseType The type of response to give
     */
    public FiringCriteria(String agreementTerm, EventData.Operator operator, Response.AdaptationType responseType) {
        this.agreementTerm = agreementTerm;
        this.operator = operator;
        this.responseType = responseType;
    }

    /**
     * This create a new firing criteria.
     *
     * @param agreementTerm The type of guarantee used.
     * @param responseType The type of response to give
     * @param operator If it is above or below the threshold value
     */
    public FiringCriteria(String agreementTerm, String operator, String responseType) {
        this.agreementTerm = agreementTerm;
        this.operator = EventData.getOperator(operator);
        this.responseType = Response.getAdaptationType(responseType);
    }

    /**
     * This indicates if the specified event would meet the firing criteria.
     *
     * @param event The event to test to see if it would meet the firing
     * criteria
     * @return true only if the criteria are met.
     */
    public boolean shouldFire(EventData event) {
        if (minMagnitude != null && event.getDeviationBetweenRawAndGuarantee() < minMagnitude) {
            return false;
        }
        if (maxMagnitude != null && event.getDeviationBetweenRawAndGuarantee() > maxMagnitude) {
            return false;
        }
        if (type != null && !event.getType().equals(type)) {
            return false;
        }
        return (agreementTerm.equals(event.getAgreementTerm())
                && operator.equals(event.getGuranteeOperator()));
    }

    /**
     * The term in the SLA that is caused the breach, must match this in order
     * for the rule to fire.
     *
     * @return the agreementTerm The SLA agreement term that must be matched for
     * the rule to be applied.
     */
    public String getAgreementTerm() {
        return agreementTerm;
    }

    /**
     * The term in the SLA that is caused the breach, must match this in order
     * for the rule to fire.
     *
     * @param agreementTerm The SLA agreement term that must be matched for the
     * rule to be applied.
     */
    public void setAgreementTerm(String agreementTerm) {
        this.agreementTerm = agreementTerm;
    }

    /**
     * The form of response that is associated with the firing of this rule.
     *
     * @return the response to execute given the firing of this rule.
     */
    public Response.AdaptationType getResponseType() {
        return responseType;
    }

    /**
     * This sets the form of response that is associated with the firing of this
     * rule.
     *
     * @param responseType the response to execute given the firing of this
     * rule.
     */
    public void setResponseType(Response.AdaptationType responseType) {
        this.responseType = responseType;
    }

    /**
     * This returns the operator that is used as part of the test to see if the
     * measured value is "greater than"/"less than"/"equal" to the guaranteed
     * value.
     *
     * @return the operator either LT, LTE, EQ, GTE, GT
     */
    public EventData.Operator getOperator() {
        return operator;
    }

    /**
     * This sets the operator that is used as part of the test to see if the
     * measured value is "greater than"/"less than"/"equal" to the guaranteed
     * value.
     *
     * @param operator the operator either LT, LTE, EQ, GTE, GT
     */
    public void setOperator(EventData.Operator operator) {
        this.operator = operator;
    }

    /**
     * This gets the optional parameter of the Event type, i.e. if the rule
     * should fire only because of warnings or because of actual violation
     * notifications.
     *
     * @return The event violation type. either SLA_BREACH or WARNING
     */
    public EventData.Type getType() {
        return type;
    }

    /**
     * This sets the optional parameter of the Event type, i.e. if the rule
     * should fire only because of warnings or because of actual violation
     * notifications.
     *
     * @param type The event violation type to set, either SLA_BREACH or WARNING
     */
    public void setType(EventData.Type type) {
        this.type = type;
    }

    /**
     * This indicates the minimum magnitude for the rule to fire, for the
     * absolute difference between the guaranteed value and the measured value.
     * This value is optional.
     *
     * @return the minimum magnitude before this rule fires (inclusive).
     */
    public double getMinMagnitude() {
        return minMagnitude;
    }

    /**
     * This sets the minimum magnitude for the rule to fire, for the absolute
     * difference between the guaranteed value and the measured value. This
     * value is optional.
     *
     * @param minMagnitude the minimum magnitude before this rule fires
     * (inclusive).
     */
    public void setMinMagnitude(double minMagnitude) {
        this.minMagnitude = minMagnitude;
    }

    /**
     * This indicates the minimum magnitude for the rule to fire, for the
     * absolute difference between the guaranteed value and the measured value.
     * This value is optional.
     *
     * @return the maximum magnitude before this rule fires (inclusive).
     */
    public double getMaxMagnitude() {
        return maxMagnitude;
    }

    /**
     * This sets the maximum magnitude for the rule to fire, for the absolute
     * difference between the guaranteed value and the measured value. This
     * value is optional.
     *
     * @param maxMagnitude the maximum magnitude before this rule fires
     * (inclusive).
     */
    public void setMaxMagnitude(double maxMagnitude) {
        this.maxMagnitude = maxMagnitude;
        if (minMagnitude == null) {
            minMagnitude = 0.0;
        }
    }

    /**
     * This examines the OVF for rules associated with the application. It
     * extracts the rules and returns them.
     *
     * @param ovf The OVF to extract the firing criteria from
     * @return The firing criteria that are from the application.
     */
    public static ArrayList<FiringCriteria> getFiringCriteriaFromOVF(OvfDefinition ovf) {
        ArrayList<FiringCriteria> answer = new ArrayList<>();
        if (ovf == null) {
            return answer;
        }
        //This ensures rules that are gloabal are collected correctly
        for (ProductSection section : ovf.getVirtualSystemCollection().getProductSectionArray()) {
            answer.addAll(parseProductSection(section));
        }
        //This ensures rules that are attached to individual VMs are collected correctly
        for (VirtualSystem vm : ovf.getVirtualSystemCollection().getVirtualSystemArray()) {
            for (ProductSection section : vm.getProductSectionArray()) {
                answer.addAll(parseProductSection(section));
            }
        }
        return answer;
    }

    /**
     * This parses a product section either for a VM System Collection or for an
     * individual VM.
     *
     * @param section The product section to read Self-adaptation rules from
     * @return The list of self-adaptation rules that have been extracted.
     */
    private static ArrayList<FiringCriteria> parseProductSection(ProductSection section) {
        ArrayList<FiringCriteria> answer = new ArrayList<>();
        int maxValue = section.getAdaptationRuleNumber();
        for (int ruleNumber = 0; ruleNumber < maxValue; ruleNumber++) {
            try {
                FiringCriteria criteria = new FiringCriteria(
                        section.getAdaptationRuleSLATerm(ruleNumber),
                        section.getAdaptationRuleComparisonOperator(ruleNumber),
                        section.getAdaptationRuleResponseType(ruleNumber));
                if (criteria.getAgreementTerm() == null || criteria.getOperator() == null || criteria.getResponseType() == null) {
                    Logger.getLogger(FiringCriteria.class.getName()).log(Level.WARNING, "A rule from the OVF was not constructed correctly!");
                    continue;
                }
                if (section.getAdaptationRuleLowerBound(ruleNumber) != null) {
                    criteria.setMinMagnitude(Double.parseDouble(section.getAdaptationRuleLowerBound(ruleNumber)));
                }
                if (section.getAdaptationRuleUpperBound(ruleNumber) != null) {
                    criteria.setMaxMagnitude(Double.parseDouble(section.getAdaptationRuleUpperBound(ruleNumber)));
                }
                if (section.getAdaptationRuleNotificationType(ruleNumber) != null) {
                    criteria.setType(EventData.getType(section.getAdaptationRuleNotificationType(ruleNumber)));
                }
                if (section.getAdaptationRuleParameters(ruleNumber) != null) {
                    criteria.setParameters(section.getAdaptationRuleParameters(ruleNumber));
                }
                answer.add(criteria);
            } catch (Exception ex) {
                Logger.getLogger(FiringCriteria.class.getName()).log(Level.WARNING, "A rule from the OVF was not constructed correctly and caused a fault!", ex);
            }
        }
        return answer;
    }

    /**
     * Actuation rules sometimes need additional information in order to fire
     * correctly, such as how many VMs are needed, the type of VM to scale etc.
     * This provides a means in the rule sets to provide this extra parameter
     * information.
     *
     * @return the parameters
     */
    public String getParameters() {
        return parameters;
    }

    /**
     * Given the key value of the parameter this returns its value.
     *
     * @param key The key name for the actuation parameter
     * @return The value of the parameter else null.
     */
    public String getParameter(String key) {
        String[] args = parameters.split(";");
        for (String arg : args) {
            if (arg.split("=")[0].equals(key)) {
                return arg.split("=")[1].trim();
            }
        }
        return null;
    }

    /**
     * Actuation rules sometimes need additional information in order to fire
     * correctly, such as how many VMs are needed, the type of VM to scale etc.
     * This provides a means in the rule sets to provide this extra parameter
     * information.
     *
     * @param parameters the parameters to set as a semi-colon delimeter based
     * list of key value pairs. i.e. argument=value;argument2=three
     */
    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return "Rule: "
                + agreementTerm + ":"
                + operator + ":"
                + type + ":"
                + responseType + ":"
                + parameters;
    }

}
