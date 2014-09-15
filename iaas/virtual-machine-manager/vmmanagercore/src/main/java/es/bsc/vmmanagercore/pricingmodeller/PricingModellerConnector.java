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

package es.bsc.vmmanagercore.pricingmodeller;

import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.IaaSPricingModeller;

/**
 * Connector for the pricing modeller developed by AUEB.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class PricingModellerConnector {

    private static IaaSPricingModeller pricingModeller = new IaaSPricingModeller();

    /**
     * Returns the predicted cost on a given host for a given amount of energy.
     *
     * @param totalEnergy total energy consumed by the VM (joules)
     * @param hostname the hostname
     * @return the predicted cost of the VM
     */
    public static double getVmCost(double totalEnergy, String hostname) {
        return pricingModeller.getVMCostEstimation(totalEnergy, hostname);
    }

}
