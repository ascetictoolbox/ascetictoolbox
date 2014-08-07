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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.types;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * This represents a duration of time/a time period, it is to be used as part of
 * the ASCETiC energy modellers query system.
 *
 * @author Richard
 */
public class TimePeriod implements Comparable<TimePeriod> {

    private final Calendar startTime;
    private final Calendar endTime;

    /**
     * This creates an object that represents a duration of time/a time period,
     * it is to be used as part of the ASCETiC energy modellers query system.
     *
     * @param startTime The time that represents the start of the query's
     * dataset, as represented in Unix time.
     * @param endTime The time that represents the end of the query's dataset, 
     * as represented in Unix time.
     */
    public TimePeriod(long startTime, long endTime) {
        this.startTime = new GregorianCalendar();
        this.startTime.setTimeInMillis(TimeUnit.SECONDS.toMillis(startTime));
        this.endTime = new GregorianCalendar();
        this.endTime.setTimeInMillis(TimeUnit.SECONDS.toMillis(endTime));
    }    
    
    /**
     * This creates an object that represents a duration of time/a time period,
     * it is to be used as part of the ASCETiC energy modellers query system.
     *
     * @param startTime The time that represents the start of the query's
     * dataset
     * @param endTime The time that represents the end of the query's dataset
     */
    public TimePeriod(Calendar startTime, Calendar endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * This creates an object that represents a duration of time/a time period,
     * it is to be used as part of the ASCETiC energy modellers query system.
     *
     * @param startTime The time that represents the start of the query's
     * dataset
     * @param seconds The amount of time to create the duration for in seconds
     */
    public TimePeriod(Calendar startTime, long seconds) {
        this.startTime = startTime;
        this.endTime = (Calendar) startTime.clone();
        endTime.setTimeInMillis(endTime.getTimeInMillis() + TimeUnit.SECONDS.toMillis(seconds));
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
    public TimePeriod(Calendar startTime, int time, TimeUnit sourceUnit) {
        this.startTime = startTime;
        this.endTime = (Calendar) startTime.clone();
        endTime.setTimeInMillis(endTime.getTimeInMillis() + sourceUnit.toMillis(time));
    }

    /**
     * This creates an object that represents a duration of time/a time period,
     * it is to be used as part of the ASCETiC energy modellers query system.
     *
     * @param time The amount of time to create the duration for
     * @param sourceUnit The units of the time parameter
     */
    public TimePeriod(int time, TimeUnit sourceUnit) {
        this.startTime = Calendar.getInstance();
        this.endTime = (Calendar) startTime.clone();
        endTime.setTimeInMillis(endTime.getTimeInMillis() - sourceUnit.toMillis(time));
    }

    /**
     * This returns the amount of seconds from the start to the end of this
     * records time period.
     *
     * @return The time in seconds between the start time and the end time.
     */
    public long getDuration() {
        if (getStartTime() == null || getEndTime() == null) {
            return -1;
        }
        long end = getEndTime().getTime().getTime();
        long start = getStartTime().getTime().getTime();
        return TimeUnit.MILLISECONDS.toSeconds(end - start);
    }

    /**
     * This converts a duration given by this class to minutes.
     *
     * @param duration The duration
     * @return The duration of the time period specified, in minutes.
     */
    public static long convertToMinutes(TimePeriod duration) {
        return TimeUnit.SECONDS.toMinutes(duration.getDuration());
    }

    /**
     * This converts a duration given by this class to hours.
     *
     * @param duration The duration
     * @return The duration of the time period specified, in hours.
     */
    public static long convertToHours(TimePeriod duration) {
        return TimeUnit.SECONDS.toHours(duration.getDuration());
    }

    /**
     * This converts a duration given by this class to days.
     *
     * @param duration The duration
     * @return The duration of the time period specified, in days.
     */
    public static long convertToDays(TimePeriod duration) {
        return TimeUnit.SECONDS.toDays(duration.getDuration());
    }

    /**
     * The start time for the duration the query represents.
     *
     * @return the start time, for this time period.
     */
    public Calendar getStartTime() {
        return startTime;
    }
    
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
        if (obj instanceof TimePeriod) {
            TimePeriod comp = (TimePeriod) obj;
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
    public int compareTo(TimePeriod other) {
            int start = startTime.compareTo(other.startTime);
            if (start != 0) {
                return start;
            }
            int end = endTime.compareTo(other.endTime);
            return end;
    }

    @Override
    public String toString() {
        return "Start: " + startTime.getTime() + " End: " + endTime.getTime();
    }
    
}
