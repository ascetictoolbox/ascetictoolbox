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
    private Response.AdaptationType responseType;

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
        return (agreementTerm.equals(event.getAgreementTerm())
                && operator.equals(event.getGuranteeOperator()));
    }

    /**
     * @return the agreementTerm
     */
    public String getAgreementTerm() {
        return agreementTerm;
    }

    /**
     * @param agreementTerm the agreementTerm to set
     */
    public void setAgreementTerm(String agreementTerm) {
        this.agreementTerm = agreementTerm;
    }

    /**
     * @return the responseType
     */
    public Response.AdaptationType getResponseType() {
        return responseType;
    }

    /**
     * @param responseType the responseType to set
     */
    public void setResponseType(Response.AdaptationType responseType) {
        this.responseType = responseType;
    }

    /**
     * @return the operator
     */
    public EventData.Operator getOperator() {
        return operator;
    }

    /**
     * @param operator the operator to set
     */
    public void setOperator(EventData.Operator operator) {
        this.operator = operator;
    }

}
