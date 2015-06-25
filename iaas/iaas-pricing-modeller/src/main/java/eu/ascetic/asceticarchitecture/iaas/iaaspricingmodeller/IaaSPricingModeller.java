/**
 *  Copyright 2014 Athens University of Economics and Business
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.EnergyModeller;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.TimePeriod;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VmDeployed;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.energyprovider.EnergyProvider;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.pricingschemesrepository.IaaSPricingModellerPricingScheme;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.pricingschemesrepository.PricingSchemeA;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.pricingschemesrepository.PricingSchemeB;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.types.*;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.billing.*;

/**
 * This is the main interface of the pricing modeller of IaaS layer. 
 * Functionality:
 * 1. The ability to provide a cost estimation of a VM per hour, given the energy consumed of this VM and the host that
 * this VM is deployed.
 *
 * 2. The ability to provide a price estimation of a VM per hour, given the energy consumed of this VM and the host that
 * this VM is deployed.
 * @author E. Agiatzidou
 */

public class IaaSPricingModeller implements IaaSPricingModellerInterface{
	
	EnergyProvider energyProvider = new EnergyProvider(this);
	EnergyModeller energyModeller;
	IaaSPricingModellerBilling billing = new IaaSPricingModellerBilling(energyProvider);
	private static int idIaaSP=0;
	
	public IaaSPricingModeller(EnergyModeller energyModeller) {
		this.energyModeller = energyModeller;
		//billing.setEnergyModeller(energyModeller);
		idIaaSP=idIaaSP+1;
		
    }
	
	///////////////////////////////////////PREDICTION////////////////////////////////////////////
	 /**
     * This function returns a price estimation based on the 
     * total energy that a VM consumes during an hour. The VM runs on top of a 
     * specific host.
     * @param totalEnergyUsed total estimated energy that a VM consumes during an hour
     * @param hostId the id of the host that the VM is running on
     * @return the estimated price of the VM running on this host
     */

	public double getVMChargesPrediction(String VMid, int schemeId,  long duration, String hostname){
		int CPU = energyModeller.getVM(VMid).getCpus();
		int RAM = energyModeller.getVM(VMid).getRamMb();
		double storage = energyModeller.getVM(VMid).getDiskGb();
		VMinfo vm = new VMinfo (RAM, CPU, storage);
		IaaSPricingModellerPricingScheme scheme = null;
		if (schemeId==0){
			scheme = new PricingSchemeA(schemeId);
		}
		if (schemeId==1){
			scheme = new PricingSchemeB(schemeId);
		}
		VMstate VM = new VMstate(VMid, vm, energyProvider, scheme);
		scheme.setEnergyModeller(energyModeller);
		
		VM.getPredictedInformation().setDuration(duration);
		
		VmDeployed EVM = energyModeller.getVM(VMid);
		Host host = energyModeller.getHost(hostname);
		Collection <VmDeployed> collection =  energyModeller.getVMsOnHost(host);
		Collection <VM> col = castCollection(collection);
		TimeParameters dur = new TimeParameters(duration);
		TimePeriod dura = new TimePeriod(dur.getStartTime(), dur.getEndTime());
		double energyPredicted = energyModeller.getPredictedEnergyForVM(EVM, col, host, dura).getTotalEnergyUsed();
		VM.getPredictedInformation().setPredictedEnergy(energyPredicted);
		
		return billing.predictVMCharges(VM);
	}
    
	private Collection <VM> castCollection(Collection<VmDeployed> collection){
		Collection<VM> col = new ArrayList<VM>();
		Iterator<VmDeployed> itr = collection.iterator();
	      while(itr.hasNext()) {
	         VmDeployed element = itr.next();
	         col.add((VM)element);
	      }
	      return col;
	}
	
	///////////////////////////////////////////BILLING/////////////////////////////////////////////
	
	
	public void initializeVM(String VMid, int schemeId){
		int CPU = energyModeller.getVM(VMid).getCpus();
		int RAM = energyModeller.getVM(VMid).getRamMb();
		double storage = energyModeller.getVM(VMid).getDiskGb();
		VMinfo vm = new VMinfo (RAM, CPU, storage);

		IaaSPricingModellerPricingScheme scheme = null;
		if (schemeId==0){
			scheme = new PricingSchemeA(schemeId);
		}
		if (schemeId==1){
			scheme = new PricingSchemeB(schemeId);
		}
		scheme.setEnergyModeller(energyModeller);
		VMstate VM = new VMstate(VMid, vm, energyProvider, scheme);
		billing.registerVM(VM);
	}
	
	public void initializeVM(String VMid){
		billing.registerVM(VMid);
	}
	
	
	public double getVMCurrentCharges(String VMid){
		return billing.getVMCharges(VMid);
		
	}
	
	public double getVMFinalCharges(String VMid, boolean deleteVM){
		return billing.getVMCharges(VMid, deleteVM);
	}
   
	
	///////////////////////Basic functions///////////////////////////////
	public int getIaaSId(){
		return idIaaSP;
	}
	
	
    public EnergyProvider getEnergyProvider(){
    	return this.energyProvider;
    }
    
    public IaaSPricingModellerBilling getBilling(){
    	return this.billing;
    }
    
    public IaaSPricingModeller getIaaSprovider(int id){
    	return this;
    }
}