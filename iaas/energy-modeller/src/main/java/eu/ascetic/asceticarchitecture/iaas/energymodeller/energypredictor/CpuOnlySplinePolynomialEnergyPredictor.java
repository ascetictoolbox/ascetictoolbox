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

import eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.vmenergyshare.EnergyDivision;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.TimePeriod;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.usage.HostEnergyCalibrationData;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.EnergyUsagePrediction;
import eu.ascetic.ioutils.caching.LRUCache;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;
import org.apache.commons.math3.analysis.interpolation.LoessInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

/**
 * This implements the CPU only spline polynomial energy predictor for the
 * ASCETiC project.
 *
 * It performs polynomial fitting over multiple spline points in order to
 * determine from the CPU load the current power consumption.
 *
 * i.e. It provides a piecewise function defined by a collection of polynomial
 * functions
 *
 * @author Richard Kavanagh
 *
 */
public class CpuOnlySplinePolynomialEnergyPredictor extends AbstractEnergyPredictor {

    private final LRUCache<Host, PredictorFunction<PolynomialSplineFunction>> modelCache = new LRUCache<>(5, 50);

    /**
     * This creates a new CPU only energy predictor that uses a polynomial fit.
     *
     * It will create a energy-modeller-predictor properties file if it doesn't
     * exist.
     *
     * The main property: iaas.energy.modeller.cpu.energy.predictor.default_load
     * should be in the range 0..1 or -1. This indicates the predictor's default
     * assumption on how much load is been induced. -1 measures the CPU's
     * current load and uses that to forecast into the future.
     *
     * In the case of using -1 as a parameter to additional parameters are used:
     * iaas.energy.modeller.cpu.energy.predictor.utilisation.observe_time.sec
     * iaas.energy.modeller.cpu.energy.predictor.utilisation.observe_time.min
     *
     * These indicate the window of how long the CPU should be monitored for, to
     * determine the current load.
     */
    public CpuOnlySplinePolynomialEnergyPredictor() {
        super();
    }

    @Override
    public EnergyUsagePrediction getHostPredictedEnergy(Host host, Collection<VM> virtualMachines, TimePeriod duration) {
        EnergyUsagePrediction wattsUsed;
        if (getDefaultAssumedCpuUsage() == -1) {
            wattsUsed = predictTotalEnergy(host, getCpuUtilisation(host, virtualMachines), duration);
        } else {
            wattsUsed = predictTotalEnergy(host, getDefaultAssumedCpuUsage(), duration);
        }
        return wattsUsed;
    }

    /**
     * This provides a prediction of how much energy is to be used by a VM
     *
     * @param vm The vm to be deployed
     * @param virtualMachines The virtual machines giving a workload on the host
     * machine
     * @param host The host that the VMs will be running on
     * @param timePeriod The time period the query should run for.
     * @return The prediction of the energy to be used.
     */
    @Override
    public EnergyUsagePrediction getVMPredictedEnergy(VM vm, Collection<VM> virtualMachines, Host host, TimePeriod timePeriod) {
        EnergyDivision division = getEnergyUsage(host, virtualMachines);
        EnergyUsagePrediction hostAnswer;
        if (getDefaultAssumedCpuUsage() == -1) {
            hostAnswer = predictTotalEnergy(host, getCpuUtilisation(host, virtualMachines), timePeriod);
        } else {
            hostAnswer = predictTotalEnergy(host, getDefaultAssumedCpuUsage(), timePeriod);
        }
        hostAnswer.setAvgPowerUsed(hostAnswer.getTotalEnergyUsed()
                / ((double) TimeUnit.SECONDS.toHours(timePeriod.getDuration())));
        EnergyUsagePrediction generalHostsAnswer = getGeneralHostPredictedEnergy(timePeriod);
        double generalPower = generalHostsAnswer.getAvgPowerUsed() / (double) virtualMachines.size();
        double generalEnergy = generalHostsAnswer.getTotalEnergyUsed() / (double) virtualMachines.size();        
        EnergyUsagePrediction answer = new EnergyUsagePrediction(vm);
        answer.setDuration(hostAnswer.getDuration());
        //Find the fraction to be associated with the VM
        double vmsEnergyFraction = division.getEnergyUsage(hostAnswer.getTotalEnergyUsed(), vm);
        division.setConsiderIdleEnergy(isConsiderIdleEnergy());
        answer.setTotalEnergyUsed(vmsEnergyFraction + generalEnergy);
        double vmsPowerFraction = division.getEnergyUsage(hostAnswer.getAvgPowerUsed(), vm);
        answer.setAvgPowerUsed(vmsPowerFraction + generalPower);
        return answer;
    }

    /**
     * This predicts the total amount of energy used by a host.
     *
     * @param host The host to get the energy prediction for
     * @param usageCPU The amount of CPU load placed on the host
     * @param timePeriod The time period the prediction is for
     * @return The predicted energy usage.
     */
    public EnergyUsagePrediction predictTotalEnergy(Host host, double usageCPU, TimePeriod timePeriod) {
        EnergyUsagePrediction answer = new EnergyUsagePrediction(host);
        PolynomialSplineFunction model = retrieveModel(host).getFunction();
        double powerUsed = model.value(getCpuUsageValue(model, getCpuUsageValue(model, usageCPU)));
        answer.setAvgPowerUsed(powerUsed);
        answer.setTotalEnergyUsed(powerUsed * ((double) TimeUnit.SECONDS.toHours(timePeriod.getDuration())));
        answer.setDuration(timePeriod);
        return answer;
    }

    /**
     * This estimates the power used by a host, given its CPU load. The CPU load
     * value is determined from the settings file.
     *
     * @param host The host to get the energy prediction for.
     * @return The predicted power usage.
     */
    @Override
    public double predictPowerUsed(Host host) {
        if (getDefaultAssumedCpuUsage() == -1) {
            return predictPowerUsed(host, getCpuUtilisation(host));
        } else {
            return predictPowerUsed(host, getDefaultAssumedCpuUsage());
        }
    }

    /**
     * This estimates the power used by a host, given its CPU load.
     *
     * @param host The host to get the energy prediction for
     * @param usageCPU The amount of CPU load placed on the host
     * @return The predicted power usage.
     */
    @Override
    public double predictPowerUsed(Host host, double usageCPU) {
        PolynomialSplineFunction model = retrieveModel(host).getFunction();
        return model.value(getCpuUsageValue(model, usageCPU));
    }

    /**
     * This calculates the mathematical function that predicts the power
     * consumption given the cpu utilisation.
     *
     * @param host The host to get the function for
     * @return The mathematical function that predicts the power consumption
     * given the cpu utilisation.
     */
    private PredictorFunction<PolynomialSplineFunction> retrieveModel(Host host) {
        PredictorFunction<PolynomialSplineFunction> answer;
        if (modelCache.containsKey(host)) {
            /**
             * A small cache avoids recalculating the regression so often.
             */
            return modelCache.get(host);
        }
        ArrayList<HostEnergyCalibrationData> dataSet = cleanData(host.getCalibrationData());
        double[] xval = new double[dataSet.size()];
        double[] yval = new double[dataSet.size()];
        int i = 0;
        for (HostEnergyCalibrationData data : dataSet) {
            xval[i] = data.getCpuUsage();
            yval[i] = data.getWattsUsed();
            i++;
        }
        LoessInterpolator fitter = new LoessInterpolator();
        PolynomialSplineFunction function = fitter.interpolate(xval, yval);
        double sse = getSumOfSquareError(function, xval, yval);
        double rmse = getRootMeanSquareError(sse, xval.length);
        answer = new PredictorFunction<>(function, sse, rmse);
        modelCache.put(host, answer);
        return answer;
    }
    
    /**
     * This ensures the CPU usage provided to the model is in the acceptable 
     * range.
     * @param model The model to get the cpu usage for
     * @param usageCPU The amount of CPU load placed on the host
     * @return The cpu usage that is within the acceptable range.
     */
    private double getCpuUsageValue(PolynomialSplineFunction model, double usageCPU) {
        /**
         * Interpolation is the process of fitting a line of best fit directly
         * to the datapoints gathered. The lowest value possible value to predict 
         * from is therefore not likely to be 0.
         */
        if (usageCPU < model.getKnots()[0]) {
            return model.getKnots()[0];
        }
        if (usageCPU > model.getKnots()[model.getKnots().length - 1]) {
            return model.getKnots()[model.getKnots().length - 1];
        }        
        return usageCPU;
    }

    /**
     * This orders Host calibration data by its CPU Utilisation.
     */
    public class CpuOrder implements Comparator<HostEnergyCalibrationData> {

        @Override
        public int compare(HostEnergyCalibrationData o1, HostEnergyCalibrationData o2) {
            return Double.compare(o1.getCpuUsage(), o2.getCpuUsage());
        }

    }

    /**
     * This creates a list of strictly incrementing data points.
     *
     * @param data The data to clean
     * @return The cleaned dataset
     */
    private ArrayList<HostEnergyCalibrationData> cleanData(ArrayList<HostEnergyCalibrationData> data) {
        ArrayList<HostEnergyCalibrationData> answer = new ArrayList<>();
        Collections.sort(data, new CpuOnlySplinePolynomialEnergyPredictor.CpuOrder());
        HostEnergyCalibrationData previous = null;
        for (HostEnergyCalibrationData current : data) {
            if (previous == null || (current.getCpuUsage() > previous.getCpuUsage()) && current.getCpuUsage() != 1.0) {
                answer.add(current);
            }
            previous = current;
        }
        return answer;
    }

    /**
     * This performs a calculation to determine how close the fit is for a given
     * model.
     *
     * @param function The PolynomialFunction to assess
     * @param observed The actual set of observed points
     * @return The sum of the square error.
     */
    private double getSumOfSquareError(PolynomialSplineFunction function, double[] xObserved, double[] yObserved) {
        double answer = 0;
        for (int i = 0; i < xObserved.length; i++) {
            double error = yObserved[i] - function.value(xObserved[i]);
            answer = answer + (error * error);
        }
        return answer;
    }

    /**
     * This calculates the root means square error
     *
     * @param sse The sum of the square error
     * @param count The count of observed points
     * @return the root means square error
     */
    private double getRootMeanSquareError(double sse, int count) {
        return Math.sqrt(sse / ((double) count));
    }

    @Override
    public double getSumOfSquareError(Host host) {
        return retrieveModel(host).getSumOfSquareError();
    }

    @Override
    public double getRootMeanSquareError(Host host) {
        return retrieveModel(host).getRootMeanSquareError();
    } 

    @Override
    public String toString() {
        return "CPU only spline polynomial energy predictor";
    }

    @Override
    public void printFitInformation(Host host) {
        System.out.println(this.toString() + " - SSE: " + 
                this.retrieveModel(host).getSumOfSquareError() + 
                " RMSE: " + this.retrieveModel(host).getRootMeanSquareError());
    }       
    
}
