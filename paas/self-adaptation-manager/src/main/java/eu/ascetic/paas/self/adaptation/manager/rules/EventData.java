/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.ascetic.paas.self.adaptation.manager.rules;

/**
 * This class represents an event that arrives at self-adaptation manager for 
 * assessment.
 * @author Richard Kavanagh
 */
public class EventData {

    private long time; //the time of the event
    private double rawValue; //the metric raw value
    private double guranteedValue; //the guranteed value
    private EventData.Type type; //breach vs warning
    private EventData.Operator guranteeOperator; // threshold direction
    private String slaUuid; //sla id
    private int guaranteeid; //sla gurantee id

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
    public int getGuaranteeid() {
        return guaranteeid;
    }

    /**
     * @param guranteeid the guarantee id to set
     */
    public void setGuaranteeid(int guranteeid) {
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
     * This returns the absolute difference between the raw value and the absolute
     * value.
     * @return 
     */
    public double getDeviationBetweenRawAndGuarantee() {
        return Math.abs(rawValue - guranteedValue);
    }
    
    /**
     * This checks to see if the deviation between two events is increasing or not.
     * @param earlier The earlier event
     * @param later The later event
     * @return The change in deviation between the raw and guaranteed value.
     */
    public static double getDifferenceBetweenDeviations(EventData earlier, EventData later) {
        return earlier.getDeviationBetweenRawAndGuarantee() - later.getDeviationBetweenRawAndGuarantee();
    }    
    
}
