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
		distribution.setDistribution(0.6, 0.3, 0.2);
	}

/////////////////////////PREDICT CHARGES ///////////////////////////////
	public double predictCharges(VMstate vm, Price average){
		Charges a = predictEnergyCharges(vm, average);
		Charges b = predictResourcesCharges(vm);
		double temp = (double) Math.round((a.getChargesOnly()+b.getChargesOnly()) * 1000) / 1000;
		return temp;
	}
	
	public Charges predictEnergyCharges(VMstate VM, Price average){
		
		Charges charges = new Charges();
			charges.setCharges(VM.getPredictedInformation().getPredictedEnergy()*average.getPriceOnly());
			return charges;
	}
	
	
	public Charges predictResourcesCharges(VMstate vm) {
		Charges b = new Charges();
		b.setCharges(distribution.getDistribution(vm)*price.getPriceOnly()*(vm.getPredictedInformation().getPredictedDuration()/3600));
		return b;
	}

///////////////////////////// UPDATE CHARGES BASED ON ENERGY CHANGES ////////////////
	@Override
	public void updateVMCharges(VMstate VM) {
		updateVMEnergyCharges(VM);
		updateVMResourceCharges(VM);
		VM.setChangeTime(VM.getResourcesChangeTime());
		VM.setTotalCharges(VM.getEnergyCharges()+VM.getResourcesCharges());
	}

	public void updateVMEnergyCharges(VMstate VM){		
		double energycharges = (double) Math.round(cost.updateEnergyCharges(VM) * 1000) / 1000;
		VM.updateEnergyCharges(energycharges);
	}

	public void updateVMResourceCharges(VMstate VM){
		Calendar time = Calendar.getInstance();
		Calendar starttime = (VM.getChangeTime());
		long duration = VM.getDuration(starttime, time);
		double Resourcecharges = (double) Math.round(distribution.getDistribution(VM)*price.getPriceOnly()*duration*1000)/1000;
		VM.updateResourcesCharges(Resourcecharges);

	}
	
	/////////////////////////// GET CHARGES /////////////////////////
	@Override
	public double getTotalCharges(VMstate VM) {
		updateVMEnergyCharges(VM);
		updateVMResourceCharges(VM);
		VM.setChangeTime(VM.getResourcesChangeTime());
		return (VM.getResourcesCharges()+VM.getEnergyCharges());
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