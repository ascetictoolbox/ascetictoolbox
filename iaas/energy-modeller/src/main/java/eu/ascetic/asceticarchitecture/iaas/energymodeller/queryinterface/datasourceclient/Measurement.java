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

    /**
     * This looks at the metrics gained, for this given gathering of measurement
     * values and compares the earliest and latest timestamps to determine how
     * long it took to get all the data values.
     *
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
     *
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
     *
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
     * This lists a set of names for the metrics that are available in a host
     * measurement
     *
     * @return
     */
    public Set<String> getMetricNameList() {
        return metrics.keySet();
    }

    /**
     * This lists the metrics that are available in a host measurement
     *
     * @return the metrics
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
     * This returns the set of items for the host measurement.
     *
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

    /**
     * This adds a metric and value to a host measurement
     *
     * @param item
     */
    public void addMetric(Item item) {
        metrics.put(item.getKey(), item);
    }

    /**
     * This gets the item that represents a given metric
     *
     * @param key
     * @return
     */
    public Item getMetric(String key) {
        return metrics.get(key);
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
        return interrupt + iowait + nice + softirq + steal;
    }

    /**
     * This provides rapid access to cpu load values from a vm measurement.
     *
     * @return The cpu load when the measurement was taken.
     */
    public double getCpuIdle() {
        return Double.parseDouble(this.getMetric(IDLE_KPI_NAME).getLastValue());
    }

}
