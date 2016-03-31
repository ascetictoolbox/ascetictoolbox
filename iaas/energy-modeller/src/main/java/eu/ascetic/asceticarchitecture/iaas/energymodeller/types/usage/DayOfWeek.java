/**
 * Copyright 2016 University of Leeds
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

import java.io.Serializable;

/**
 *
 * @author Richard Kavanagh
 */
public class DayOfWeek implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private final int day;
    private final int hour;

    public DayOfWeek(int day, int hour) {
        this.day = day;
        this.hour = hour;
    }

    /**
     * @return the day
     */
    public int getDay() {
        return day;
    }

    /**
     * @return the hour
     */
    public int getHour() {
        return hour;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DayOfWeek) {
            DayOfWeek other = (DayOfWeek) obj;
            return other.getDay() == day && other.getHour() == hour;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + this.day;
        hash = 29 * hash + this.hour;
        return hash;
    }
}
