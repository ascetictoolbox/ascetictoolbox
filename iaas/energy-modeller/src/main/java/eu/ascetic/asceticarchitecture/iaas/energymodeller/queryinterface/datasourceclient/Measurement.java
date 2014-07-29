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

import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.Item;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

/**
 * This is the base class for all measurements, either for a vm instance or a
 * host physical machine.
 *
 * @author Richard
 */
public abstract class Measurement {

    private long clock;
    private HashMap<String, Item> metrics = new HashMap<>();
    protected static final String POWER_KPI_NAME = KpiList.POWER_KPI_NAME;
    protected static final String ENERGY_KPI_NAME = KpiList.ENERGY_KPI_NAME;

    private static final String IDLE_KPI_NAME = KpiList.IDLE_KPI_NAME;
    private static final String INTERUPT_KPI_NAME = KpiList.INTERUPT_KPI_NAME;
    private static final String IO_WAIT_KPI_NAME = KpiList.IO_WAIT_KPI_NAME;
    private static final String NICE_KPI_NAME = KpiList.NICE_KPI_NAME;
    private static final String SOFT_IRQ_KPI_NAME = KpiList.SOFT_IRQ_KPI_NAME;
    private static final String STEAL_KPI_NAME = KpiList.STEAL_KPI_NAME;
    private static final String SYSTEM_KPI_NAME = KpiList.SYSTEM_KPI_NAME;
    private static final String USER_KPI_NAME = KpiList.USER_KPI_NAME;
    private static final String MEMORY_AVAILABLE_KPI_NAME = KpiList.MEMORY_AVAILABLE_KPI_NAME;     
    private static final String MEMORY_TOTAL_KPI_NAME = KpiList.MEMORY_TOTAL_KPI_NAME;

    /**
     * This looks at the metrics gained, for this given gathering of measurement
     * values and compares the earliest and latest timestamps to determine how
     * long it took to get all the data values.
     *
     * @return The difference between the oldest piece of data recorded and the
     * newest.
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
     *
     * @return The highest delay encountered by any metric, in this set of
     * measurement data.
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
     *
     * @return The lowest delay encountered by any metric, in this set of
     * measurement data.
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
     * This lists a set of names for the metrics that are available in this
     * measurement
     *
     * @return The list of all metrics names that were measured.
     */
    public Set<String> getMetricNameList() {
        return metrics.keySet();
    }

    /**
     * This lists the metrics that are available in this measurement
     *
     * @return the list of all metrics and the key value that is used to quickly
     * identify a metric.
     */
    public HashMap<String, Item> getMetrics() {
        return metrics;
    }

    /**
     * This gets the count of how many values for different metrics are stored.
     *
     * @return The count of how many values for different metrics are stored.
     */
    public int getMetricCount() {
        return metrics.size();
    }

    /**
     * This returns the set of items/metric values for the measurement.
     *
     * @return The metric values for this measurement.
     */
    public Collection<Item> getItems() {
        return metrics.values();
    }

    /**
     * This sets the set of metrics and their keys for the measurement.
     *
     * @param metrics the metrics to set
     */
    public void setMetrics(HashMap<String, Item> metrics) {
        this.metrics = metrics;
    }

    /**
     * This adds a metric and value to a measurement
     *
     * @param item a metric to add to this measurement dataset
     */
    public void addMetric(Item item) {
        metrics.put(item.getKey(), item);
    }

    /**
     * This gets the item that represents a given metric
     *
     * @param key The key that is used to identify a given measurement
     * @return The metric and its value that is identified by the key.
     */
    public Item getMetric(String key) {
        return metrics.get(key);
    }

    /**
     * This provides the time of the measurement in Unix time.
     *
     * @return The time the measurement was taken
     */
    public long getClock() {
        return clock;
    }

    /**
     * This sets the time of the measurement in Unix time.
     *
     * @param clock The time the measurement was taken
     */
    public void setClock(long clock) {
        this.clock = clock;
    }

    /**
     * This provides rapid access to cpu load values from a vm measurement.
     *
     * @return The cpu load when the measurement was taken.
     */
    public double getCpuLoad() {
        double interrupt = 0.0;
        double iowait = 0.0;
        double nice = 0.0;
        double softirq = 0.0;
        double steal = 0.0;
        double system = 0.0;
        double user = 0.0;
        if (metrics.containsKey(SYSTEM_KPI_NAME)) {
            system = Double.parseDouble(this.getMetric(SYSTEM_KPI_NAME).getLastValue());
        }
        if (metrics.containsKey(USER_KPI_NAME)) {
            user = Double.parseDouble(this.getMetric(USER_KPI_NAME).getLastValue());
        }
        if (metrics.containsKey(INTERUPT_KPI_NAME)) {
            interrupt = Double.parseDouble(this.getMetric(INTERUPT_KPI_NAME).getLastValue());
        }
        if (metrics.containsKey(IO_WAIT_KPI_NAME)) {
            iowait = Double.parseDouble(this.getMetric(IO_WAIT_KPI_NAME).getLastValue());
        }
        if (metrics.containsKey(NICE_KPI_NAME)) {
            nice = Double.parseDouble(this.getMetric(NICE_KPI_NAME).getLastValue());
        }
        if (metrics.containsKey(SOFT_IRQ_KPI_NAME)) {
            softirq = Double.parseDouble(this.getMetric(SOFT_IRQ_KPI_NAME).getLastValue());
        }
        if (metrics.containsKey(STEAL_KPI_NAME)) {
            steal = Double.parseDouble(this.getMetric(STEAL_KPI_NAME).getLastValue());
        }
        return system + user + interrupt + iowait + nice + softirq + steal;
    }

    /**
     * This provides rapid access to cpu load values from a vm measurement.
     *
     * @return The cpu load when the measurement was taken.
     */
    public double getCpuIdle() {
        return Double.parseDouble(this.getMetric(IDLE_KPI_NAME).getLastValue());
    }

    /**
     * This provides rapid access to memory values for a measurement.
     *
     * @return The total memory available when the measurement was taken.
     */
    public double getMemoryAvailable() {
        return Double.parseDouble(this.getMetric(MEMORY_AVAILABLE_KPI_NAME).getLastValue());
    } 
    
    /**
     * This provides rapid access to memory values for a measurement.
     *
     * @return The total memory used when the measurement was taken.
     */
    public double getMemoryUsed() {
        return getMemoryTotal() - getMemoryAvailable();
    }        
    
    /**
     * This provides rapid access to memory values for a measurement.
     *
     * @return The total memory available when the measurement was taken.
     */
    public double getMemoryTotal() {
        return Double.parseDouble(this.getMetric(MEMORY_TOTAL_KPI_NAME).getLastValue());
    }

}
