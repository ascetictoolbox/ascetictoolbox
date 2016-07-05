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

import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.billing.PaaSPricingModellerRegistration;
import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.queue.client.GenericPricingMessage.Unit;
import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.rest.EMInteraction;
import eu.ascetic.asceticarchitecture.paas.type.Charges;
import eu.ascetic.asceticarchitecture.paas.type.Price;
import eu.ascetic.asceticarchitecture.paas.type.ResourceDistribution;
import eu.ascetic.asceticarchitecture.paas.type.VMinfo;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.log4j.Logger;

/**
 * 
 * @author E. Agiatzidou
 */

public abstract class PaaSPricingModellerPricingScheme {
		
	int scheme;
	//ResourceDistribution distribution = new ResourceDistribution();
	static Logger logger = Logger.getLogger(PaaSPricingModellerPricingScheme.class);
	public PaaSPricingModellerPricingScheme(int id){
		scheme=id;
	//	distribution.setDistribution(0.6, 0.3, 0.1); 	
	}



	/**
	 * This function calculated the total charges of a VM based on total duration in hours. 
	 * It is like the current methods. Rounds up to the next hour.
	 * @param vm
	 * @return the total charges of a VM depending on the total hours that it will run
	 */////////////
	
	public Charges predictResourcesCharges(VMinfo vm, ResourceDistribution distribution, double price) {
		Charges b = new Charges();
		
		double temp = distribution.getDistribution(vm)*price*vm.getPredictedDuration();
		b.setCharges(vm.getChangeTime(), temp);
		//System.out.println("Resource charges " + b.getChargesOnly());
		return b;
	}
	
	public double predictResourcePrice (VMinfo VM, double price, ResourceDistribution distribution, double duration){
		double Resourcecharges = distribution.getDistribution(VM)*price;
		double count = Resourcecharges*(duration);
	//	System.out.println("predictResourcePrice: " + count);
		return (double) Math.round(count*1000) / 1000;
	}
	
	public double predictEnergyPrice (VMinfo VM, double duration){
		double price = getEnergyPrice(VM);
		 double energyCharges = getPredictedEnergy(VM, duration) * price;
	//	 System.out.println("predictResourcePrice: " + energyCharges);
		//double energycharges = (energy/1000)*price; 
		return (double) Math.round(energyCharges * 1000) / 1000;
	}
	
	private double getPredictedEnergy(VMinfo VM, double duration) {
		 	double difference=3000;
	        try{
	        	EMInteraction response = new EMInteraction();
	        	double energy = response.getPredictedEnergyofVM(VM.getAppID(), Integer.toString(VM.getDepID()), Integer.toString(VM.getVMid()), Double.toString(duration));
	        	VM.updateEnergyConsumption(energy/1000);
	        	return energy/1000;
	        }
	       catch (Exception ex){
			//	System.out.println("Pricing Modeller getPredictedEnergy: Could receive asnwer");
				logger.info("Could not send the message to queue");
			}
	      //  if (VM.getEnergyConsumptionofLastPeriod()!=0){
	    //    	difference= VM.getEnergyConsumptionofLastPeriod();
	     //   }
	     //   System.out.println("Pricing scheme: I am updating energy difference"+difference);
	        return difference;

	}


	public Charges predictEnergyCharges(VMinfo VM, double average, boolean energySet){
		Charges charges = new Charges();
		
		if (!energySet){
			
		VM.setEnergyPredicted(getPredictedEnergy(VM, VM.getPredictedDuration()));
		}
		charges.setCharges(VM.getChangeTime(), VM.getEnergyPredicted()*average);
	//	System.out.println("Energy charges " + charges);
		return charges;
	}
	
	public double updateAverageEnergyPrice(){
		double price =0.007;
		return price;
		
	}
	
	
	public void updateVMResourceCharges(VMinfo VM, double price, ResourceDistribution distribution){
		Unit unit = Unit.CHARGES;
		long duration = VM.getDuration(VM.getResourcesChargesAll().getTime(), VM.getChangeTime());
		long totalDuration = VM.getDuration(VM.getStartTime(),VM.getChangeTime());
	//	System.out.println("Pricing Scheme Updating Resource: VMid =" + VM.getVMid()+" Last change time is: "+ VM.getResourcesChargesAll().getTime().getTimeInMillis()+" Now is: "+VM.getChangeTime().getTimeInMillis()+" and the diff is "+duration);
	//	System.out.println("Pricing Scheme Updating Resource: VMid =" + VM.getVMid()+" Total duration is: "+totalDuration);
		VM.setCurrentDuration(duration);
		VM.setTotalDuration(totalDuration);
	//	System.out.println("Pricing Scheme Updating Resource: VMid =" + VM.getVMid()+"the resource charges are calculated with this price " + price);
	//	System.out.println("Pricing Scheme Updating Resource: VMid =" + VM.getVMid()+"and the distribution is " + distribution.getDistribution(VM));
		double Resourcecharges = distribution.getDistribution(VM)*price;
		double count = (double) Math.round((Resourcecharges*(duration)) * 1000) / 1000;
		VM.updateResourcesCharges(count);
		VM.setCurrentCharges(count);
		
	//	System.out.println("Pricing Scheme Updating Resource: VMid =" + VM.getVMid()+" Charged for resources = " + count);
	//	System.out.println("Pricing Scheme Updating Resource: VMid =" + VM.getVMid()+" Total Resource charges= " + VM.getResourcesCharges());
		
		}
	
	public void updateVMEnergyCharges(VMinfo VM){	
	//	VM.setChangeTime();
		long duration = VM.getDuration(VM.getEnergyChargesAll().getTime(), VM.getChangeTime());
		double energycharges = updateEnergyCharges(VM);
		
	//	System.out.println("Last change time is: "+ VM.getEnergyChargesAll().getTime().getTimeInMillis()+" Now is: "+VM.getChangeTime().getTimeInMillis()+" and the diff is "+duration);
	//	System.out.println("Updating energy charges to " + energycharges);
		VM.updateEnergyCharges(energycharges);
		VM.setCurrentCharges(energycharges);
	//	System.out.println("Total energy charges " + VM.getEnergyCharges());
		
	}
	
	public double updateEnergyCharges(VMinfo VM){
		//the energy charges for the past period
		
		double price = getEnergyPrice(VM);
        double energyCharges = (double) Math.round((getEnergy(VM) * price) * 1000) / 1000;
   //     System.out.println("Pricing scheme: I am updating energy with this price "+getEnergyPrice(VM));
        return energyCharges;
		
	}
	
	
	 private double getEnergyPrice(VMinfo vM) {
		 return vM.getIaaSProvider().getEnergyPriceForBilling();

	}


	public double getEnergy(VMinfo VM){
	        double difference=3000;
	        ZonedDateTime zdt1 = ZonedDateTime.ofInstant(Instant.ofEpochMilli(VM.getEnergyChargesAll().getTime().getTimeInMillis()), ZoneId.systemDefault());
	//        System.out.println(zdt1.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
	        ZonedDateTime zdt2 = ZonedDateTime.ofInstant(Instant.ofEpochMilli(VM.getChangeTime().getTimeInMillis()), ZoneId.systemDefault());
	//        System.out.println(zdt2.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
	       try{
	        	EMInteraction response = new EMInteraction();
	        	double energy = response.getEnergyofVM(VM.getAppID(), Integer.toString(VM.getDepID()), Integer.toString(VM.getVMid()), zdt1.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),zdt2.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
	        	VM.updateEnergyConsumption(energy/1000);
	        	return energy/1000;
	        }
	       catch (Exception ex){
			//	System.out.println("Pricing Modeller  getEnergy: Could receive asnwer");
				logger.info("Could not send the message to queue");
			}
	      //  if (VM.getEnergyConsumptionofLastPeriod()!=0){
	    //    	difference= VM.getEnergyConsumptionofLastPeriod();
	     //   }
	  //      System.out.println("Pricing scheme: I am updating energy difference"+difference);
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


	public double getVMPredictedPrice(VMinfo vM, double duration) {
		// TODO Auto-generated method stub
		return 0;
	}



	public double predictTotalCharges(VMinfo vm, boolean energySet) {
		// TODO Auto-generated method stub
		return 0;
	}

	
}