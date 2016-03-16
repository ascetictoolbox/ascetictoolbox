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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage;

/**
 * This class represents the load history of a VM that is based upon weekly
 * load cycles.
 * @author Richard Kavanagh
 */
public class VmLoadHistoryWeekRecord extends VmLoadHistoryRecord {

    /**
     * This holds the fields: SELECT avg(cpu_load), STDDEV_POP(cpu_load),"
     * "Weekday(FROM_UNIXTIME(clock)) as Day_of_Week, "
     * "Hour(FROM_UNIXTIME(clock)) as Hour_in_Day "
     *
     * the load factor value is generic and could be for any given device, not
     * just the CPU.
     */
    
    private final int dayOfWeek;
    private final int hourOfDay;

    /**
     * This creates a new VM load record, providing average load information
     * for a given hour and day of the week.
     * @param dayOfWeek The day of the week
     * @param hourOfDay The hour of the day
     * @param utilisation The average cpu utilisation found to be induced by the VM
     * @param stdDev The standard deviation, used to give a notion of spread 
     * from the average (lower is better)
     */
    public VmLoadHistoryWeekRecord(int dayOfWeek, int hourOfDay, double utilisation, double stdDev) {
        super(utilisation,stdDev);
        this.dayOfWeek = dayOfWeek;
        this.hourOfDay = hourOfDay;
    }

    /**
     * This gets the day of the week of this load history record.
     * @return The day of the week (0-6).
     */
    public int getDayOfWeek() {
        return dayOfWeek;
    }

    /**
     * This gets the hour of the day for this load history record.
     * @return The hour of the day.
     */
    public int getHourOfDay() {
        return hourOfDay;
    }
    
}
