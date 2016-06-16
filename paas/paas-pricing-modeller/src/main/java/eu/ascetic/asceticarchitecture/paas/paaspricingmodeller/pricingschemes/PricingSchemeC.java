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

import java.util.Calendar;

import eu.ascetic.asceticarchitecture.paas.type.ResourceDistribution;
import eu.ascetic.asceticarchitecture.paas.type.VMinfo;



/**
 * This pricing scheme has ID==0. 
 * It gives only a static prices per hour. 
 * @author E. Agiatzidou
 */



public class PricingSchemeC extends PaaSPricingModellerPricingScheme  {
	
	double price;
	
	ResourceDistribution distribution = new ResourceDistribution();

	
	public PricingSchemeC(int id) {
		super(id);
		
		
	}
	private double getResourcePrice(VMinfo VM){
		return VM.getIaaSProvider().getResoucePrice();
	}


	private ResourceDistribution getDistribution(VMinfo VM){
		return VM.getIaaSProvider().getDistribution();
	}
	
	/////////////////////////PREDICTION/////////////////////////
	/*
	@Override
	public double predictCharges(VMstate vm, Price average) {
		Charges b = predictResourcesCharges(vm, price);
		double temp = (double) Math.round(b.getChargesOnly()*1000)/1000;
		return temp;
	}
*/	


////////////////////////////////// BILLING //////////////////////////
	
	
	@Override
	public double getTotalCharges(VMinfo VM) {
		VM.setChangeTime();
		updateVMResourceCharges(VM, getResourcePrice(VM), getDistribution(VM));
		double reduction = 0;
		double difference = 0;
		/*
		double predEner = VM.getPredictedInformation().getPredictedPowerPerHour();
		
		long duration = VM.getTotalDuration();
		
		double realEner = cost.updateEnergy(VM);
		
		double predictedTotalEnergy = duration*predEner;

		if (predictedTotalEnergy>realEner){
				 difference = (predictedTotalEnergy - realEner);
				 
			}
		reduction = (difference*100)/predictedTotalEnergy;
		
		double newCharges = VM.getResourcesCharges()-VM.getResourcesCharges()*reduction/100;
		
		VM.setTotalCharges(newCharges);
		*/
		return (VM.getTotalCharges());
		
	}
	
	
}