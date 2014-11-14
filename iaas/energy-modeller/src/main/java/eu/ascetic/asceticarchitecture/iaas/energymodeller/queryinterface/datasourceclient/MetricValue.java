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

/**
 * This class records the value of a specific metric. It is aimed at separating
 * out the measurement from the origin of the value, i.e. Zabbix/Ganglia etc
 *
 * @author Richard
 */
public class MetricValue {

    /**
     * The name.
     */
    private String name;
    /**
     * The key.
     */
    private String key;

    /**
     * The delay.
     */
    private String delay;

    /**
     * The value that was taken for the metric.
     */
    private String value;
    /**
     * The time.
     */
    private long clock;

    /**
     * Instantiates a new metric value.
     *
     * @param name the name of the metric
     */
    public MetricValue(String name) {
        this.name = name;
    }

    /**
     * Instantiates a new metric value.
     *
     * @param name The name of the metric (human readable)
     * @param key The key used to identify the metric
     * @param value The value the metric holds
     * @param clock The time the value was taken.
     */
    public MetricValue(String name, String key, String value, long clock) {
        this.name = name;
        this.key = key;
        this.value = value;
        this.clock = clock;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the key.
     *
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the key.
     *
     * @param key the new key
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Gets the delay.
     *
     * @return the delay
     */
    public String getDelay() {
        return delay;
    }

    /**
     * Sets the delay.
     *
     * @param delay the new delay
     */
    public void setDelay(String delay) {
        this.delay = delay;
    }

    /**
     * Gets the last value.
     *
     * @return the last value
     */
    public String getValueAsString() {
        return value;
    }

    /**
     * Gets the last value.
     *
     * @return the last value
     */
    public double getValue() {
        return Double.parseDouble(value);
    }

    /**
     * Sets the last value.
     *
     * @param value the new last value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the last clock. The value is given in Unix time.
     *
     * @return the last clock
     */
    public long getClock() {
        return clock;
    }

    /**
     * Sets the last clock. The value is given in Unix time.
     *
     * @param clock the new last clock
     */
    public void setClock(long clock) {
        this.clock = clock;
    }

}
