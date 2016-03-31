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
import java.util.Objects;

/**
 * This class stores the running average information and enables its processing.
 */
public class RunningAverage implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String property;
    private double total;
    private double count;

    /**
     *
     * @param property
     * @param total
     * @param count
     */
    public RunningAverage(String property, double total, int count) {
        this.property = property;
        this.total = total;
        this.count = (double) count;
    }

    /**
     *
     * @param property
     * @param intialValue
     */
    public RunningAverage(String property, double intialValue) {
        count = 1;
        total = intialValue;
        this.property = property;
    }

    /**
     * This returns the app tag or disk reference information.
     *
     * @return
     */
    public String getProperty() {
        return property;
    }

    /**
     *
     * @param value
     */
    public void add(double value) {
        count = count + 1;
        total = total + value;
    }

    /**
     *
     * @return
     */
    public double getAverage() {
        return total / count;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass().equals(this.getClass())) {
            return ((RunningAverage) obj).property.equals(this.property);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + Objects.hashCode(this.property);
        return hash;
    }

}
