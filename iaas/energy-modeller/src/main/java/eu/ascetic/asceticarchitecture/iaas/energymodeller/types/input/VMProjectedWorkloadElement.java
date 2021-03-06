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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.types.input;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.TimePeriod;
import java.util.Calendar;

/**
 * This class represents an estimated workload for a virtual machine that is due
 * to be executed.
 *
 * @deprecated Not needed in the 1st year. 100% Load only should be considered.
 *
 * @author Richard
 */
public class VMProjectedWorkloadElement implements Comparable<Object> {

    /**
     * Either null for ongoing long term jobs providing averages or a projected
     * period of time for the deployment should be specified.
     */
    public TimePeriod duration;
    
    /*
     * TODO:
     * The exact type and values ranges for these numbers need considering
     * further. i.e. CPU should it be % of overall use?
     * memory usage is most important, does using more matter? No
     * does making many writes yes, and what about page faults?
     * i.e.
     *  cpuUsage
     *  networkUsage
     *  diskUsage
     *  memoryUsage
     */
   

    private static final int BEFORE = -1;
    private static final int EQUAL = 0;
    private static final int AFTER = 1;

    /**
     * This provides the start time of this workload element.
     *
     * @return The start time of this workload element, null if the workload is
     * open ended with no expected start or completion time.
     */
    public Calendar getStartTime() {
        if (duration == null) {
            return null;
        }
        return duration.getStartTime();
    }

    /**
     * This provides the end time of this workload element.
     *
     * @return The end time of this workload element, null if the workload is
     * open ended with no expected completion time.
     */
    public Calendar getEndTime() {
        if (duration == null) {
            return null;
        }
        return duration.getEndTime();
    }

    /**
     * This indicates if the workload represents a deployment that has a
     * projected end period or not.
     *
     * @return if the deployment is long term or not
     */
    public boolean isLongTermDeployment() {
        return duration == null;
    }

    /**
     * This returns the duration of the projected workload element
     *
     * @return The duration this projected workload element describes.
     */
    public TimePeriod getDuration() {
        return duration;
    }

    /**
     * This sets the duration of the projected workload element
     *
     * @param duration The duration this projected workload element should
     * describe.
     */
    public void setDuration(TimePeriod duration) {
        this.duration = duration;
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof VMProjectedWorkloadElement) {
            VMProjectedWorkloadElement comparable = (VMProjectedWorkloadElement) o;
            if (this.isLongTermDeployment()) {
                if (comparable.isLongTermDeployment()) {
                    return EQUAL;
                } else {
                    return AFTER;
                }
            }
            if (comparable.isLongTermDeployment()) {
                return BEFORE;
            }
            return duration.compareTo(comparable.duration);
        } else {
            throw new ClassCastException();
        }
    }
}
