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

import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM;
import java.util.HashMap;

/**
 * This class provides a way of assigning weights to VMs to indicate how much of
 * a hosts energy usage should be allocated to each VM.
 *
 * This is expected to be used particularly in regards to prediction where load
 * data is not available, hence fractioning energy usage cannot simply use
 * measured values.
 *
 * @author Richard Kavanagh
 */
public class EnergyDivision {

    private Host host;
    private final HashMap<VM, Double> vmWeight = new HashMap<>();
    private double sumOfWeights = 0; // A cached sum of all weights assigned.
    private boolean considerIdleEnergy = false;

    /**
     * This creates a new energy division record, that indicates the ratios by
     * which energy should be divided among the VMs on a given host.
     *
     * @param host The host who's energy is to be divided among the VMs
     */
    public EnergyDivision(Host host) {
        this.host = host;
    }

    /**
     * This returns the host who's energy is to be fractioned out.
     *
     * @return the host The host in which the VMs reside/are planned to reside.
     */
    public Host getHost() {
        return host;
    }

    /**
     * This sets the host who's energy is to be fractioned out.
     *
     * @param host The host who's energy is to be divided among the VMs
     */
    public void setHost(Host host) {
        this.host = host;
    }

    /**
     * This gets the VMs held in this division record and their associated
     * weights.
     *
     * @return the vmWeight
     */
    public HashMap<VM, Double> getVmWeight() {
        return vmWeight;
    }

    /**
     * This adds a VM and its associated weight to the energy division. If the
     * VM is already present the weight assigned will get updated.
     *
     * @param vm The VM to add to the energy division
     * @param weight The weight to assign the VM. The weight must be positive,
     * in order for it to be added.
     */
    public void addVmWeight(VM vm, double weight) {
        if (weight >= 0.0) {
            sumOfWeights += weight;
            Double previous = vmWeight.put(vm, weight);
            //If the vms was already added remove the previous weight.
            if (previous != null) {
                sumOfWeights -= previous;
            }
        }
    }

    /**
     * This removes a VM from the energy division record.
     *
     * @param vm The VM to remove from the energy division
     */
    public void removeVM(VM vm) {
        vmWeight.remove(vm);
    }

    /**
     * This takes a VM and shows the energy that it uses relative to the weights
     * assigned in this energy division record.
     *
     * @param hostEnergyUsage The amount of energy or power used by a given
     * host.
     * @param vm The VM to get the energy value from
     * @return The amount of energy used by the given VM.
     */
    public double getEnergyUsage(double hostEnergyUsage, VM vm) {
        double vmsWeight = vmWeight.get(vm);
        if (considerIdleEnergy) {
            //active energy usage of a VM
            double activeEnergy = (hostEnergyUsage - host.getIdlePowerConsumption());
            if (activeEnergy < 0) {
                activeEnergy = 0;
            }
            double answer = (vmsWeight / sumOfWeights) * activeEnergy;
            /**
             * Fraction off the energy associated with the host been idle
             * evenly.
             */
            answer = answer + host.getIdlePowerConsumption() / vmWeight.size();
            return answer;
        } else {
            return (vmsWeight / sumOfWeights) * hostEnergyUsage;
        }
    }

    /**
     * This indicates if the energy division record should take account of
     * host's idle energy usage.
     *
     * @return the considerIdleEnergy
     */
    public boolean isConsiderIdleEnergy() {
        return considerIdleEnergy;
    }

    /**
     * This sets if the energy division record should take account of host's
     * idle energy usage.
     *
     * @param considerIdleEnergy the considerIdleEnergy to set
     */
    public void setConsiderIdleEnergy(boolean considerIdleEnergy) {
        this.considerIdleEnergy = considerIdleEnergy;
    }

}
