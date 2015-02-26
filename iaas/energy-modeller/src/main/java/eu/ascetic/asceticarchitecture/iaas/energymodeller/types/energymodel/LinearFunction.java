/**
 * Copyright 2015 University of Leeds
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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energymodel;

import org.apache.commons.math3.analysis.UnivariateFunction;

/**
 * This is to be used for energy models that are a univariate linear function.
 * i.e. that follow y = mx + c.
 *
 * @author Richard Kavanagh
 */
public class LinearFunction implements UnivariateFunction {

    double coefficient = 0.0;
    double intercept = 0.0;
    
    /**
     * This creates a new linear function that follows the equation y = mx + c.
     */
    public LinearFunction() {
    }

    /**
     * This creates a new linear function that follows the equation y = mx + c.
     * @param coefficient The gradient of the line
     * @param intercept The intercept
     */
    public LinearFunction(double coefficient, double intercept) {
        this.coefficient = coefficient;
        this.intercept = intercept;
    }

    /**
     * This gets the gradient of the line.
     * @return The gradient of the line.
     */
    public double getCoefficient() {
        return coefficient;
    }

    /**
     * This sets the gradient of the line.
     * @param coefficient The gradient of the line.
     */
    public void setCoefficient(double coefficient) {
        this.coefficient = coefficient;
    }

    /**
     * This gets the intercept of the line.
     * @return The intercept of the line.
     */
    public double getIntercept() {
        return intercept;
    }

    /**
     * This sets the intercept of the line.
     * @param intercept The intercept to set.
     */
    public void setIntercept(double intercept) {
        this.intercept = intercept;
    }

    @Override
    public double value(double usageCPU) {
        return coefficient * usageCPU + intercept;
    }

}
