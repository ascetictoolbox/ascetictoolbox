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

package eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.billing;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;

import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.energyprovider.EnergyProvider;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.pricingschemesrepository.IaaSPricingModellerPricingScheme;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.pricingschemesrepository.PricingSchemeB;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.types.*;

/**
 * This is the standard interface for any pricing module to be loaded into the
 * ASCETiC architecture. This interface includes all the methods that another
 * component from the architecture may call.
 * 
 * @author E. Agiatzidou
 */

public class IaaSPricingModellerBilling implements IaaSPricingModellerBillingInterface {
	LinkedList<VMstate> queue = new LinkedList<VMstate>();

	static HashMap<String, VMstate> registeredStaticEnergyPricesVMs = new HashMap<String, VMstate>();
	static HashMap<String, VMstate> registeredDynamicEnergyPricesVMs = new HashMap<String, VMstate>();

	Price averageDynamicEnergyPrice = new StaticEnergyPrice();

	// Price staticEnergyPrice;

	// Price dynamicEnergyPrice;

	EnergyProvider energyProvider;
	//EnergyModeller energyModeller;

	public IaaSPricingModellerBilling(EnergyProvider provider) {
		this.energyProvider = provider;
		//this.energyModeller = null;
		
		// staticEnergyPrice = provider.getStaticEnergyPrice();
		// dynamicEnergyPrice = provider.getDynamicEnergyPrice();
	}
	
	//public void setEnergyModeller(EnergyModeller energyModeller){
	//	this.energyModeller = energyModeller;
	//}
	

	// ///////////////////////////// VM REGISTRATION////////////////////////////////
	@Override
	public void registerVM(VMstate vm) {
		if ((vm.getPricingScheme().getSchemeId() == 0)) {
			registeredStaticEnergyPricesVMs.put(vm.getVMid(), vm);
			vm.setStartTime();
			System.out.println("VM registered in static VMs");
		} else {
			registeredDynamicEnergyPricesVMs.put(vm.getVMid(), vm);
			System.out.println("VM registered in dynamic VMs");
			vm.setStartTime();
		}
	}

	public void registerVM(String VMid) {
		ListIterator<VMstate> listIterator = queue.listIterator();
		VMstate vm;
		boolean found = false;
		while (listIterator.hasNext()) {
			vm = listIterator.next();
			if (vm.getVMid().matches(VMid)) {
				if ((vm.getPricingScheme().getSchemeId() == 0)) {
					registeredStaticEnergyPricesVMs.put(vm.getVMid(), vm);
					System.out.println("VM registered in static VMs");
					vm.setStartTime();
				} else {
					registeredDynamicEnergyPricesVMs.put(vm.getVMid(), vm);
					System.out.println("VM registered in dynamic VMs");
					vm.setStartTime();
				}
				found = true;
			}
		}
		if (found == false)
			System.out.println("VM has not been found");
	}

	@Override
	public void unregisterVM(VMstate vm) {
		if ((vm.getPricingScheme().getSchemeId() == 0)){
			registeredStaticEnergyPricesVMs.remove(vm.getVMid());
			vm.setEndTime();}
		else{
			registeredDynamicEnergyPricesVMs.remove(vm.getVMid());
		vm.setEndTime();}

	}

	@Override
	public void stopChargingVM(VMstate vm) {
		vm.setEndTime();

	}

	// ////////////////////////////// FOR PREDICTION // /////////////////////////////////
	public double predictVMCharges(VMstate VM) {
		queue.push(VM);
		if (queue.size() > 10)
			queue.pollLast();
		IaaSPricingModellerPricingScheme scheme = VM.getPricingScheme();
		System.out.println("The scheme is " + scheme.getSchemeId());

		VM.setPredictedCharges(scheme.predictCharges(VM,
				averageDynamicEnergyPrice));
		System.out.println("The total predicted price is "
				+ VM.getPredictedCharges());
		return VM.getPredictedCharges();
	}

	
	
		// ///////////////////////////////BILLING////////////////////////////////////

	
	@Override
	public double getVMCharges(String VMid) {
		IaaSPricingModellerPricingScheme scheme = getVM(VMid)
				.getPricingScheme();
		return scheme.getTotalCharges(getVM(VMid));
	}

	public double getVMCharges(String VMid, boolean b) {
		IaaSPricingModellerPricingScheme scheme = getVM(VMid)
				.getPricingScheme();
		double charges = scheme.getTotalCharges(getVM(VMid));
		if (b)
			unregisterVM(getVM(VMid));
		else
			stopChargingVM(getVM(VMid));
		return charges;
	}

	// //////////////////////////// FOR DYNAMIC ENERGY PRICES  // /////////////////////////

	public void updateVMCharges(Price price) {
		System.out.println("Bil: The new energy price is "  + price.getPriceOnly());
		updateAverageEnergyPrice();
		for (String key : registeredDynamicEnergyPricesVMs.keySet()) {
			registeredDynamicEnergyPricesVMs.get(key).getPricingScheme()
					.updateVMCharges(getVM(key));
		}

	}

	public void updateAverageEnergyPrice() {
		IaaSPricingModellerPricingScheme scheme = new PricingSchemeB(1);
		averageDynamicEnergyPrice.setPrice(scheme.updateAverageEnergyPrice(
				energyProvider, averageDynamicEnergyPrice));
		System.out.println("Bil: and the new avg "
				+ averageDynamicEnergyPrice.getPriceOnly());
		System.out.println("Bil: and the new dynamic price is "
				+ energyProvider.getNewDynamicEnergyPrice().getPriceOnly());
	}
	
	public Price getAverageDynamicEnergyPrice() {
		return averageDynamicEnergyPrice;
	}


	// //////////////////// FOR DYNAMIC VM Characteristics//////////////////
	/*
	 * 
	 * public void updateVMcharacteristics(VMinfo vm, int VMid){
	 * IaaSPricingModellerCost cost = new IaaSPricingModellerCost();
	 * cost.updateDynamicResourcesCharges(getVM(VMid), vm);; }
	 */

	// ///////////////////////// GENERAL //////////////////////////

	public void printStaticVMs() {
		for (String i : registeredStaticEnergyPricesVMs.keySet()) {
			VMstate value = registeredStaticEnergyPricesVMs.get(i);
			System.out.println(i + " "
					+ value.getVMinfo().getVMCharacteristics());

		}
	}

	public void printDynamicVMs() {
		for (String i : registeredDynamicEnergyPricesVMs.keySet()) {
			VMstate value = registeredDynamicEnergyPricesVMs.get(i);
			System.out.println(i + " " + value.printVMCharacteristics());

		}
	}
	
	private VMstate getVM(String VMid) {
		if (registeredDynamicEnergyPricesVMs.containsKey(VMid))
			return registeredDynamicEnergyPricesVMs.get(VMid);
		else
			return registeredStaticEnergyPricesVMs.get(VMid);
	}

}