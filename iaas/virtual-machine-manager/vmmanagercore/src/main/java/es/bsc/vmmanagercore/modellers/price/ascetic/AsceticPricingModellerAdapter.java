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
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.billing.IaaSPricingModellerBilling;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.energyprovider.EnergyProvider;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.pricingschemesrepository.IaaSPricingModellerPricingScheme;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.types.EnergyPrediction;

/**
 * Connector for the pricing modeller developed in the Ascetic project by AUEB.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class AsceticPricingModellerAdapter implements PricingModeller {

    /*
    Right now, we do not know at the IaaS level for how long a VM is going to be running.
    Anyway, we just need to make fair comparisons. Therefore, selecting the same time for all the VMs is good
    enough for now. It is important to take into account that this ignores the fact that VMs that are given a host
    with more available resources will take less time to complete its execution.
     */

    private static final long FIXED_DURATION_MIN = 60;
    private static IaaSPricingModeller pricingModeller;

    public AsceticPricingModellerAdapter(EnergyModeller energyModeller) {
        pricingModeller = new IaaSPricingModeller(energyModeller);
    }

	/*
    public double getVmCost(int cpus, int ramMb, int diskGb, String hostname) {

    }*/

    public void initializeVmBilling(String vmId, String hostname) {
        pricingModeller.initializeVM(vmId, getSchemeIdForVm(), hostname);
    }

    // For now, return always 1. This will be changed once we get this information from the PaaS level.
    private int getSchemeIdForVm() {
        return 1;
    }

	public double getVMChargesPrediction(int cpus, int ramMb, double diskGb, String hostname) {
		return pricingModeller.getVMChargesPrediction(
				cpus, ramMb, diskGb, getSchemeIdForVm(), FIXED_DURATION_MIN, hostname);
	}

	public IaaSPricingModeller getIaaSprovider(int id) {
		return pricingModeller.getIaaSprovider(id);
	}

	public EnergyProvider getEnergyProvider() {
		return pricingModeller.getEnergyProvider();
	}

	public IaaSPricingModellerPricingScheme initializeScheme(int schemeId) {
		return pricingModeller.initializeScheme(schemeId);
	}

	public double getVMPricePerHourPrediction(int CPU, int RAM, double storage, int schemeId, long duration, String hostname) {
		return pricingModeller.getVMPricePerHourPrediction(CPU, RAM, storage, schemeId, duration, hostname);
	}

	public double getVMFinalCharges(String VMid, boolean deleteVM) {
		return pricingModeller.getVMFinalCharges(VMid, deleteVM);
	}

	public EnergyPrediction getEnergyPredicted(int CPU, int RAM, double storage, long duration, String hostname) {
		return pricingModeller.getEnergyPredicted(CPU, RAM, storage, duration, hostname);
	}

	public EnergyPrediction getEnergyPredicted(int CPU, int RAM, double storage, String hostname) {
		return pricingModeller.getEnergyPredicted(CPU, RAM, storage, hostname);
	}

	public void initializeVM(String VMid, int schemeId, String hostname) {
		pricingModeller.initializeVM(VMid, schemeId, hostname);
	}

	public IaaSPricingModellerBilling getBilling() {
		return pricingModeller.getBilling();
	}
}
