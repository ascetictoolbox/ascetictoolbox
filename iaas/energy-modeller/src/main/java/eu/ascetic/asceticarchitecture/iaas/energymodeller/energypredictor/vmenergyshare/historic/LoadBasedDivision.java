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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.vmenergyshare.historic;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VmDeployed;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.usage.HostVmLoadFraction;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.HostEnergyRecord;

/**
 * This creates a load based division mechanism for dividing host energy among
 * VMs. It is intended to be used with historic load data.
 *
 * Energy is fractioned out taking into account the amount of load has been
 * placed on each machine. This is done by ratio.
 *
 * @author Richard
 */
public class LoadBasedDivision extends AbstractHistoricLoadBasedDivision {

    /**
     * This creates a load based division mechanism for the specified host, that
     * is yet to be specified.
     */
    public LoadBasedDivision() {
    }

    /**
     * This creates a load based division mechanism for the specified host.
     *
     * @param host The host to divide energy for, among its VMs.
     */
    public LoadBasedDivision(Host host) {
        super(host);
    }

    /**
     * This returns the energy usage for a named VM
     *
     * @param vm The VM to get energy usage for.
     * @return The energy used by this VM.
     */
    @Override
    public double getEnergyUsage(VM vm) {
        VmDeployed deployed = (VmDeployed) vm;
        cleanData();
        int recordCount = (energyUsage.size() <= loadFraction.size() ? energyUsage.size() : loadFraction.size());

        /**
         * Calculate the energy used by a VM taking into account the work it has
         * performed.
         */
        double vmEnergy = 0;
        //Access two records at once hence ensure size() -2
        for (int i = 0; i <= recordCount - 2; i++) {
            HostEnergyRecord energy1 = energyUsage.get(i);
            HostEnergyRecord energy2 = energyUsage.get(i + 1);
            HostVmLoadFraction load1 = loadFraction.get(i);
            HostVmLoadFraction load2 = loadFraction.get(i + 1);
            if (load1.getVMs().contains(deployed) && load2.getVMs().contains(deployed)) {
                long timePeriod = energy2.getTime() - energy1.getTime();
                double deltaEnergy = Math.abs((((double) timePeriod) / 3600d)
                        * (energy1.getPower() + load1.getHostPowerOffset()
                        + energy2.getPower() + load2.getHostPowerOffset()) * 0.5);
                double avgLoadFraction = (load1.getFraction(deployed) + load2.getFraction(deployed)) / 2;
                vmEnergy = vmEnergy + (deltaEnergy * avgLoadFraction);
            }
        }
        return vmEnergy;
    }
}
