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



public class Charges {
	
	double charges;
	
	//Price price;
	
	Time lastChargeTime;
	
	public Charges() {
		charges=0.0;
	 }
	
	
	public void setCharges(double charges){
		this.charges = charges;
		lastChargeTime = new Time();
	}
	
	public Charges getCharges(){
		return this;
	}
	
	
	public double getChargesOnly(){
		return this.charges;
	}
	
	public Time getTimeOnly(){
		return this.lastChargeTime;
	}
	
	public void changeTime(){
		lastChargeTime = new Time();
	}
	
	public Charges addCharges(Charges one, Charges two){
		charges = one.getChargesOnly()+two.getChargesOnly();
		lastChargeTime = new Time();
		return this;
	}
	
	public void updateCharges(double charges){
		this.charges = this.charges + charges;
		lastChargeTime = new Time();
	}
	
	public void setTime(Time time){
		lastChargeTime = time;
	}
}