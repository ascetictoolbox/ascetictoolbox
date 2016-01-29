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

package es.bsc.vmm.ascetic.modellers.price.ascetic;

import es.bsc.vmm.ascetic.modellers.price.PricingModeller;
import es.bsc.vmm.ascetic.modellers.energy.ascetic.AsceticEnergyModellerAdapter;
import es.bsc.demiurge.core.clopla.domain.Host;
import es.bsc.demiurge.core.drivers.VmAction;
import es.bsc.demiurge.core.models.scheduling.DeploymentPlan;
import es.bsc.demiurge.core.models.scheduling.VmAssignmentToHost;
import es.bsc.demiurge.core.models.vms.Vm;
import es.bsc.demiurge.core.models.vms.VmDeployed;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.IaaSPricingModeller;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.billing.IaaSPricingModellerBilling;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.energyprovider.EnergyProvider;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.pricingschemesrepository.IaaSPricingModellerPricingScheme;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.types.EnergyPrediction;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


import java.util.List;
import java.util.Map;

/**
 * Connector for the pricing modeller developed in the Ascetic project by AUEB.
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class AsceticPricingModellerAdapter implements PricingModeller {

    /*
    Right now, we do not know at the IaaS level for how long a VM is going to be running.
    Anyway, we just need to make fair comparisons. Therefore, selecting the same time for all the VMs is good
    enough for now. It is important to take into account that this ignores the fact that VMs that are given a host
    with more available resources will take less time to complete its execution.
     */



    private static final long FIXED_DURATION_SEC = 3600;
    private static IaaSPricingModeller pricingModeller;

	private Logger log = LogManager.getLogger(AsceticPricingModellerAdapter.class);

    public AsceticPricingModellerAdapter(AsceticEnergyModellerAdapter energyModeller) {
        pricingModeller = new IaaSPricingModeller(energyModeller.getEnergyModeller());
    }

	/*
    public double getVmCost(int cpus, int ramMb, int diskGb, String hostname) {

    }*/


    // For now, return always 1. This will be changed once we get this information from the PaaS level.
    private int getSchemeIdForVm() {
        return 1;
    }

	@Override
	public double getVMChargesPrediction(int cpus, int ramMb, double diskGb, String hostname) {
		return pricingModeller.getVMChargesPrediction(
				cpus, ramMb, diskGb, getSchemeIdForVm(), FIXED_DURATION_SEC, hostname);
	}

	@Override
	public IaaSPricingModeller getIaaSprovider(int id) {
		return pricingModeller.getIaaSprovider(id);
	}

	@Override
	public EnergyProvider getEnergyProvider() {
		return pricingModeller.getEnergyProvider();
	}

	@Override
	public IaaSPricingModellerPricingScheme initializeScheme(int schemeId) {
		return pricingModeller.initializeScheme(schemeId);
	}

	@Override
	public double getVMPricePerHourPrediction(int CPU, int RAM, double storage, int schemeId, long duration, String hostname) {
		return pricingModeller.getVMPricePerHourPrediction(CPU, RAM, storage, schemeId, duration, hostname);
	}

	@Override
	public double getVMFinalCharges(String VMid, boolean deleteVM) {
		return pricingModeller.getVMFinalCharges(VMid, deleteVM);
	}

	@Override
	public EnergyPrediction getEnergyPredicted(int CPU, int RAM, double storage, long duration, String hostname) {
		return pricingModeller.getEnergyPredicted(CPU, RAM, storage, duration, hostname);
	}

	@Override
	public EnergyPrediction getEnergyPredicted(int CPU, int RAM, double storage, String hostname) {
		return pricingModeller.getEnergyPredicted(CPU, RAM, storage, hostname);
	}

	@Override
	public void initializeVM(String VMid, String hostname, String appId ) {
		pricingModeller.initializeVM(VMid, getSchemeIdForVm(), hostname, appId);
	}

	@Override
	public IaaSPricingModellerBilling getBilling() {
		return pricingModeller.getBilling();
	}

	@Override
	public String getLabel() {
		return "cost";
	}

	@Override
	public double getDeploymentEstimation(VmAssignmentToHost vma, List<VmDeployed> vmsDeployed, DeploymentPlan deploymentPlan) {
		return getVMChargesPrediction(vma.getVm().getCpus(), vma.getVm().getRamMb(), vma.getVm().getDiskGb(), vma.getHost().getHostname());
	}

	@Override
	public double getCurrentEstimation(String vmId, Map options) {
		Boolean deleteVM = options.get("deleteVM") != null && options.get("deleteVM").equals(Boolean.TRUE);
		return this.getVMFinalCharges(vmId, deleteVM);
	}

	@Override
	public double getCloplaEstimation(Host host, List<es.bsc.demiurge.core.clopla.domain.Vm> vmsDeployedInHost) {
		double result = 0.0;
		for (es.bsc.demiurge.core.clopla.domain.Vm vm: vmsDeployedInHost) {
			result += pricingModeller.getVMChargesPrediction(
					vm.getNcpus(), vm.getRamMb(), vm.getDiskGb(), getSchemeIdForVm(), FIXED_DURATION_SEC , host.getHostname());
		}
		return result;
	}

	@Override
	public void onVmDeployment(final VmDeployed vm) {
		Thread thread = new Thread() {
			public void run(){
				//
				try {
					log.debug("Waiting 10 seconds before initializing VM billing. VM ID = " + vm.getId() + "; Hostname = " + vm.getHostName());
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				pricingModeller.initializeVM(vm.getId(), getSchemeIdForVm(),  vm.getHostName(), vm.getApplicationId());
			}
		};
		thread.start();

	}
	@Override
	public void onVmDestruction(final VmDeployed vm) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					// indicating vm has been stopped
					pricingModeller.getVMFinalCharges(vm.getId(),true);
				} catch (Exception e) {
					log.warn("Error closing pricing Modeler for VM " + vm.getId() + ": " + e.getMessage());
				}
			}
		}).start();
	}
	@Override
	public void onVmMigration(VmDeployed vm) {
		// do nothing
	}
	@Override
	public void onVmAction(VmDeployed vm, VmAction action) {
		// do nothing
	}

	@Override
	public void onPreVmDeployment(Vm vm) {
		// do nothing
	}
}
