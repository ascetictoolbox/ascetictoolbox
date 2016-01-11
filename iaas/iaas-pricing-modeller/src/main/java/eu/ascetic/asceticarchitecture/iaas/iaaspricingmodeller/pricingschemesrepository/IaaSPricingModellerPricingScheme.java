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

import eu.ascetic.asceticarchitecture.iaas.energymodeller.EnergyModeller;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.cost.IaaSPricingModellerCost;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.energyprovider.EnergyProvider;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.types.Charges;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.types.Price;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.types.ResourceDistribution;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.types.TimeParameters;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.types.VMstate;

import java.lang.Math;
import java.util.Calendar;

/**
 * 
 * @author E. Agiatzidou
 */

public abstract class IaaSPricingModellerPricingScheme implements IaaSPricingModellerPricingSchemeRepositoryInterface{


	private EnergyModeller EnergyModeller = null;
	
	int scheme;
	

	
	ResourceDistribution distribution = new ResourceDistribution();
	
	IaaSPricingModellerCost cost = new IaaSPricingModellerCost(EnergyModeller);
	
	public IaaSPricingModellerPricingScheme(int id){
		scheme=id;
		distribution.setDistribution(0.6, 0.3, 0.1); 	
	}
	
	public void setEnergyModeller(EnergyModeller energyModeller){
		EnergyModeller = energyModeller;
	
	}
	
	/**
	 * This function calculated the total charges of a VM based on total duration in hours. 
	 * It is like the current methods. Rounds up to the next hour.
	 * @param vm
	 * @return the total charges of a VM depending on the total hours that it will run
	 */
	public Charges predictResourcesCharges(VMstate vm, Price price) {
		Charges b = new Charges();
		b.setCharges(vm.getChangeTime(), distribution.getDistribution(vm)*price.getPriceOnly()*(Math.ceil(vm.getPredictedInformation().getPredictedDuration()/3600)));
		return b;
	}
	
	
	public Charges predictEnergyCharges(VMstate VM, Price average){
		Charges charges = new Charges();
		charges.setCharges(VM.getChangeTime(), VM.getPredictedInformation().getTotalPredictedEnergy()*average.getPriceOnly());
		return charges;
	}

/////////////// UPDATE THE AVERAGE ENERGY PRICE ////////////////////	
	public double updateAverageEnergyPrice(EnergyProvider provider, Price oldAverage){
		/**
		Calendar startTime = oldAverage.getChangeTimeOnly();
		Calendar endTime = provider.getNewDynamicEnergyPrice().getChangeTimeOnly();
		TimeParameters temp = new TimeParameters(startTime, endTime);
		long dur = temp.getTotalDuration();
		double a = 1;
		double b =Math.exp(-a*(dur/3600));
		double price = ((1-b)*oldAverage.getPriceOnly())+b*provider.getNewDynamicEnergyPrice().getPriceOnly();
		**/
		double price =0.007;
		return price;
		
	}
	
	
	public void updateVMResourceCharges(VMstate VM, Price price){
		long duration = VM.getDuration(VM.getResourcesChargesAll().getTime(), VM.getChangeTime());
		long totalDuration = VM.getDuration(VM.getStartTime(),VM.getChangeTime());
		//System.out.println("Last change time is: "+ VM.getResourcesChargesAll().getTime().getTimeInMillis()+"Now is: "+VM.getChangeTime().getTimeInMillis()+"and the diff is "+duration);
		VM.setDuration(duration);
		double Resourcecharges = distribution.getDistribution(VM)*price.getPriceOnly();
		
		//for changes during operation. To recheck
		/*
		if (VM.getResourcesCharges()!=Resourcecharges){
			if (totalDuration>3600){
				VM.setHours((int)(Math.ceil(totalDuration)/3600));
			}
				
			if (VM.getHoursCounter()<VM.getHours()){
				VM.updateResourcesCharges(Resourcecharges*(VM.getHours()-VM.getHoursCounter()));
				VM.setHoursCounter();
			}
			VM.setHours(1);
			VM.resetHoursCounter();
		}
		*/
		if (totalDuration>3600){
			VM.setHours((int)(Math.ceil(totalDuration)/3600));
		}
			
		if (VM.getHoursCounter()<VM.getHours()){
			VM.updateResourcesCharges(Resourcecharges*(VM.getHours()-VM.getHoursCounter()));
			VM.setHoursCounter();
		}
		
		
		
		//double Resourcecharges = (double) Math.round(distribution.getDistribution(VM)*price.getPriceOnly()*Math.ceil(duration)*1000)/1000;
         //  Resourcecharges = distribution.getDistribution(VM)*price.getPriceOnly()*Math.ceil(duration/3600);
		
		
		}
	
	public void updateVMEnergyCharges(VMstate VM){		
		//double energycharges = (double) Math.round(cost.updateEnergyCharges(VM) * 1000) / 1000;
		double energycharges = cost.updateEnergyCharges(VM);
		VM.updateEnergyCharges(energycharges);
		
	}
	
	
	public void updateVMCharges(VMstate VM){
		
	}
	//////////////////GENERAL //////////////////////////////////
	
	
	public int getSchemeId(){
		return scheme;
	}

	public Price getResourcePrice() {
		// TODO Auto-generated method stub
		return null;
	}

	
}