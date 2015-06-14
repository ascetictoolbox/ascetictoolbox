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

import java.util.HashMap;

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
	IaaSPricingModellerBilling billing = new IaaSPricingModellerBilling(energyProvider);
	
	static HashMap<Integer,IaaSPricingModellerPricingScheme> pricingSchemes = new HashMap<Integer,IaaSPricingModellerPricingScheme>();
	
	private static int idIaaSP=0;
	
	public IaaSPricingModeller() {
		idIaaSP=idIaaSP+1;
		IaaSPricingModellerPricingScheme A = new PricingSchemeA(1);
		IaaSPricingModellerPricingScheme B = new PricingSchemeA(2);
		pricingSchemes.put(1,A);
		pricingSchemes.put(2,B);
		
    }
	
	
	private IaaSPricingModellerPricingScheme findScheme(int schemeId){
		if (pricingSchemes.containsKey(schemeId))
			return pricingSchemes.get(schemeId);
		else
			return null;
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

	public double getVMChargesPrediction(int VMid, double RAM, double CPU, double storage, int schemeId, double energyPredicted, double duration){
		VMinfo vm = new VMinfo (RAM, CPU, storage);
		IaaSPricingModellerPricingScheme scheme = findScheme(schemeId);
		VMstate VM = new VMstate(VMid, vm, energyProvider, scheme);
		VM.setPrediction(duration, energyPredicted);
		return billing.predictVMCharges(VM);
	}
    
	
	
	///////////////////////////////////////////BILLING/////////////////////////////////////////////
	
	
	public double getVMCurrentCharges(int VMid){
		return billing.getVMCharges(VMid);
		
	}
	
	public double getVMFinalCharges(int VMid, boolean unregister){
		return billing.getVMCharges(VMid, unregister);
	}
   
	
	
	/////////////////Dynamic Changes to VM characteristics/////////////////////////////////
	/*public void changeVMcharacteristics(int VMid, double RAM, double CPU, double storage){
		VMinfo vm = new VMinfo (RAM, CPU, storage);
		billing.updateVMcharacteristics(vm, VMid);
	}
   */
	
	
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
    
    public void initializeVM(int VMid, double RAM, double CPU, double storage, int schemeId){
		VMinfo vm = new VMinfo (RAM, CPU, storage);
		IaaSPricingModellerPricingScheme scheme = findScheme(schemeId);
		VMstate VM = new VMstate(VMid, vm, energyProvider, scheme);
		billing.registerVM(VM);
	}
	
	
	public void initializeVM(int VMid){
		billing.registerVM(VMid);
	}
}