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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import java.util.Objects;

/**
 * This is the object representation of a record held in the database regarding
 * a hosts energy usage.
 *
 * @author Richard
 */
public class HostEnergyRecord implements Comparable<HostEnergyRecord> {

    private final Host host;
    private final long time;
    private final double power;
    private final double energy;

    /**
     * This creates a new host energy record.
     * @param host The host the record is for
     * @param time The time when the measurement was taken.
     * @param power The power that was being consumed at the time the measurement was taken.
     * @param energy The energy used from an arbritrary single point in the past.
     */
    public HostEnergyRecord(Host host, long time, double power, double energy) {
        this.host = host;
        this.time = time;
        this.power = power;
        this.energy = energy;
    }

    /**
     * This returns the host that this record is for.
     * @return The host that used the energy.
     */
    public Host getHost() {
        return host;
    }

    /**
     * The value for the energy used at the time the record represents.
     * @return The energy used from an arbritrary point of time in the past.
     */
    public double getEnergy() {
        return energy;
    }

    /**
     * The power recorded at the time of the host energy record.
     * @return The power measured to be in use at the specified time.
     */
    public double getPower() {
        return power;
    }

    /**
     * This returns the time that the energy record represents.
     * @return The time that the energy record represents
     */
    public long getTime() {
        return time;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof HostEnergyRecord) {
            HostEnergyRecord compareTo = (HostEnergyRecord) obj;
            return ((time == compareTo.getTime()) && host.equals(compareTo.getHost()));
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.host);
        hash = 59 * hash + (int) (this.time ^ (this.time >>> 32));
        return hash;
    }

    /**
     * This comparison orders energy records by time.
     *
     * @param energyRecord the energy record to compare to.
     * @return -1 if the before, 0 if at the same time 1 if in the future.
     */
    @Override
    public int compareTo(HostEnergyRecord energyRecord) {
        return Long.valueOf(this.time).compareTo(energyRecord.getTime());
    }

}
