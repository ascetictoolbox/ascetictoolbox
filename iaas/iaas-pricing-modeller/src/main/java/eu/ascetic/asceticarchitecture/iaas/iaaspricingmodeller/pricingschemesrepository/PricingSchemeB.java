/**
 *  Copyright 2015 Athens University of Economics and Business
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


package eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.pricingschemesrepository;

import java.util.Calendar;

import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.types.Charges;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.types.DynamicEnergyPrice;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.types.Price;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.types.ResourceDistribution;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.types.StaticResourcePrice;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.types.VMstate;



/**
 * 
 * @author E. Agiatzidou
 */

public class PricingSchemeB extends IaaSPricingModellerPricingScheme implements IaaSPricingModellerPricingSchemeRepositoryInterface{

	StaticResourcePrice price;
	
	DynamicEnergyPrice energyPrice;

	
	ResourceDistribution distribution = new ResourceDistribution();

	
	public PricingSchemeB(int id) {
		super(id);
		price = new StaticResourcePrice(0.1);
	}

/////////////////////////PREDICT CHARGES ///////////////////////////////
	public double predictCharges(VMstate vm, Price average){
		Charges a = predictEnergyCharges(vm, average);
		Charges b = predictResourcesCharges(vm, price);
		double temp = (double) Math.round((a.getChargesOnly()+b.getChargesOnly()) * 1000) / 1000;
		return temp;
	}
	



///////////////////////////// UPDATE CHARGES BASED ON ENERGY CHANGES ////////////////
	public void updateVMCharges(VMstate VM) {
		updateVMEnergyCharges(VM);
		updateVMResourceCharges(VM, price);
		VM.setChangeTime(VM.getResourcesChangeTime());
		VM.setTotalCharges(VM.getEnergyCharges()+VM.getResourcesCharges());
	}

	

	
	/////////////////////////// GET CHARGES /////////////////////////
	@Override
	public double getTotalCharges(VMstate VM) {
		updateVMEnergyCharges(VM);
		updateVMResourceCharges(VM, price);
		VM.setChangeTime(VM.getResourcesChangeTime());
		VM.setTotalCharges(VM.getResourcesCharges()+VM.getEnergyCharges());
		return (VM.getTotalCharges());
	}

	@Override
	public ResourceDistribution getDistribution() {
		return distribution;
	}

	@Override
	public Price getResourcePrice() {
		return price;
	}
	

}