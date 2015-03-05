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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.TimePeriod;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.EnergyUsagePrediction;
import java.util.Collection;

/**
 * This predictor automatically selects between a polynomial or linear
 * predictor, based upon the CPU value only. It uses sum of the root mean square
 * error to determine which option is best.
 *
 * @author Richard Kavanagh
 */
public class CPUOnlyBestFitEnergyPredictor extends AbstractEnergyPredictor {

    private final CpuOnlyEnergyPredictor linear = new CpuOnlyEnergyPredictor();
    private final CpuOnlyPolynomialEnergyPredictor polynomial = new CpuOnlyPolynomialEnergyPredictor();

    @Override
    public EnergyUsagePrediction getHostPredictedEnergy(Host host, Collection<VM> virtualMachines, TimePeriod timePeriod) {
        if (linear.getRootMeanSquareError(host) <= polynomial.getRootMeanSquareError(host)) {
            return linear.getHostPredictedEnergy(host, virtualMachines, timePeriod);
        } else {
            return polynomial.getHostPredictedEnergy(host, virtualMachines, timePeriod);
        }
    }

    @Override
    public EnergyUsagePrediction getVMPredictedEnergy(VM vm, Collection<VM> virtualMachines, Host host, TimePeriod timePeriod) {
        if (linear.getRootMeanSquareError(host) <= polynomial.getRootMeanSquareError(host)) {
            return linear.getVMPredictedEnergy(vm, virtualMachines, host, timePeriod);
        } else {
            return polynomial.getVMPredictedEnergy(vm, virtualMachines, host, timePeriod);
        }
    }

    @Override
    public double getSumOfSquareError(Host host) {
        double lin = linear.getSumOfSquareError(host);
        double poly = polynomial.getSumOfSquareError(host);
        return (lin <= poly ? lin : poly);
    }

    @Override
    public double getRootMeanSquareError(Host host) {
        double lin = linear.getRootMeanSquareError(host);
        double poly = polynomial.getRootMeanSquareError(host);
        return (lin <= poly ? lin : poly);
    }

}
