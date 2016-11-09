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
	
	String IaaSID;
	
	ResourceDistribution distribution= new ResourceDistribution();
	
	double staticResourcePrice;
	
	double resourcePrice;
	
	double averageEnergyPrice;
	
	double EnergyPrice;
	double oldEnergyPrice = 0.18;
	
	public IaaSProvider(String id) {
		
		IaaSID = id;
		distribution.setDistribution(0.004984, 0.009968, 0.0002759); 
		//int IaaSid = Integer.parseInt(IaaSID.trim());
		
		//int even= IaaSid%2;
		//if (even == 0){
		if (IaaSID.contains("0")){
			staticResourcePrice = 1;}
			else{
				staticResourcePrice = 1.20;
			}
	
			if (IaaSID.contains("0")){
				resourcePrice = 0.5;}
			else{
				resourcePrice = 0.6;
			}
	
		
		averageEnergyPrice = 0.15; //per kwat per hour
		EnergyPrice = 0.08;
	}
	
	public double getResoucePrice(){
		//System.out.println(" resource price = " + resourcePrice);
		return resourcePrice;
	}
	
	//TESTED
	public double getStaticResoucePrice(){
		//System.out.println("IaaS Provider static resource price = " + staticResourcePrice);
		return staticResourcePrice;
	}
	
	public double getPriceSec(double price){
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
	
	public double getAverageEnergyPrice(){
		return averageEnergyPrice;
	}
	
	//TESTED
	public ResourceDistribution getDistribution(){
	//	System.out.println("IaaS Provider: the distribution "+distribution.cpuPer);
		return distribution;
	}
	
	public double getCurrentEnergyPrice(){
		return EnergyPrice;
	}
	
	public String getID(){
		return IaaSID;
	}

	//TESTED
	public double predictResourcesCharges(VMinfo vm, double duration, double price) {
		Charges b = new Charges();
	//	System.out.print("here5");
		//b.setCharges((distribution.getDistribution(vm)*price*(Math.ceil(duration/3600))));
		b.setCharges((distribution.getDistribution(vm)*price*duration));
		return b.getChargesOnly();
	}
	
	
	public double predictEnergyCharges(double energy, double average){
		Charges charges = new Charges();
		charges.setCharges((energy/1000)*average);
		return charges.getChargesOnly();
	}

	public double getEnergyPriceForBilling(){
		return oldEnergyPrice;
	}
	
	public void setEnergyPrice(double price){
		oldEnergyPrice = EnergyPrice;
		this.EnergyPrice=price;
	}
	
	
}