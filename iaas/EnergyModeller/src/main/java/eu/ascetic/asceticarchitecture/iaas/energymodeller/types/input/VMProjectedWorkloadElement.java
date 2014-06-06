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

import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.TimePeriod;
import java.util.Calendar;

/**
 * This class represents an estimated workload for a virtual machine that is due
 * to be executed.
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
     */
    public double cpuUsage;
    public double networkUsage;
    public double diskUsage;
    public double memoryUsage;

    public Calendar getStartTime() {
        if (duration == null) {
            return null;
        }
        return duration.getStartTime();
    }

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
     * @return
     */
    public boolean isLongTermDeployment() {
        return duration == null;
    }

    @Override
    public int compareTo(Object o) {
        int BEFORE = -1;
        int EQUAL = 0;
        int AFTER = 1;
        if (o instanceof VMProjectedWorkloadElement) {
            VMProjectedWorkloadElement comparable = (VMProjectedWorkloadElement) o;
            if (this.isLongTermDeployment()) {
                if (comparable.isLongTermDeployment()) {
                    return EQUAL;
                } else {
                    return BEFORE;
                }
            }
            if (comparable.isLongTermDeployment()) {
                return AFTER;
            }
            return duration.compareTo(comparable.duration);
        } else {
            throw new ClassCastException();
        }
    }
}
