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



public class Charges {
	
	double charges;

	
	TimeParameters time;
	
	public Charges() {
		charges=0.0;
		time = new TimeParameters();
	 }
	
	
	public void setCharges(double charges){
		this.charges = (double) Math.round(charges * 1000) / 1000;
		time.setLastChangeTime();
	}	
	
	public double getChargesOnly(){
		return this.charges;
	}
	
	public long getTimeOnly(){
		return time.getLastChangeTimeinSec();
	}
	
	public void changeTime(){
		time.setLastChangeTime();
	}
	
	public Charges addCharges(Charges one, Charges two){
		charges = one.getChargesOnly()+two.getChargesOnly();
		time.setLastChangeTime();
		return this;
	}
	
	public void updateCharges(double charges){
		this.charges = this.charges + charges;
		time.setLastChangeTime();
	}
	
	public void setTime(Calendar time){
		this.time.setLastChangeTime(time);
	}
}