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

import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.cost.IaaSPricingModellerCost;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.types.Charges;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.types.Price;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.types.ResourceDistribution;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.types.StaticResourcePrice;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.types.Time;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.types.VMstate;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.types.VMinfo;

/**
 * This pricing scheme has ID==0. 
 * It gives only a static prices per hour. 
 * @author E. Agiatzidou
 */



public class PricingSchemeA extends IaaSPricingModellerPricingScheme implements IaaSPricingModellerPricingSchemeRepositoryInterface {
	
	StaticResourcePrice price;
	
	ResourceDistribution distribution = new ResourceDistribution();
	
	public PricingSchemeA(int id) {
		super(id);
		price = new StaticResourcePrice();
		distribution.setDistribution(0.6, 0.3, 0.2);
		
	}

	
	
	/////////////////////////PREDICTION/////////////////////////
	@Override
	public double predictCharges(VMstate vm, Price average) {
		Charges b = predictResourcesCharges(vm);
		System.out.println("B: The resource charges are " + b); 
		return b.getChargesOnly();
	}
	
	
	public Charges predictResourcesCharges(VMstate vm) {
		//System.out.println("B: The resource price is " + VM.getResourcePrice() + " and the duration is " + VM.getVMinfo().getPredictedDuration());
		Charges b = new Charges();
		b.setCharges(distribution.getDistribution(vm)*price.getPriceOnly()*vm.getPredictedInformation().getPredictedDuration());
		return b;
	}
	
	
	//////////// UPDATE CHARGES AFTER ENERGY CHANGE //////////////////
	
	@Override
	public void updateVMCharges(VMstate VM) {
		// TODO Auto-generated method stub
		
	}


////////////////////////////////// BILLING //////////////////////////
	
	
	@Override
	public double getTotalCharges(VMstate VM) {
		updateVMResourceCharges(VM);
		VM.setChangeTime(VM.getResourcesChangeTime());
		return (VM.getResourcesCharges()+VM.getEnergyCharges());

		
	}

	public void updateVMResourceCharges(VMstate VM){
		System.out.println("Pr: The VMid is "+VM.getVMid() +"and the static price is " + price);
		Time time = new Time();
		double duration = time.difTime(VM.getChangeTime());
		double Resourcecharges = distribution.getDistribution(VM)*price.getPriceOnly()*duration;
		VM.updateResourcesCharges(Resourcecharges);

	}

	
	
	
	
}