/**
 *  Copyright 2015 Athens University of Economics and Business
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


package eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.pricingschemes;

import eu.ascetic.asceticarchitecture.paas.type.Charges;
import eu.ascetic.asceticarchitecture.paas.type.Price;
import eu.ascetic.asceticarchitecture.paas.type.ResourceDistribution;
import eu.ascetic.asceticarchitecture.paas.type.VMinfo;

/**
 * 
 * @author E. Agiatzidou
 */

public abstract class PaaSPricingModellerPricingScheme {
		
	int scheme;
	ResourceDistribution distribution = new ResourceDistribution();
	
	public PaaSPricingModellerPricingScheme(int id){
		scheme=id;
	//	distribution.setDistribution(0.6, 0.3, 0.1); 	
	}


	/**
	 * This function calculated the total charges of a VM based on total duration in hours. 
	 * It is like the current methods. Rounds up to the next hour.
	 * @param vm
	 * @return the total charges of a VM depending on the total hours that it will run
	 */
	
	public Charges predictResourcesCharges(VMinfo vm, double price) {
		Charges b = new Charges();
		b.setCharges(vm.getChangeTime(), distribution.getDistribution(vm)*price*(Math.ceil(vm.getActualDuration()/3600)));
		return b;
	}
	
	
	public Charges predictEnergyCharges(VMinfo VM, double average){
		Charges charges = new Charges();
		charges.setCharges(VM.getChangeTime(), VM.getEnergyPredicted()*average);
		return charges;
	}
	

	
	public double predictTotalCharges(VMinfo vm){
		return 0;
	}
		
	
	public double updateAverageEnergyPrice(){
		double price =0.007;
		return price;
		
	}
	
	
	public void updateVMResourceCharges(VMinfo VM, double price, ResourceDistribution distribution){
		long duration = VM.getDuration(VM.getResourcesChargesAll().getTime(), VM.getChangeTime());
		long totalDuration = VM.getDuration(VM.getStartTime(),VM.getChangeTime());
	//	System.out.println("Last change time is: "+ VM.getResourcesChargesAll().getTime().getTimeInMillis()+" Now is: "+VM.getChangeTime().getTimeInMillis()+" and the diff is "+duration);
	//	System.out.println("Total duration is: "+totalDuration);
		VM.setDuration(duration);
	//	System.out.println("the resource charges are calculated with this price " + price);
	//	System.out.println("and the distribution is " + distribution.getDistribution(VM));
		double Resourcecharges = distribution.getDistribution(VM)*price;
		double count = Resourcecharges*(duration);
		VM.updateResourcesCharges(count);
	//	System.out.println("Resource charges= " + count);
	//	System.out.println("Total Resource charges= " + VM.getResourcesCharges());
		}
	
	public void updateVMEnergyCharges(VMinfo VM){	
		VM.setChangeTime();
		double energycharges = updateEnergyCharges(VM);
		long duration = VM.getDuration(VM.getEnergyChargesAll().getTime(), VM.getChangeTime());
	//	System.out.println("Last change time is: "+ VM.getEnergyChargesAll().getTime().getTimeInMillis()+" Now is: "+VM.getChangeTime().getTimeInMillis()+" and the diff is "+duration);
	//	System.out.println("Updating energy charges to " + energycharges);
		VM.updateEnergyCharges(energycharges);
	//	System.out.println("Total energy charges " + VM.getEnergyCharges());
		
	}
	
	public double updateEnergyCharges(VMinfo VM){
		//the energy charges for the past period
		
		double price = getEnergyPrice(VM);
        double energyCharges = updateEnergy(VM) * price;
      //  System.out.println("I am updating energy with this price "+getEnergyPrice(VM));
        return energyCharges;
		
	}
	
	
	 private double getEnergyPrice(VMinfo vM) {
		 return vM.getIaaSProvider().getEnergyPriceForBilling();

	}


	public double updateEnergy(VMinfo VM){
	        double difference=100;
	        difference = VM.getEnergyConsumptionofLastPeriod();
	        return difference;
	    }
 
	
	 
	public void updateVMCharges(VMinfo VM){
		
	}
	//////////////////GENERAL //////////////////////////////////
	
	
	public int getSchemeId(){
		return scheme;
	}


	public double getTotalCharges(VMinfo VM) {
		// TODO Auto-generated method stub
		return 0;
	}

	
}