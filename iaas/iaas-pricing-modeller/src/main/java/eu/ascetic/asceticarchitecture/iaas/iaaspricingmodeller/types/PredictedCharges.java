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



public class PredictedCharges {
	
	double charges;
	double pricePerHour;
	
	
	public PredictedCharges() {
		charges=0.0;
		pricePerHour=0.0;
	 }
	
	
	public void setCharges(double charges){
		this.charges = (double) Math.round(charges * 1000) / 1000;
		
	}
	
	public PredictedCharges getCharges(){
		return this;
	}
	
	
	public double getChargesOnly(){
		return this.charges;
	}
	
	public double getPriceOnly(){
		return pricePerHour;
	}
	
	public void setPrice(double price){
		pricePerHour=price;
	}
	
	public void setCharges(PredictedCharges one){
		this.charges = one.getChargesOnly();
		this.pricePerHour = one.getPriceOnly();
	}
	
	
}