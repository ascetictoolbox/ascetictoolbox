/**
 Copyright (C) 2013-2014  Barcelona Supercomputing Center

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package es.bsc.vmmanagercore.modellers.price.ascetic;

import es.bsc.vmmanagercore.modellers.price.PricingModeller;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.EnergyModeller;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.IaaSPricingModeller;

/**
 * Connector for the pricing modeller developed in the Ascetic project by AUEB.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class AsceticPricingModellerAdapter implements PricingModeller {

    private static IaaSPricingModeller pricingModeller = new IaaSPricingModeller();

    @Override
    public double getVmCost(double totalEnergy, String hostname) {
        return pricingModeller.getVMCostEstimation(totalEnergy, hostname);
    }

    /*
    Right now, we do not know at the IaaS level how long a VM is going to be executed.
    Anyway, we just need to make fair comparisons. Therefore, selecting the same time for all the VMs is good
    enough for now. It is important to take into account that this ignores the fact that VMs that are given a host
    with more available resources will take less time to complete its execution.
     */

    /*

    private static final long FIXED_DURATION_MIN = 60;
    private static IaaSPricingModeller pricingModeller;

    public AsceticPricingModellerAdapter(EnergyModeller energyModeller) {
        pricingModeller = new IaaSPricingModeller(energyModeller);
    }

    @Override
    public double getVmCost(String vmId, String hostname) {
        return pricingModeller.getVMChargesPrediction(
                constructVmIdForPricingModeller(vmId, hostname), getSchemeIdForVm(), FIXED_DURATION_MIN);
    }

    private String constructVmIdForPricingModeller(String vmId, String hostname) {
        return vmId + "_" + hostname;
    }

    // For now, return always 1. This will be changed once we get this information from the PaaS level.
    private int getSchemeIdForVm() {
        return 1;
    }

    */

}
