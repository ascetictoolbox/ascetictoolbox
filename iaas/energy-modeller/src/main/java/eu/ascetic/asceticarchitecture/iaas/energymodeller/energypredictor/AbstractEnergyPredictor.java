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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.vmenergyshare.DefaultEnergyShareRule;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.vmenergyshare.EnergyDivision;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.vmenergyshare.EnergyShareRule;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.TimePeriod;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.EnergyUsagePrediction;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This implements the default and utility functions for an energy predictor. It
 * is expected that any energy predictor loaded into the ASCETiC architecture,
 * will override this class.
 *
 * @author Richard Kavanagh
 */
public abstract class AbstractEnergyPredictor implements EnergyPredictorInterface {

    private EnergyShareRule energyShareRule = new DefaultEnergyShareRule();
    private static final String DEFAULT_ENERGY_SHARE_RULE_PACKAGE
            = "eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.vmenergyshare";

    /**
     * This uses the current energy share rule for the energy predictor
     * allowing for the translation between host energy usage and VMs energy usage.
     * @param host The host to analyse
     * @param vms The VMs that are on/to be on the host
     * @return The fraction of energy or used per host.
     */
    public EnergyDivision getEnergyUsage(Host host, Collection<VM> vms) {
        return energyShareRule.getEnergyUsage(host, vms);
    }

    /**
     * This returns the current energy share rule that is in use by the 
     * energy predictor.
     * @return the energyShareRule The rule that divides the energy usage of hosts
     * into each VM.
     */
    public EnergyShareRule getEnergyShareRule() {
        return energyShareRule;
    }

    /**
     * This sets the current energy share rule that is in use by the 
     * energy predictor.
     * @param energyShareRule The rule that divides the energy usage of hosts
     * into each VM.
     */
    public void setEnergyShareRule(EnergyShareRule energyShareRule) {
        this.energyShareRule = energyShareRule;
    }

    /**
     * This sets the current energy share rule that is in use by the energy
     * predictor.
     *
     * @param energyShareRule The rule that divides the energy usage of hosts
     * into each VM.
     */
    public void setEnergyShareRule(String energyShareRule) {
        try {
            if (!energyShareRule.startsWith(DEFAULT_ENERGY_SHARE_RULE_PACKAGE)) {
                energyShareRule = DEFAULT_ENERGY_SHARE_RULE_PACKAGE + "." + energyShareRule;
            }
            this.energyShareRule = (EnergyShareRule) (Class.forName(energyShareRule).newInstance());
        } catch (IllegalAccessException | InstantiationException | ClassNotFoundException ex) {
            if (energyShareRule == null) {
                this.energyShareRule = new DefaultEnergyShareRule();
            }
            Logger.getLogger(AbstractEnergyPredictor.class.getName()).log(Level.WARNING, "The energy share rule specified was not found", ex);
        }
    }
    
    /**
     * This provides a prediction of how much energy is to be used by a VM,
     * over the next hour.
     *
     * @param vm The vm to be deployed
     * @param virtualMachines The virtual machines giving a workload on the host
     * machine
     * @param host The host that the VMs will be running on
     * @return The prediction of the energy to be used.
     */
    @Override
    public EnergyUsagePrediction getVMPredictedEnergy(VM vm, Collection<VM> virtualMachines, Host host) {
        TimePeriod duration = new TimePeriod(new GregorianCalendar(), TimeUnit.HOURS.toSeconds(1));
        return getVMPredictedEnergy(vm, virtualMachines, host, duration);
    }
    
    /**
     * This provides a prediction of how much energy is to be used by a host
     * in the next hour.
     *
     * @param host The host to get the energy prediction for
     * @param virtualMachines The virtual machines giving a workload on the host
     * machine
     * @return The prediction of the energy to be used.
     */
    @Override
    public EnergyUsagePrediction getHostPredictedEnergy(Host host, Collection<VM> virtualMachines) {
        TimePeriod duration = new TimePeriod(new GregorianCalendar(), 1, TimeUnit.HOURS);
        return getHostPredictedEnergy(host, virtualMachines, duration);
    }      

    /**
     * This for a set of VMs provides the amount of memory allocated in Mb.
     * @param virtualMachines The VMs to get the memory used.
     * @return The amount of memory allocated to VMs in Mb.
     */
    public static int getAlloacatedMemory(Collection<VM> virtualMachines) {
        int answer = 0;
        for (VM vm : virtualMachines) {
            answer = answer + vm.getRamMb();
        }
        return answer;
    }

    /**
     * This for a set of VMs provides the amount of memory allocated in Mb.
     * @param virtualMachines The VMs to get the memory used.
     * @return The amount of memory allocated to VMs in Mb.
     */
    public static int getAlloacatedCpus(Collection<VM> virtualMachines) {
        int answer = 0;
        for (VM vm : virtualMachines) {
            answer = answer + vm.getCpus();
        }
        return answer;
    }

    /**
     * This for a set of VMs provides the amount of memory allocated in Mb.
     * @param virtualMachines The VMs to get the memory used.
     * @return The amount of memory allocated to VMs in Mb.
     */
    public static double getAlloacatedDiskSpace(Collection<VM> virtualMachines) {
        double answer = 0;
        for (VM vm : virtualMachines) {
            answer = answer + vm.getDiskGb();
        }
        return answer;
    }
    
    /**
     * The predictor function class represents a wrap around of a predictor 
     * and its estimated error.
     * @param <T> The type of the object that is to be used to generate the
     * prediction.
     */
    public class PredictorFunction<T> {
        
        T function;
        double sumOfSquareError;

        /**
         * This creates a new instance of a prediction function.
         * @param function The function that the predictor is to use to estimate
         * power/energy consumption.
         * @param sumOfSquareError The sum of the square error for the prediction function.
         */
        public PredictorFunction(T function, double sumOfSquareError) {
            this.function = function;
            this.sumOfSquareError = sumOfSquareError;
        }

        /**
         * This returns the object that provides the prediction function
         * @return The function that the predictor is to use to estimate
         * power/energy consumption.
         */
        public T getFunction() {
            return function;
        }

        /**
         * This returns the sum of the square error for the prediction function.
         * @return The sum of the square error for the prediction function.
         */
        public double getSumOfSquareError() {
            return sumOfSquareError;
        }    
    }    

    /**
     * TODO Add utility functions here that may be used by the energy models
     * that are created over the time of the project.
     */
    
}
