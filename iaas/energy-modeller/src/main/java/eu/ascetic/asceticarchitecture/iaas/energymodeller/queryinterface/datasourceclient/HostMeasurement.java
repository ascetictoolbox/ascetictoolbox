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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.monitoring.api.datamodel.Item;
import java.util.Collection;
import java.util.HashMap;

/**
 * This represents a single snapshot of the data from a data source.
 *
 * @author Richard
 */
public class HostMeasurement {

    private Host host;
    private long clock;
    private HashMap<String, Item> metrics = new HashMap<>();

    /**
     * This creates a host measurement.
     * @param host The host the measurement is for
     */
    public HostMeasurement(Host host) {
        this.host = host;
    }

    /**
     * This creates a host measurement.
     * @param host The host the measurement is for
     * @param clock The time when the measurement was taken
     */
    public HostMeasurement(Host host, long clock) {
        this.host = host;
        this.clock = clock;
    }

    /**
     * This looks at the metrics gained, for this given gathering of measurement
     * values and compares the earliest and latest timestamps to determine how 
     * long it took to get all the data values.
     * @return 
     */
    public long getMaximumClockDifference() {
        long lowest = Integer.MAX_VALUE;
        long highest = Integer.MIN_VALUE;
        for (Item entry : metrics.values()) {
            long current = entry.getLastClock();
            if (current < lowest) {
                lowest = current;
            }
            if (current > highest) {
                highest = current;
            }
        }
        return highest - lowest;
    }

    /**
     * This returns the maximum delay that any metric encountered.
     * @return 
     */
    public long getMaxDelay() {
        long delay = Integer.MIN_VALUE;
        for (Item entry : metrics.values()) {
            long current = Long.parseLong(entry.getDelay());
            if (current > delay) {
                delay = current;
            }
        }
        return delay;
    }      
    
    /**
     * This returns the minimum delay that any metric encountered.
     * @return 
     */
    public long getMinDelay() {
        long delay = Integer.MAX_VALUE;
        for (Item entry : metrics.values()) {
            long current = Long.parseLong(entry.getDelay());
            if (current < delay) {
                delay = current;
            }
        }
        return delay;
    }    

    /**
     * @return the host
     */
    public Host getHost() {
        return host;
    }

    /**
     * @param host the host to set
     */
    public void setHost(Host host) {
        this.host = host;
    }

    /**
     * @return the metrics
     */
    public HashMap<String, Item> getMetrics() {
        return metrics;
    }
    
    /**
     * This returns the set of items for the host measurement.
     * @return 
     */
    public Collection<Item> getItems() {
        return metrics.values();
    }

    /**
     * @param metrics the metrics to set
     */
    public void setMetrics(HashMap<String, Item> metrics) {
        this.metrics = metrics;
    }

    public void addMetric(Item item) {
        metrics.put(item.getKey(), item);
    }

    public Item getMetric(String key) {
        return metrics.get(key);
    }

    @Override
    public String toString() {
        return host.toString() + " Time: " + clock +  " Metric Count: " + metrics.size() + " Clock Diff: " + getMaximumClockDifference();
    }

    /**
     * @return the clock
     */
    public long getClock() {
        return clock;
    }

    /**
     * @param clock the clock to set
     */
    public void setClock(long clock) {
        this.clock = clock;
    }
    
    
    
    
}
