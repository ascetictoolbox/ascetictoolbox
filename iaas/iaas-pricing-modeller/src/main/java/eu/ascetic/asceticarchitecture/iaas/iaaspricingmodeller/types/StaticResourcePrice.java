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



public class StaticResourcePrice extends Price{
	
	public StaticResourcePrice(int IaaSID, int scheme) {
		int even= IaaSID%2;
		if (scheme == 0){
			if (even == 0){
				Price = 1;}
			else{
				Price = 1.20;
			}
		}
		else {
			if (even == 0){
				Price = 0.5;}
			else{
				Price = 0.6;
			}
		}
	 }
	
	
	public StaticResourcePrice() {
		Price = 0.14; //per hour all included based on Amazon m3 large 2CPU 7.5GB memory 32 SSD

	 }
	
	public StaticResourcePrice(double price) {
		Price = price; //per hour all included based on Amazon m3 large 2CPU 15GB memory 2*32 SSD
	 }
	
	
	public void setPrice(double price){
		Price=price;
		timeOfPriceRequest = Calendar.getInstance();
	}
	
	public StaticResourcePrice getPrice(){
		return this;
	}
	
	
	public double getPriceOnly(){
		return this.Price;
	}
	
	public double getPriceSecOnly(double price){
		if (price == 1){
			//	System.out.println("here1");
				return 0.00028;}
			if (price == 1.20)
				return 0.00034;
			if (price == 0.5){
			//	System.out.println("her2e");
				return 0.00014;}
			if (price == 0.6){
			//	System.out.println("here3");
				return 0.00017;}
			return 0.00028;
	}
}