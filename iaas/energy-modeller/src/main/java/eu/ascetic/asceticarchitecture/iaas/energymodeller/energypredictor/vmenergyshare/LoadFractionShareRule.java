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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.vmenergyshare;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.VmMeasurement;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VmDeployed;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.usage.HostVmLoadFraction;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This looks at the fraction of load placed on each VM and determines the 
 * share of energy that is should have based upon this.
 * @author Richard
 */
public class LoadFractionShareRule implements EnergyShareRule {

    private HashMap<VmDeployed, Double> fractions = new HashMap<>();
    
    @Override
    public EnergyDivision getEnergyUsage(Host host, Collection<VM> vms) {
        EnergyDivision answer = new EnergyDivision(host);
        for (VM vm : vms) {
            VmDeployed deployed = (VmDeployed) vm;
            answer.addVmWeight(vm, fractions.get(deployed));
            Logger.getLogger(LoadFractionShareRule.class.getName()).log(Level.FINE, "VM: {0} Ratio: {1}", new Object[]{deployed.getName(), fractions.get(deployed)});
        }
        return answer;
    }

    /**
     * This sets the load fractions to use in this energy share rule.
     * @param vmMeasurements The Vm measurements that are used to set this load
     * fraction data.
     */
    public void setVmMeasurements(List<VmMeasurement> vmMeasurements) {
        fractions = HostVmLoadFraction.getFraction(vmMeasurements);
    }   

    /**
     * This returns the data that indicates which VMs should take which fraction
     * of the overall energy.
     * @return the fractioning of the host energy data.
     */
    public HashMap<VmDeployed, Double> getFractions() {
        return fractions;
    }

    /**
     * This allows the data that indicates which VMs should take which fraction
     * of the overall energy to be directly set.
     * @param fractions the fractioning of the host energy data to set.
     */
    public void setFractions(HashMap<VmDeployed, Double> fractions) {
        this.fractions = fractions;
    }

}
