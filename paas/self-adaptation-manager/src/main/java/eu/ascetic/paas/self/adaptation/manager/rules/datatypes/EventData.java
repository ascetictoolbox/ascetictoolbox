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
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents an event that arrives at self-adaptation manager for
 * assessment.
 *
 * @author Richard Kavanagh
 */
public class EventData implements Comparable<EventData> {

    private long time; //the time of the event
    private double rawValue; //the metric raw value
    private double guranteedValue; //the guranteed value
    private EventData.Type type; //breach, warning or other (i.e. informative)
    private EventData.Operator guranteeOperator; // threshold direction
    private String slaUuid; //sla id
    private String applicationId;
    private String deploymentId;
    private String agreementTerm;
    private String guaranteeid; //sla gurantee id
    //augments information about the event, with the original deployment information
    private OvfDefinition ovf;

    private static final Map<String, Operator> OPERATOR_MAPPING
            = new HashMap<>();

    static {
        OPERATOR_MAPPING.put("LT", EventData.Operator.LT);
        OPERATOR_MAPPING.put("LTE", EventData.Operator.LTE);
        OPERATOR_MAPPING.put("EQ", EventData.Operator.EQ);
        OPERATOR_MAPPING.put("GT", EventData.Operator.GT);
        OPERATOR_MAPPING.put("GTE", EventData.Operator.GTE);
        OPERATOR_MAPPING.put(null, null);
        OPERATOR_MAPPING.put("", null);
    }

    /**
     * This is an enumeration that indicates if the event notification is of a
     * breach or a proximity warning
     */
    public enum Type {

        SLA_BREACH, WARNING, OTHER
    }

    /**
     * The operator used to say what the nature of the guarantee is.
     */
    public enum Operator {

        LT, LTE, EQ, GT, GTE
    }

    /**
     * The no-args constructor for event Data.
     */
    public EventData() {
    }

    /**
     * This creates a new event data object.
     *
     * @param time The time of the event (Unix time).
     * @param rawValue The raw value reported by the SLA manager, for the metric
     * that breached its guarantee.
     * @param guranteedValue The value for the threshold that forms the
     * guarantee placed upon the value that breached.
     * @param type This indicates if the event notifies of a breach or a warning
     * of a potential future breach.
     * @param guranteeOperator The operator that defines the threshold placed
     * upon the guarantee. e.g. greater_than, less_than ...
     * @param slaUuid The uuid for the SLA
     * @param applicationId The id of the application that caused the breach
     * @param deploymentId The id of the specific deployment that caused the
     * breach
     * @param guaranteeid The id of the guarantee that was breached
     * @param agreementTerm The type of guarantee that was breached.
     */
    public EventData(long time, double rawValue, double guranteedValue, Type type,
            Operator guranteeOperator, String slaUuid, String applicationId,
            String deploymentId, String guaranteeid, String agreementTerm) {
        this.time = time;
        this.rawValue = rawValue;
        this.guranteedValue = guranteedValue;
        this.type = type;
        this.guranteeOperator = guranteeOperator;
        this.slaUuid = slaUuid;
        this.applicationId = applicationId;
        this.deploymentId = deploymentId;
        this.guaranteeid = guaranteeid;
        this.agreementTerm = agreementTerm;
    }

    /**
     * @return the guranteeOperator
     */
    public EventData.Operator getGuranteeOperator() {
        return guranteeOperator;
    }

    /**
     * @param guranteeOperator the guranteeOperator to set
     */
    public void setGuranteeOperator(EventData.Operator guranteeOperator) {
        this.guranteeOperator = guranteeOperator;
    }

    /**
     * This gets the value of the SLA Id that is associated with the
     * notification event
     *
     * @return the slaUuid
     */
    public String getSlaUuid() {
        return slaUuid;
    }

    /**
     * This sets the value of the SLA Id that is associated with the
     * notification event
     *
     * @param slaUuid the slaUuid to set
     */
    public void setSlaUuid(String slaUuid) {
        this.slaUuid = slaUuid;
    }

    /**
     * @return the guarantee id
     */
    public String getGuaranteeid() {
        return guaranteeid;
    }

    /**
     * @param guranteeid the guarantee id to set
     */
    public void setGuaranteeid(String guranteeid) {
        this.guaranteeid = guranteeid;
    }

    public void setAgreementTerm(String agreementTerm) {
        this.agreementTerm = agreementTerm;
    }

    public String getAgreementTerm() {
        return agreementTerm;
    }

    /**
     * @return the time
     */
    public long getTime() {
        return time;
    }

    /**
     * @param time the time to set
     */
    public void setTime(long time) {
        this.time = time;
    }

    /**
     * @return the type
     */
    public EventData.Type getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(EventData.Type type) {
        this.type = type;
    }

    /**
     * @return the rawValue
     */
    public double getRawValue() {
        return rawValue;
    }

    /**
     * @param rawValue the rawValue to set
     */
    public void setRawValue(double rawValue) {
        this.rawValue = rawValue;
    }

    /**
     * @return the guranteedValue
     */
    public double getGuranteedValue() {
        return guranteedValue;
    }

    /**
     * @param guranteedValue the guranteedValue to set
     */
    public void setGuranteedValue(double guranteedValue) {
        this.guranteedValue = guranteedValue;
    }

    /**
     * This gets the application id associated with the origin of the event.
     *
     * @return the applicationId
     */
    public String getApplicationId() {
        return applicationId;
    }

    /**
     * This sets the application id associated with the origin of the event.
     *
     * @param applicationId the applicationId to set
     */
    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    /**
     * This gets the deployment id associated with the origin of the event.
     *
     * @return the deploymentId
     */
    public String getDeploymentId() {
        return deploymentId;
    }

    /**
     * This sets the deployment id associated with the origin of the event.
     *
     * @param deploymentId the deploymentId to set
     */
    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    /**
     * This gets the OVF definition of the original deployment.
     *
     * @return the ovf definition of the original deployment
     */
    public OvfDefinition getOvf() {
        return ovf;
    }

    /**
     * This sets the OVF definition of the original deployment.
     *
     * @param ovf the ovf description of the original deployment.
     */
    public void setOvf(OvfDefinition ovf) {
        this.ovf = ovf;
    }

    @Override
    public int compareTo(EventData event) {
        //This sequences event data in cronlogical order.
        return Long.compare(this.getTime(), event.getTime());
    }

    /**
     * This provides the mapping between the string representation of a operator
     * and the operator.
     *
     * @param operator The string representation of the operator.
     * @return The operator required.
     */
    public static Operator getOperator(String operator) {
        return OPERATOR_MAPPING.get(operator);
    }

    /**
     * This returns the difference between the guaranteed value and the raw
     * value.
     *
     * @return
     */
    public double getDeviationBetweenRawAndGuarantee() {
        return guranteedValue - rawValue;
    }

    /**
     * This returns the difference between the guaranteed value and the raw
     * value.
     *
     * @param absolute If the absolute difference should be shown or not
     * @return
     */
    public double getDeviationBetweenRawAndGuarantee(boolean absolute) {
        double answer = guranteedValue - rawValue;
        if (absolute) {
            return Math.abs(answer);
        }
        return answer;
    }

    /**
     * This returns the difference between the raw value and the absolute value,
     * in terms of how much slack is present before the guarantee has been
     * breached.
     *
     * @return The slack associated with the guarantee
     */
    public double getGuaranteeSlack() {
        switch (guranteeOperator) {
            case EQ:
                return Math.abs(rawValue - guranteedValue);
            case GT:
            case GTE:
                return rawValue - guranteedValue;
            case LT:
            case LTE:
                return guranteedValue - rawValue;
        }
        return 0;
    }

    /**
     * This checks to see if the deviation between two events is increasing or
     * not.
     *
     * @param earlier The earlier event
     * @param later The later event
     * @return The change in deviation between the raw and guaranteed value.
     */
    public static double getDifferenceBetweenDeviations(EventData earlier, EventData later) {
        return Math.abs(earlier.getDeviationBetweenRawAndGuarantee()) - Math.abs(later.getDeviationBetweenRawAndGuarantee());
    }

    /**
     * This checks to see if the deviation between two events amount of
     * available slack is increasing or decreasing.
     *
     * @param earlier The earlier event
     * @param later The later event
     * @return The change in available SLA slack.
     */
    public static double getChangeInSlack(EventData earlier, EventData later) {
        return earlier.getGuaranteeSlack() - later.getGuaranteeSlack();
    }

    /**
     * This safely converts a string into an Type. In the event the type isn't
     * recognised the other type is returned.
     *
     * @param type The string representation of the type
     * @return
     */
    public static Type getType(String type) {
        if (type == null) {
            return EventData.Type.OTHER;
        }
        //SLA_BREACH, WARNING, OTHER
        if (type.equalsIgnoreCase("SLA_BREACH") || type.toLowerCase().contains("breach")) {
            return Type.SLA_BREACH;
        }
        if (type.equalsIgnoreCase("WARNING") || type.toLowerCase().contains("warning")) {
            return Type.WARNING;
        }
        return Type.OTHER; //This is an information type event.
    }

}
