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



public abstract class Price {
	
	double Price;
	
	
	Calendar lastPriceChange;
	
	Calendar timeOfPriceRequest; 
	 
	
	public Price() {
		Price = 0;
		timeOfPriceRequest = Calendar.getInstance();
		lastPriceChange=timeOfPriceRequest;
	 }
	
	
	public void setPrice(Price price){
		Price=price.getPriceOnly();
		lastPriceChange=price.getChangeTimeOnly();
	}
	
	public void setPrice(double price){
		Price=price;
		lastPriceChange=Calendar.getInstance();
	}
	
	
	public Price getPrice(){
		return this;
	}
	
	
	public double getPriceOnly(){
		return this.Price;
	}
	
	public Calendar getChangeTimeOnly(){
		return lastPriceChange;
	}
	
	public void changeTime(){
		lastPriceChange = Calendar.getInstance();
	}
}