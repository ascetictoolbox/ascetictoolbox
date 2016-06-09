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
import java.util.ArrayList;

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
     * This examines the OVF for rules associated with the application. It extracts
     * the rules and returns them.
     *
     * @param ovf The OVF to extract the firing criteria from
     * @return The firing criteria that are from the application.
     */
    public static ArrayList<FiringCriteria> getFiringCriteriaFromOVF(OvfDefinition ovf) {
        ArrayList<FiringCriteria> answer = new ArrayList<>();
        for (ProductSection section : ovf.getVirtualSystemCollection().getProductSectionArray()) {
            int maxValue = section.getAdaptationRuleNumber();
            for (int i = 0; i < maxValue; i++) {
                FiringCriteria criteria = new FiringCriteria(
                        section.getAdaptationRuleAgreementTerm(i),
                        section.getAdaptationRuleDirection(i),
                        section.getAdaptationRuleResponseType(i));
                if (section.getAdaptationRuleLowerBound(i) != null) {
                    criteria.setMinMagnitude(Double.parseDouble(section.getAdaptationRuleLowerBound(i)));
                }
                if (section.getAdaptationRuleUpperBound(i) != null) {
                    criteria.setMaxMagnitude(Double.parseDouble(section.getAdaptationRuleUpperBound(i)));
                }
                if (section.getAdaptationRuleNotificationType(i) != null) {
                    criteria.setType(EventData.Type.valueOf(section.getAdaptationRuleNotificationType(i)));
                }                
                answer.add(criteria);
            }
        }
        return answer;
    }

}
