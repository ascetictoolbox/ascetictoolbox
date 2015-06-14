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



public class StaticResourcePrice extends Price{
	
	double Price;
	
	
	Time lastPriceChange;
	
	Time timeOfPriceRequest; 
	 
	
	public StaticResourcePrice() {
		Price = 0.14; //per hour all included based on Amazon m3 large 2CPU 15GB memory 2*32 SSD
		timeOfPriceRequest = new Time();
		lastPriceChange=timeOfPriceRequest;
	 }
	
	public StaticResourcePrice(double price) {
		Price = price; //per hour all included based on Amazon m3 large 2CPU 15GB memory 2*32 SSD
		timeOfPriceRequest = new Time();
		lastPriceChange=timeOfPriceRequest;
	 }
	
	public void setPrice(double price){
		Price=price;
		timeOfPriceRequest = new Time();
	}
	
	public StaticResourcePrice getPrice(){
		return this;
	}
	
	
	public double getPriceOnly(){
		return this.Price;
	}
	
	public Time getTimeOnly(){
		return this.timeOfPriceRequest;
	}
	
	public void changeTime(){
		lastPriceChange = new Time();
	}
}