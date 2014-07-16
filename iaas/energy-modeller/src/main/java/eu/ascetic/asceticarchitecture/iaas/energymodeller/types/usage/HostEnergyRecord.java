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
 * TODO consider if this is needed? Should the query just take the data and have
 * it prepared from the database without getting raw data?
 *
 * @author Richard
 */
public class HostEnergyRecord implements Comparable<HostEnergyRecord> {

    private final Host host;
    private final long time;
    private final double power;
    private final double energy;

    public HostEnergyRecord(Host host, long time, double power, double energy) {
        this.host = host;
        this.time = time;
        this.power = power;
        this.energy = energy;
    }

    public Host getHost() {
        return host;
    }

    public double getEnergy() {
        return energy;
    }

    public double getPower() {
        return power;
    }

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

    @Override
    public int compareTo(HostEnergyRecord o) {
        return new Long(this.time).compareTo(o.getTime());
    }

}
