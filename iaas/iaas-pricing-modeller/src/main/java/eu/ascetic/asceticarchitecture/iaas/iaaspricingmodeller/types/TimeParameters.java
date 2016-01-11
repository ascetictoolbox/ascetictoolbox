/**
 * Copyright 2014 University of Leeds
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
package eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.types;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.cost.IaaSPricingModellerCost;

/**
 * This represents a duration of time/a time period, it is to be used as part of
 * the ASCETiC energy modellers query system.
 *
 * @author Richard
 */
public class TimeParameters {

    private Calendar startTime;
    private Calendar endTime;
  //  private Calendar lastChange;
    
    static Logger logger = null;
    
    public TimeParameters (){
    	startTime = Calendar.getInstance();
    	//System.out.println("start time " +startTime.getTimeInMillis());
    	this.endTime = Calendar.getInstance();
    	//System.out.println("End time " +endTime.getTimeInMillis());
    	//lastChange =  Calendar.getInstance();
    	//System.out.println("last time " +lastChange.getTimeInMillis());
    	logger = Logger.getLogger(TimeParameters.class);
    }
    
    public TimeParameters (long duration){
    	startTime = Calendar.getInstance();
    	this.endTime = Calendar.getInstance();
    	endTime.setTimeInMillis(endTime.getTimeInMillis() + TimeUnit.SECONDS.toMillis(duration));
    	// lastChange = Calendar.getInstance();
    	logger = Logger.getLogger(TimeParameters.class);
    }
    
    /**
     * This creates an object that represents a duration of time/a time period,
     * it is to be used as part of the ASCETiC energy modellers query system.
     *
     * @param startTime The time that represents the start of the query's
     * dataset, as represented in Unix time.
     * @param endTime The time that represents the end of the query's dataset, 
     * as represented in Unix time.
     */
    public TimeParameters(long startTime, long endTime) {
        this.startTime = new GregorianCalendar();
        this.startTime.setTimeInMillis(TimeUnit.SECONDS.toMillis(startTime));
        this.endTime = new GregorianCalendar();
        this.endTime.setTimeInMillis(TimeUnit.SECONDS.toMillis(endTime));
       // this.lastChange = this.startTime;
        logger = Logger.getLogger(TimeParameters.class);
    }    
    
    /**
     * This creates an object that represents a duration of time/a time period,
     * it is to be used as part of the ASCETiC energy modellers query system.
     *
     * @param startTime The time that represents the start of the query's
     * dataset
     * @param endTime The time that represents the end of the query's dataset
     */
    public TimeParameters(Calendar startTime, Calendar endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
        //this.lastChange = (Calendar) startTime.clone();
        logger = Logger.getLogger(TimeParameters.class);
    }

    /**
     * This creates an object that represents a duration of time/a time period,
     * it is to be used as part of the ASCETiC energy modellers query system.
     *
     * @param startTime The time that represents the start of the query's
     * dataset
     * @param seconds The amount of time to create the duration for in seconds
     */
    public TimeParameters(Calendar startTime, long seconds) {
        this.startTime = startTime;
        this.endTime = (Calendar) startTime.clone();
        endTime.setTimeInMillis(endTime.getTimeInMillis() + TimeUnit.SECONDS.toMillis(seconds));
        //this.lastChange = (Calendar) startTime.clone();
        logger = Logger.getLogger(TimeParameters.class);
    }

    /**
     * This creates an object that represents a duration of time/a time period,
     * it is to be used as part of the ASCETiC energy modellers query system.
     *
     * @param startTime The time that represents the start of the query's
     * dataset
     * @param time The amount of time to create the duration for
     * @param sourceUnit The units of the time parameter
     */
    public TimeParameters(Calendar startTime, int time, TimeUnit sourceUnit) {
        this.startTime = startTime;
        this.endTime = startTime;
        endTime.setTimeInMillis(endTime.getTimeInMillis() + sourceUnit.toMillis(time));
        logger = Logger.getLogger(TimeParameters.class);
    }

    /**
     * This creates an object that represents a duration of time/a time period,
     * it is to be used as part of the ASCETiC energy modellers query system.
     *
     * @param time The amount of time to create the duration for
     * @param sourceUnit The units of the time parameter
     */
    public TimeParameters(int time, TimeUnit sourceUnit) {
        this.startTime = Calendar.getInstance();
        this.endTime = Calendar.getInstance();
        endTime.setTimeInMillis(endTime.getTimeInMillis() - sourceUnit.toMillis(time));
        logger = Logger.getLogger(TimeParameters.class);
    }

    /**
     * This returns the amount of seconds from the start to the end of this
     * records time period.
     *
     * @return The time in seconds between the start time and the end time.
     */
    public long getTotalDuration() {
        if (getStartTime() == null || getEndTime() == null) {
            return -1;
        }
        if (getEndTime().getTimeInMillis()>=getStartTime().getTimeInMillis()){
        	long end = getEndTime().getTimeInMillis();
        	//System.out.println("end: " +end);
        	long start = getStartTime().getTimeInMillis();
        	//System.out.println("start: " +start);
        	//System.out.println("Diff: " +(end-start));
        	return TimeUnit.MILLISECONDS.toSeconds(end - start);
        	
        }
        else{
        	logger.info("End time is less than start time");
        	return -1;
        }
        	
    }
    
    public long getDuration(Calendar startTime, Calendar endTime) {
        if (getStartTime() == null || getEndTime() == null) {
            return -1;
        }
        long end = endTime.getTime().getTime();

        long start = startTime.getTime().getTime();
        return TimeUnit.MILLISECONDS.toSeconds(end - start);
    }


    /**
     * This converts a duration given by this class to minutes.
     *
     * @param duration The duration
     * @return The duration of the time period specified, in minutes.
     */
    public static long convertToMinutes(TimeParameters duration) {
        return TimeUnit.SECONDS.toMinutes(duration.getTotalDuration());
    }

    /**
     * This converts a duration given by this class to hours.
     *
     * @param duration The duration
     * @return The duration of the time period specified, in hours.
     */
    public static long convertToHours(TimeParameters duration) {
        return TimeUnit.SECONDS.toHours(duration.getTotalDuration());
    }

    /**
     * This converts a duration given by this class to days.
     *
     * @param duration The duration
     * @return The duration of the time period specified, in days.
     */
    public static long convertToDays(TimeParameters duration) {
        return TimeUnit.SECONDS.toDays(duration.getTotalDuration());
    }

    /**
     * The start time for the duration the query represents.
     *
     * @return the start time, for this time period.
     */
    public Calendar getStartTime() {
        return startTime;
    }
    
   /* public void setStartTime(){
    	startTime = Calendar.getInstance();
    }*/
    
    /**
     * The start time for the duration the query represents.
     *
     * @return the start time, for this time period in seconds.
     */
    public long getStartTimeInSeconds() {
        return TimeUnit.MILLISECONDS.toSeconds(startTime.getTimeInMillis());
    }    

    /**
     * The end time for the duration the query represents.
     *
     * @return the end time, for this time period.
     */
    public Calendar getEndTime() {
        return endTime;
    }
    
    public void setEndTime(long endTime){
    	  this.endTime = new GregorianCalendar();
          this.endTime.setTimeInMillis(TimeUnit.SECONDS.toMillis(endTime));
    }
    /**
     * The start time for the duration the query represents.
     *
     * @return the end time, for this time period.
     */
    public long getEndTimeInSeconds() {
        return TimeUnit.MILLISECONDS.toSeconds(endTime.getTimeInMillis());
    }    
    

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TimeParameters) {
        	TimeParameters comp = (TimeParameters) obj;
            return (startTime.equals(comp.startTime)
                    && endTime.equals(comp.endTime));
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(this.startTime);
        hash = 29 * hash + Objects.hashCode(this.endTime);
        return hash;
    }



    @Override
    public String toString() {
        return "Start: " + startTime.getTime() + " End: " + endTime.getTime();
    }
    
  /*  public long getLastChangeTimeinSec(){
    	return TimeUnit.MILLISECONDS.toSeconds(lastChange.getTimeInMillis());
    }
    
    public Calendar getLastChangeTime(){
    	return lastChange;
    }
    
    public void setLastChangeTime(){
    	this.lastChange = Calendar.getInstance();
    	System.out.println("start " + startTime.getTimeInMillis());
    	System.out.println("end " + endTime.getTimeInMillis());
    	System.out.println("last " + lastChange.getTimeInMillis());
    }
    
    public void setLastChangeTime(Calendar time){
    	this.lastChange = time;
    }
    
    public void setLastChangeTime(long time){
    	 this.lastChange.setTimeInMillis(TimeUnit.SECONDS.toMillis(time));
    	
    }
   
    */
    public void setEndTime(){
    	this.endTime = Calendar.getInstance();
    }
}
