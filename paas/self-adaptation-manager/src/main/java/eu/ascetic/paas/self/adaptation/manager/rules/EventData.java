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
package eu.ascetic.paas.self.adaptation.manager.rules;

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
    private EventData.Type type; //breach vs warning
    private EventData.Operator guranteeOperator; // threshold direction
    private String slaUuid; //sla id
    private String guaranteeid; //sla gurantee id

    /**
     * 
     */
    public EventData() {
    }
    
    /**
     * 
     * @param time
     * @param rawValue
     * @param guranteedValue
     * @param type
     * @param guranteeOperator
     * @param slaUuid
     * @param guaranteeid 
     */
    public EventData(long time, double rawValue, double guranteedValue, Type type, Operator guranteeOperator, String slaUuid, String guaranteeid) {
        this.time = time;
        this.rawValue = rawValue;
        this.guranteedValue = guranteedValue;
        this.type = type;
        this.guranteeOperator = guranteeOperator;
        this.slaUuid = slaUuid;
        this.guaranteeid = guaranteeid;
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
     * @return the slaUuid
     */
    public String getSlaUuid() {
        return slaUuid;
    }

    /**
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

    @Override
    public int compareTo(EventData event) {
        //This sequences event data in cronlogical order.
        return Long.compare(this.getTime(), event.getTime());
    }

    /**
     * This is an enumeration that indicates if the event notification is of a
     * breach or a proximity warning
     */
    public enum Type {

        SLA_BREACH, WARNING
    }

    /**
     * The operator used to say what the nature of the guarantee is.
     */
    public enum Operator {

        LT, LTE, EQ, GT, GTE
    }

    /**
     * This returns the absolute difference between the raw value and the
     * absolute value.
     *
     * @return
     */
    public double getDeviationBetweenRawAndGuarantee() {
        return Math.abs(rawValue - guranteedValue);
    }

    /**
     * This returns the difference between the raw value and the
     * absolute value, in terms of how much slack is present before the guarantee
     * has been breached.
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
        return earlier.getDeviationBetweenRawAndGuarantee() - later.getDeviationBetweenRawAndGuarantee();
    }
    
    /**
     * This checks to see if the deviation between two events amount of available 
     * slack is increasing or decreasing.
     * 
     * @param earlier The earlier event
     * @param later The later event
     * @return  The change in available SLA slack.
     */
    public static double getChangeInSlack(EventData earlier, EventData later) {
        return earlier.getGuaranteeSlack() - later.getGuaranteeSlack();
    }    

}
