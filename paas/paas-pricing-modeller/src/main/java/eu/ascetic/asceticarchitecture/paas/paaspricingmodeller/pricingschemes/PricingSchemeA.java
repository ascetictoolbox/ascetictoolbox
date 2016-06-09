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


package eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.pricingschemes;

import eu.ascetic.asceticarchitecture.paas.type.Charges;
import eu.ascetic.asceticarchitecture.paas.type.DeploymentInfo;
import eu.ascetic.asceticarchitecture.paas.type.Price;
import eu.ascetic.asceticarchitecture.paas.type.ResourceDistribution;
import eu.ascetic.asceticarchitecture.paas.type.Time;
import eu.ascetic.asceticarchitecture.paas.type.VMinfo;



/**
 * This pricing scheme has ID==0. 
 * It gives only a static prices per hour. 
 * @author E. Agiatzidou
 */



public class PricingSchemeA extends PaaSPricingModellerPricingScheme {
	
Price price;
ResourceDistribution distribution = new ResourceDistribution();
	
	
	public PricingSchemeA(int id) {
		super(id);

	}

	public Price getPrice(){
		return price;
	}
	
	/////////////////////////PREDICTION/////////////////////////
	@Override 
	public double predictTotalCharges(VMinfo vm) {
		Charges b = predictResourcesCharges(vm, getResourcePrice(vm));
		//double temp = (double) Math.round(b.getChargesOnly()*1000)/1000; 
		double temp = b.getChargesOnly();
		return temp;
	}
	


////////////////////////////////// BILLING //////////////////////////
	
	
	@Override
	public double getTotalCharges(VMinfo VM) {
		VM.setChangeTime();
		updateVMResourceCharges(VM, getResourcePrice(VM), getDistribution(VM));
		VM.setTotalCharges(VM.getResourcesCharges());
		return (VM.getTotalCharges());
		
	}
	
	private double getResourcePrice(VMinfo VM){
		return VM.getIaaSProvider().getStaticResoucePrice();
	}
	
	private ResourceDistribution getDistribution(VMinfo VM){
		return VM.getIaaSProvider().getDistribution();
	}
	
}