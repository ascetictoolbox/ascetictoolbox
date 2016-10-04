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
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.queue.GenericPricingMessage;
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

	Boolean chargesUpdated = false;
	Price dynamicEnergyPrice = new DynamicEnergyPrice();
	
	Price staticEnergyPrice = new StaticEnergyPrice();
	
	Timer timer;
	
	long delay = 10;
	
	
	public EnergyProvider(IaaSPricingModeller iaasprovider){
		idEP=idEP+1;
		this.iaasprovider = iaasprovider;
		timer = new Timer (true);
		
		//timer.scheduleAtFixedRate(new EnergyPriceSetter(this), TimeUnit.SECONDS.toMillis(delay), TimeUnit.HOURS.toMillis(1));
		//timer.scheduleAtFixedRate(new EnergyPriceSetter(this), TimeUnit.SECONDS.toMillis(delay), 40000);
	}
	
	public EnergyProvider(){
		idEP=idEP+1;
		//timer = new Timer (true);
		//timer.scheduleAtFixedRate(new EnergyPriceSetter(this), TimeUnit.SECONDS.toMillis(delay), TimeUnit.HOURS.toMillis(12));
	}
	
	public int getId(){
		return idEP;
	}
	
	public void setStaticEnergyPrice(double price){
		staticEnergyPrice.setPrice(price);
	}
	
	public void setFlagForChargesUpdated(Boolean value){
		chargesUpdated = value;
	}
	
	public Boolean getFlagForChargesUpdated(){
		return chargesUpdated;
	}
	
	public StaticEnergyPrice getStaticEnergyPrice(){
		return (StaticEnergyPrice) staticEnergyPrice;
	}
	
	public DynamicEnergyPrice getNewDynamicEnergyPrice(){
		return (DynamicEnergyPrice) dynamicEnergyPrice;
	}
	
	public DynamicEnergyPrice getOldDynamicEnergyPrice(){
		return (DynamicEnergyPrice) dynamicEnergyPriceOld;
	}
	public Price getCurrentEnergyPrice(int scheme){
		if (scheme==0 || scheme ==1)
			return staticEnergyPrice;
		else 
			return dynamicEnergyPrice;
		
	}

	public void updateEnergyPrice(){
		DynamicEnergyPrice price = new DynamicEnergyPrice();
		price.changePriceBinary();
		
		try {

			updateDynamicEnergyPrice(price);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		

	public void updateDynamicEnergyPrice(DynamicEnergyPrice price) throws Exception{
	//	System.out.println("Energy Provider: Energy Price has changed to "+price.getPriceOnly());
		dynamicEnergyPrice.setPrice(price);
		dynamicEnergyPriceOld.setPrice(price.getOldPriceOnly());
		GenericPricingMessage msg = new GenericPricingMessage(iaasprovider.getIaaSProviderID(), price.getPriceOnly());
		
		iaasprovider.publishToQueue(msg);
		
		chargesUpdated = false;
		iaasprovider.getBilling().updateVMCharges(dynamicEnergyPriceOld);
	}
	
}