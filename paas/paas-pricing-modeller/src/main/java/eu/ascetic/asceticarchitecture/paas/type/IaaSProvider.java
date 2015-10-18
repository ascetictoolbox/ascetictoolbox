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




public class IaaSProvider {
	
	int IaaSID;
	
	ResourceDistribution distribution= new ResourceDistribution();
	
	double staticResourcePrice;
	
	double resourcePrice;
	
	double averageEnergyPrice;
	
	public IaaSProvider(int id) {
		
		IaaSID = id;
		distribution.setDistribution(0.6, 0.3, 0.1); 
		staticResourcePrice = 0.14;
		resourcePrice = 0.1;
		averageEnergyPrice = 0.007;
	}
	
	public double getResoucePrice(){
		return resourcePrice;
	}
	
	public double getStaticResoucePrice(){
		return staticResourcePrice;
	}
	
	public double getAverageEnergyPrice(){
		return averageEnergyPrice;
	}
	
	public ResourceDistribution getDistribution(){
		return distribution;
	}
	
	
	
	public double predictResourcesCharges(VMinfo vm, long duration, double price) {
		Charges b = new Charges();
		b.setCharges((distribution.getDistribution(vm)*price*(Math.ceil(duration/3600))));
		return b.getChargesOnly();
	}
	
	
	public double predictEnergyCharges(double energy, double average){
		Charges charges = new Charges();
		charges.setCharges(energy*average);
		return charges.getChargesOnly();
	}
}