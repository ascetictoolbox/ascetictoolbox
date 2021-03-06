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

package eu.ascetic.asceticarchitecture.paas.type;

import java.util.Calendar;

public class Charges {
double charges;

	
	Calendar lastChange;
	
	public Charges(Calendar time) {
		charges=0.0;
		lastChange=time;
	 }
	
	
	public Charges() {
		charges=0.0;
		lastChange=Calendar.getInstance();
	}


	public void setCharges(Calendar time, double charges){
		this.charges = (double) Math.round(charges * 1000) / 1000;
		lastChange=time;
	}	
	
	public double getChargesOnly(){
		return this.charges;
	}
	
	public long getTimeOnly(){
		return lastChange.getTimeInMillis();
	}
	
	public Calendar getTime(){
		return lastChange;
	}
	
	/*public void changeTime(){
		time.setEndTime();
	}*/
	
	public Charges addCharges(Calendar time, Charges one, Charges two){
		charges = one.getChargesOnly()+two.getChargesOnly();
		lastChange=time;
		return this;
	}
	
	public void updateCharges(Calendar time, double charges){
		this.charges =this.charges + charges;
		lastChange=time;
		
	}
	
	public void setTime(){
		lastChange=Calendar.getInstance();
	}
	

	
	
	
	public void setCharges(double charges){
		this.charges = charges;
		lastChange = Calendar.getInstance();
	}

		
	
	
	public Charges getCharges(){
		return this;
	}
	
	
	
	
	public Charges addCharges(Charges one, Charges two){
		charges = one.getChargesOnly()+two.getChargesOnly();
		lastChange =Calendar.getInstance();
		return this;
	}
	
	
	
	public void updateCharges(double charges){
		this.charges = this.charges + charges;
		lastChange =Calendar.getInstance();
	}
	
	
	
	
}