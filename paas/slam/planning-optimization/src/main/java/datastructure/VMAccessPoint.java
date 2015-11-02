/**
 *  Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */



package datastructure;

/**
 * The general QoS (availability) for VM access point.
 * 
 * @author Kuan Lu
 */
public class VMAccessPoint {
    /**
     * Availability
     */
    private double availability;

    /**
     * Gets availability.
     */
    public double getAvailability() {
        return availability;
    }

    /**
     * Sets availability.
     */
    public void setAvailability(double availability) {
        this.availability = availability;
    }
}
