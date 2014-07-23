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
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM;
import java.util.Collection;

/**
 * This implements the default and utility functions for an energy predictor. 
 * It is expected that any energy predictor loaded into the ASCETiC architecture, 
 * will override this class.
 *
 * @author Richard
 */
public abstract class AbstractEnergyPredictor implements EnergyPredictorInterface {

    private EnergyShareRule energyShareRule = new DefaultEnergyShareRule();
    
    /**
     * This uses the current energy share rule for the energy predictor
     * allowing for the translation between host energy usage and VMs energy usage.
     * @param host The host to analyse
     * @param vms The VMs that are on/to be on the host
     * @return The fraction of energy or used per host.
     */
    public EnergyDivision getEnergyUsage(Host host, Collection<VM> vms){
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
     * TODO Add utility functions here that may be used by the energy models 
     * that are created over the time of the project.
     */
    
}
