/*  Copyright 2015 Athens University of Economics and Business
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

package eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.types;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;



public class DynamicEnergyPrice extends Price{
	
	double oldPrice=0;
	
	public DynamicEnergyPrice() {
		Price = 0.08; //the average per kWatt per hour
		oldPrice = Price;  
	 }
	
	
	public void setPrice(double price){
		Price=price;
		timeOfPriceRequest = Calendar.getInstance();
	}
	
	public DynamicEnergyPrice changePrice(){
		oldPrice=Price;
		Price=0.08 + (Math.random()*(0.1-0.05));
		Price = (double) Math.round(Price * 1000) / 1000;
		lastPriceChange = Calendar.getInstance();
		
		return this;
	}
	
	public DynamicEnergyPrice changePriceBinary(){
		oldPrice=Price;
		if (Price == 0.08)
			Price = 0.18;

		else
			Price=0.08;
		
		lastPriceChange = Calendar.getInstance();
		return this;
	}
	
	public DynamicEnergyPrice getPrice(){
		return this;
	}
	
	
	public double getPriceOnly(){
		return this.Price;
	}
	
	public double getOldPriceOnly(){
		return this.oldPrice;
	}
	
	public long getTimeOnly(){
		return TimeUnit.MILLISECONDS.toSeconds(this.timeOfPriceRequest.getTimeInMillis());
	}
	
	
}