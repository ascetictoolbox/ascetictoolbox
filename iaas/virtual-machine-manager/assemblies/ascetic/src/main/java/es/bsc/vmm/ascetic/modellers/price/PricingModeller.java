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

package es.bsc.vmm.ascetic.modellers.price;


import es.bsc.demiurge.core.drivers.Estimator;
import es.bsc.demiurge.core.drivers.VmmListener;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.IaaSPricingModeller;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.billing.IaaSPricingModellerBilling;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.energyprovider.EnergyProvider;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.pricingschemesrepository.IaaSPricingModellerPricingScheme;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.types.EnergyPrediction;

/**
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 */
public interface PricingModeller extends Estimator, VmmListener {

    /*
     * Returns the predicted cost on a given host for a given VM size
     *
     * @param cpus the cpus of the VM
     * @param ramMb the RAM (in MB) of the VM
     * @param diskGb the disk size (in GB) of the VM
     * @param hostname the hostname
     * @return the predicted cost of the VM
     */
//    double getVmCost(int cpus, int ramMb, int diskGb, String hostname);

	double getVMChargesPrediction(int cpus, int ramMb, double diskGb, String hostname);

	IaaSPricingModeller getIaaSprovider(int id);

	EnergyProvider getEnergyProvider();

	IaaSPricingModellerPricingScheme initializeScheme(int schemeId);

	double getVMPricePerHourPrediction(int CPU, int RAM, double storage, int schemeId, long duration, String hostname);

	double getVMFinalCharges(String VMid, boolean deleteVM);

	EnergyPrediction getEnergyPredicted(int CPU, int RAM, double storage, long duration, String hostname);

	EnergyPrediction getEnergyPredicted(int CPU, int RAM, double storage, String hostname);

	void initializeVM(String VMid, String hostname, String appId);

	IaaSPricingModellerBilling getBilling();

}
