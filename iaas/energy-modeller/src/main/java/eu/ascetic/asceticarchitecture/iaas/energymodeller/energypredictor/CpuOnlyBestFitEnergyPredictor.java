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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * This predictor automatically selects between a polynomial or linear
 * predictor, based upon the CPU value only. It uses sum of the root mean square
 * error to determine which option is best.
 *
 * @author Richard Kavanagh
 */
public class CpuOnlyBestFitEnergyPredictor extends AbstractEnergyPredictor {

    private final CpuOnlyEnergyPredictor linear = new CpuOnlyEnergyPredictor();
    private final CpuOnlyPolynomialEnergyPredictor polynomial = new CpuOnlyPolynomialEnergyPredictor();
    private final CpuOnlySplinePolynomialEnergyPredictor splinePolynomial = new CpuOnlySplinePolynomialEnergyPredictor();
    private final ArrayList<EnergyPredictorInterface>predictors = new ArrayList<>();
    private final HashMap<Host, EnergyPredictorInterface> predictorMap = new HashMap<>();
    
    /**
     * This creates a new CPU Only Best fit energy predictor
     */
    public CpuOnlyBestFitEnergyPredictor() {
        super();
        predictors.add(linear);
        predictors.add(polynomial);
        predictors.add(splinePolynomial);
    }
    
    @Override
    public EnergyUsagePrediction getHostPredictedEnergy(Host host, Collection<VM> virtualMachines, TimePeriod timePeriod) {
        return getBestFit(host).getHostPredictedEnergy(host, virtualMachines, timePeriod);
    }

    @Override
    public EnergyUsagePrediction getVMPredictedEnergy(VM vm, Collection<VM> virtualMachines, Host host, TimePeriod timePeriod) {
            return getBestFit(host).getVMPredictedEnergy(vm, virtualMachines, host, timePeriod);
    }
    
    @Override
    public double predictPowerUsed(Host host) {
        return getBestFit(host).predictPowerUsed(host);
    }

    @Override
    public double predictPowerUsed(Host host, double usageCPU) {
        return getBestFit(host).predictPowerUsed(host, usageCPU);
    }

    @Override
    public double getSumOfSquareError(Host host) {
        return getBestFit(host).getSumOfSquareError(host);
    }

    /**
     * This takes the list of predictors and finds the best predictor for the given
     * host.
     * @param host The host to get the best predictor for
     * @return The best predictor for the host specified.
     */
    public EnergyPredictorInterface getBestFit(Host host) {
        EnergyPredictorInterface answer = predictorMap.get(host);
        if (answer == null) {
            answer = getBestPredictor(host, predictors);
            predictorMap.put(host, answer);
        }
        return answer;
    }

    @Override
    public double getRootMeanSquareError(Host host) {
        return getBestFit(host).getRootMeanSquareError(host);
    }
    
    /**
     * This outputs information about how good a fit is provided by each of the
     * predictors in use.
     * @param host The host to check the fit for.
     */
    @Override
    public void printFitInformation(Host host) {
        System.out.println("Using the " + AbstractEnergyPredictor.getBestPredictor(host, predictors).toString());
        System.out.println("Linear - SSE: " + linear.getSumOfSquareError(host) + " RMSE: " + linear.getRootMeanSquareError(host));
        System.out.println("Polynomial - SSE: " + polynomial.getSumOfSquareError(host) + " RMSE: " + polynomial.getRootMeanSquareError(host));
        System.out.println("Polynomial Spline - SSE: " + splinePolynomial.getSumOfSquareError(host) + " RMSE: " + splinePolynomial.getRootMeanSquareError(host));
    }
    
    @Override
    public String toString() {
        return "CPU only best fit energy predictor";
    }    

}
