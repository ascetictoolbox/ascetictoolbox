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

import org.codehaus.jackson.map.introspect.BasicClassIntrospector.GetterMethodFilter;




public class EnergyPrediction {
	
	double totalEnergyPredicted;
	double avrgEnergyPredicted;
	
	public EnergyPrediction() {
	 }
	
	public void setTotalEnergy(double energy){
		totalEnergyPredicted=energy;
	}
	
	public void setAvergPower(double power){
		avrgEnergyPredicted=power;
	}
	
	public double getTotalEnergy(){
		return totalEnergyPredicted;
	}
	
	public double getAvrgPower(){
		return avrgEnergyPredicted;
	}
}