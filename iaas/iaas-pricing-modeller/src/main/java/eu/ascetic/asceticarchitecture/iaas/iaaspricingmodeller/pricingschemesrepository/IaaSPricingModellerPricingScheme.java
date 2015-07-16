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
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.types.Price;
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
	
	
	IaaSPricingModellerCost cost = new IaaSPricingModellerCost(EnergyModeller);
	
	public void setEnergyModeller(EnergyModeller energyModeller){
		EnergyModeller = energyModeller;
	}

	//////////////////////////////UPDATE THE CHARGES OF VM ////////////////////////////
	public abstract void updateVMCharges(VMstate VM);
		

	
	//public void updateVMStaticCharges(VMstate VM){
	//	cost.updateResourcesCharges(VM);
	//}
	
	

/////////////// UPDATE THE AVERAGE ENERGY PRICE ////////////////////	
	public double updateAverageEnergyPrice(EnergyProvider provider, Price oldAverage){
		Calendar startTime = oldAverage.getChangeTimeOnly();
		Calendar endTime = provider.getNewDynamicEnergyPrice().getChangeTimeOnly();
		TimeParameters temp = new TimeParameters(startTime, endTime);
		long dur = temp.getTotalDuration();
		double a = 1;
		double b =Math.exp(-a*(dur/3600));
		double price = ((1-b)*oldAverage.getPriceOnly())+b*provider.getNewDynamicEnergyPrice().getPriceOnly();
		return price;
		
	}
	
	
	//////////////////GENERAL //////////////////////////////////
	public IaaSPricingModellerPricingScheme(int id){
		scheme=id;
		
	}
	
	public int getSchemeId(){
		return scheme;
	}

	public Price getResourcePrice() {
		// TODO Auto-generated method stub
		return null;
	}

	
}