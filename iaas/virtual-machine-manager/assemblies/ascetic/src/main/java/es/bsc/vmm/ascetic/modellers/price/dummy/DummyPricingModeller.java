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

package es.bsc.vmm.ascetic.modellers.price.dummy;

import es.bsc.vmm.ascetic.modellers.price.PricingModeller;
import es.bsc.demiurge.core.clopla.domain.Host;
import es.bsc.demiurge.core.clopla.domain.Vm;
import es.bsc.demiurge.core.drivers.VmAction;
import es.bsc.demiurge.core.models.scheduling.DeploymentPlan;
import es.bsc.demiurge.core.models.scheduling.VmAssignmentToHost;
import es.bsc.demiurge.core.models.vms.VmDeployed;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.IaaSPricingModeller;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.billing.IaaSPricingModellerBilling;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.energyprovider.EnergyProvider;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.pricingschemesrepository.IaaSPricingModellerPricingScheme;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.types.EnergyPrediction;

import java.util.List;
import java.util.Map;

/**
 * This is a dummy Pricing Modeller. It always returns 0, but it can be helpful in cases where there is not any
 * Pricing Modeller defined.
 * Using this Modeller is safer than setting the Pricing Modeller attribute to null in the VMM.
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class DummyPricingModeller implements PricingModeller {

	@Override
	public double getVMChargesPrediction(int cpus, int ramMb, double diskGb, String hostname) {
		return 0;
	}

	@Override
	public IaaSPricingModeller getIaaSprovider(int id) {
		return null;
	}

	@Override
	public EnergyProvider getEnergyProvider() {
		return null;
	}

	@Override
	public IaaSPricingModellerPricingScheme initializeScheme(int schemeId) {
		return null;
	}

	@Override
	public double getVMPricePerHourPrediction(int CPU, int RAM, double storage, int schemeId, long duration, String hostname) {
		return 0;
	}

	@Override
	public double getVMFinalCharges(String VMid, boolean deleteVM) {
		return 0;
	}

	@Override
	public EnergyPrediction getEnergyPredicted(int CPU, int RAM, double storage, long duration, String hostname) {
		return null;
	}

	@Override
	public EnergyPrediction getEnergyPredicted(int CPU, int RAM, double storage, String hostname) {
		return null;
	}

	@Override
	public void initializeVM(String VMid, String hostname, String appId) {

	}

	@Override
	public IaaSPricingModellerBilling getBilling() {
		return null;
	}

	@Override
	public String getLabel() {
		return "dummy";
	}

	@Override
	public double getDeploymentEstimation(VmAssignmentToHost vma, List<VmDeployed> vmsDeployed, DeploymentPlan deploymentPlan) {
		return 0;
	}

	@Override
	public double getCurrentEstimation(String vmId, Map options) {
		return 0;
	}

	@Override
	public double getCloplaEstimation(Host host, List<Vm> vmsDeployedInHost) {
		return 0;
	}

	@Override
	public void onVmDeployment(VmDeployed vm) {

	}

	@Override
	public void onVmDestruction(VmDeployed vm) {

	}

	@Override
	public void onVmMigration(VmDeployed vm) {

	}

	@Override
	public void onVmAction(VmDeployed vm, VmAction action) {

	}

	@Override
	public void onPreVmDeployment(es.bsc.demiurge.core.models.vms.Vm vm) {

	}
}
