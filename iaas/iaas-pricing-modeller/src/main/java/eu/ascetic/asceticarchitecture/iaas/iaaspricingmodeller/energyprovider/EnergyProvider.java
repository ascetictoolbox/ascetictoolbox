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


package eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.energyprovider;

import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.IaaSPricingModeller;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.types.*;

import java.util.Timer;
import java.util.concurrent.TimeUnit;

/**
 * 
 * @author E. Agiatzidou
 */

public class EnergyProvider implements EnergyProviderInterface{
	 
	private static int idEP=0;

	IaaSPricingModeller iaasprovider;
	
	Price dynamicEnergyPriceOld = new DynamicEnergyPrice();
	
	Price dynamicEnergyPriceNew = new DynamicEnergyPrice();
	
	Price staticEnergyPrice = new StaticEnergyPrice();
	
	Timer timer;
	
	long delay = 15;
	
	public EnergyProvider(IaaSPricingModeller iaasprovider){
		idEP=idEP+1;
		this.iaasprovider = iaasprovider;
		timer = new Timer (true);
		timer.scheduleAtFixedRate(new EnergyPriceSetter(this), TimeUnit.SECONDS.toMillis(delay), TimeUnit.SECONDS.toMillis(20));
	}
	
	public EnergyProvider(){
		idEP=idEP+1;
		timer = new Timer (true);
		timer.scheduleAtFixedRate(new EnergyPriceSetter(this), TimeUnit.SECONDS.toMillis(delay), TimeUnit.SECONDS.toMillis(5));
	}
	
	public int getId(){
		return idEP;
	}
	
	public void setStaticEnergyPrice(double price){
		staticEnergyPrice.setPrice(price);
	}
	
	public StaticEnergyPrice getStaticEnergyPrice(){
		return (StaticEnergyPrice) staticEnergyPrice;
	}
	
	public DynamicEnergyPrice getNewDynamicEnergyPrice(){
		return (DynamicEnergyPrice) dynamicEnergyPriceNew;
	}
	
	public DynamicEnergyPrice getOldDynamicEnergyPrice(){
		return (DynamicEnergyPrice) dynamicEnergyPriceOld;
	}
	public Price getCurrentEnergyPrice(int scheme){
		if (scheme==0 || scheme ==1)
			return staticEnergyPrice;
		else 
			return dynamicEnergyPriceNew;
		
	}

	public void updateDynamicEnergyPrice(DynamicEnergyPrice price){
		dynamicEnergyPriceNew.setPrice(price);
		iaasprovider.getBilling().updateVMCharges(dynamicEnergyPriceOld);
	}
	
	
	
}