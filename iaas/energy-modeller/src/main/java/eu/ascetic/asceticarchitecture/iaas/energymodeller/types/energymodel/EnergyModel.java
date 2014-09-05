/**
 * Copyright 2014 Athens University of Economics and Business
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

public class EnergyModel {

    double coefficientCPU = 0.0;
    double coefficientRAM = 0.0;
    double intercept = 0.0;

    public EnergyModel() {

    }

    public EnergyModel(double coefficientCPU, double coefficientRAM, double intercept) {
        this.coefficientCPU = coefficientCPU;
        this.coefficientRAM = coefficientRAM;
        this.intercept = intercept;
    }

    public double getCoefCPU() {
        return coefficientCPU;
    }

    public double getCoefRAM() {
        return coefficientRAM;
    }

    public double getIntercept() {
        return intercept;
    }

    public void setCoefCPU(double coefficientCPU) {
        this.coefficientCPU = coefficientCPU;
    }

    public void setCoefRAM(double coefficientRAM) {
        this.coefficientRAM = coefficientRAM;
    }

    public void setIntercept(double intercept) {
        this.intercept = intercept;
    }

}
