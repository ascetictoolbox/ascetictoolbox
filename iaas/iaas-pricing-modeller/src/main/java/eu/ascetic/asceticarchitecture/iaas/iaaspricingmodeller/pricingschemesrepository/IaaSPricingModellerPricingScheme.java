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
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.energyprovider.EnergyProvider;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.types.Price;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.types.VMstate;

import java.lang.Math;

/**
 * 
 * @author E. Agiatzidou
 */

public abstract class IaaSPricingModellerPricingScheme implements IaaSPricingModellerPricingSchemeRepositoryInterface{
	
	private static final int E = 0;
	
	int scheme;
	
	IaaSPricingModellerCost cost = new IaaSPricingModellerCost();


	//////////////////////////////UPDATE THE CHARGES OF VM ////////////////////////////
	public abstract void updateVMCharges(VMstate VM);
		

	
	//public void updateVMStaticCharges(VMstate VM){
	//	cost.updateResourcesCharges(VM);
	//}
	
	

/////////////// UPDATE THE AVERAGE ENERGY PRICE ////////////////////	

///check this.....
	public double updateAverageEnergyPrice(EnergyProvider provider, Price oldAverage){
		double dur = provider.getNewDynamicEnergyPrice().getTimeOnly().difTime(oldAverage.getTimeOnly());
		double a = 1;
		System.out.println("Pr: The old average price is " + oldAverage.getPriceOnly() + "the new is " +provider.getNewDynamicEnergyPrice().getPriceOnly());
		double price = (1-Math.exp(-a*(dur))*oldAverage.getPriceOnly())+Math.exp(-a*(dur))*provider.getNewDynamicEnergyPrice().getPriceOnly();
		System.out.println("Pr: The old average price is " + oldAverage.getPriceOnly() + "the new is " +provider.getNewDynamicEnergyPrice().getPriceOnly());
		System.out.println("Pr: and the new avg " + price); 
		return price;
	}
	
	
	//////////////////GENERAL //////////////////////////////////
	public IaaSPricingModellerPricingScheme(int id){
		scheme=id;
		
	}
	
	public int getSchemeId(){
		return scheme;
	}

	
}