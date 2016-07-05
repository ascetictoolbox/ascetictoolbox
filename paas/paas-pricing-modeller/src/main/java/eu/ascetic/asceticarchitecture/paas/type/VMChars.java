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








import java.util.HashMap;

import eu.ascetic.asceticarchitecture.paas.type.Charges;
import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.pricingschemes.PaaSPricingModellerPricingScheme;
import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.pricingschemes.PricingSchemeA;
import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.pricingschemes.PricingSchemeB;
import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.pricingschemes.PricingSchemeC;


public class VMChars {

	double RAM;
	double CPU;
	double storage;
	
	Charges totalChargesForThisVMCharacteristics;
	
	public VMChars(double RAM, double CPU, double storage, Charges totalCharges){
		this.RAM = RAM;
		this.CPU = CPU;
		this.storage = storage;
		totalChargesForThisVMCharacteristics = totalCharges;
	}
	
	public double getRAM(){
		return RAM;
	}

			
	public double getCPU(){
		return CPU;
	}
	
	public double getStorage(){
		return storage;
	}
	
	public String getVMCharacteristics(){
		String toPrint = "RAM: " + RAM + "CPU: " + CPU + "Storage: " + storage;
	    return toPrint;
	}
	
	public void setTotalCharges(Charges totalCharges){
		totalChargesForThisVMCharacteristics = totalCharges;		
	}

}