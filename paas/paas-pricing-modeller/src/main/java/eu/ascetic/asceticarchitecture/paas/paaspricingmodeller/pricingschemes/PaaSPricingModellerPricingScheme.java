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

//import java.time.Instant;
//import java.time.ZoneId;
//import java.time.ZonedDateTime;
//import java.time.format.DateTimeFormatter;

import java.sql.Timestamp;
import java.util.Date;
import java.util.TimeZone;
import java.text.DateFormat;
import java.text.SimpleDateFormat;




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
	//TESTED
	public Charges predictResourcesCharges(VMinfo vm, ResourceDistribution distribution, double price) {
		Charges b = new Charges();
		
		double temp = distribution.getDistribution(vm)*price*vm.getPredictedDuration();
		b.setCharges(vm.getChangeTime(), temp);
	//	System.out.println("Scheme predict resource charges: Resource charges " + b.getChargesOnly());
		return b;
	}
	
	//TESTED
	public double predictResourcePrice (VMinfo VM, double price, ResourceDistribution distribution, double duration){
		double Resourcecharges = distribution.getDistribution(VM)*price;
		double count = Resourcecharges*(duration);
//		System.out.println("Scheme the duration is: " + duration);
		return (double) Math.round(count*1000) / 1000;
	}
	
	//TESTED
	public double predictEnergyPrice (VMinfo VM, double duration){
		double price = getEnergyPrice(VM);
		 double energyCharges = getPredictedEnergy(VM, duration) * price;
	//	 System.out.println("PRModellerScheme predictEnergyPrice: " + energyCharges);
		//double energycharges = (energy/1000)*price; 
		return (double) Math.round(energyCharges * 1000) / 1000;
	}
	
	//TESTED
	private double getPredictedEnergy(VMinfo VM, double duration) {
		 	double difference=0;
	        try{
	        	EMInteraction response = new EMInteraction();
	        	double energy = response.getPredictedEnergyofVM(VM.getAppID(), Integer.toString(VM.getDepID()), Integer.toString(VM.getVMid()), Double.toString(duration));
	        	VM.updateEnergyConsumption(energy/1000);
	       // 	System.out.println("Pricing Modeller Scheme energy is " + energy/1000);
	        	return energy/1000;
	        }
	       catch (Exception ex){
	    	   
		//		System.out.println("Pricing Modeller Scheme getPredictedEnergy: Could receive asnwer");
				logger.error("Pricing Modeller getPredictedEnergy: Could recieve answer from EM modeller");
	       }
	      //  if (VM.getEnergyConsumptionofLastPeriod()!=0){
	    //    	difference= VM.getEnergyConsumptionofLastPeriod();
	     //   }
	      // System.out.println("Pricing scheme: I am updating energy difference"+difference);
	        return difference;

	}

//TESTED
	public Charges predictEnergyCharges(VMinfo VM, double average, boolean energySet){
		Charges charges = new Charges();
		
		if (!energySet){
			//System.out.println("here");
		VM.setEnergyPredicted(getPredictedEnergy(VM, VM.getPredictedDuration()));
		}
		charges.setCharges(VM.getChangeTime(), VM.getEnergyPredicted()*average);
		//System.out.println("Energy charges " + charges);
		return charges;
	}
	
	public double updateAverageEnergyPrice(){
		double price =0.007;
		return price;
		
	}
	
	
	//TESTED
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
	
	
	//TESTED
	public void updateVMEnergyCharges(VMinfo VM){	
	//	VM.setChangeTime();
		long duration = VM.getDuration(VM.getEnergyChargesAll().getTime(), VM.getChangeTime());
		double energycharges = updateEnergyCharges(VM);
		
	//	System.out.println("Scheme  Last change time is: "+ VM.getEnergyChargesAll().getTime().getTimeInMillis()+" Now is: "+VM.getChangeTime().getTimeInMillis()+" and the diff is "+duration);
	//	System.out.println("Scheme  Updating energy charges to " + energycharges);
		VM.updateEnergyCharges(energycharges);
		VM.setCurrentCharges(energycharges);
	//	System.out.println("Total energy charges " + VM.getEnergyCharges());
		
	}

	
	//TESTED
	public double updateEnergyCharges(VMinfo VM){
		//the energy charges for the past period
		
		double price = getEnergyPrice(VM);
		double energy = getEnergy(VM)/1000;
        double energyCharges = (double) Math.round(( energy* price) * 1000) / 1000;
   //     System.out.println("Pricing scheme: I am updating energy "+energy+"  with this price "+getEnergyPrice(VM));
        return energyCharges;
		
	}
	
	//TESTED
	 private double getEnergyPrice(VMinfo vM) {
		 double price =  vM.getIaaSProvider().getEnergyPriceForBilling();
	//	 System.out.println("Scheme the energy price is: " + price);
		 return price;

	}


	 //TESTED
	public double getEnergy(VMinfo VM){
	        double difference=3000;
	       
	        TimeZone tz = TimeZone.getTimeZone("UTC");
	        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");     
	        df.setTimeZone(tz);
	             
	        Date startDate = new Date(VM.getEnergyChargesAll().getTime().getTimeInMillis());
	        String startDateISO = df.format(startDate);
	        
	        Date endDate = new Date(VM.getChangeTime().getTimeInMillis());
	        String endDateISO = df.format(endDate);
	     //   System.out.println("start " + startDateISO + " end " + endDateISO + " difference "+(endDate.getTime()-startDate.getTime()));
	        
	      //  ZonedDateTime zdt1 = ZonedDateTime.ofInstant(Instant.ofEpochMilli(VM.getEnergyChargesAll().getTime().getTimeInMillis()), ZoneId.systemDefault());
	     //   System.out.println(zdt1.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
	     //   ZonedDateTime zdt2 = ZonedDateTime.ofInstant(Instant.ofEpochMilli(VM.getChangeTime().getTimeInMillis()), ZoneId.systemDefault());
	     //   System.out.println("Scheme getEnergy " +zdt2.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
	       try{
	    	    EMInteraction response = new EMInteraction();
	    	    double energy = response.getEnergyofVM(VM.getAppID(), Integer.toString(VM.getDepID()), Integer.toString(VM.getVMid()), startDateISO, endDateISO);
	       //	double energy = response.getEnergyofVM(VM.getAppID(), Integer.toString(VM.getDepID()), Integer.toString(VM.getVMid()), zdt1.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),zdt2.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
	        	VM.updateEnergyConsumption(energy/1000);
	        //	System.out.println("Energy taken from EM: " +energy);
	        	return energy/1000;
	        }
	       catch (Exception ex){
	    	   	System.out.println("Pricing Modeller Scheme getEnergy: Could not receive asnwer");
				logger.error("Pricing Modeller Scheme getEnergy: Could not receive answer from Energy Modeller");
	    	    difference= VM.getEnergyFromAppMan();
	    	    if (difference == 0){
	    	    	VM.setEnergyFailed(true);
	    	    }
				
			}
	      //  if (VM.getEnergyConsumptionofLastPeriod()!=0){
	    //    	difference= VM.getEnergyConsumptionofLastPeriod();
	     //   }
	    //   System.out.println("Pricing scheme: I am updating energy difference"+difference);
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