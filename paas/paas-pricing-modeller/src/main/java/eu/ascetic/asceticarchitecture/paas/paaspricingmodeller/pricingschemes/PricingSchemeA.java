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

import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.queue.client.GenericPricingMessage.Unit;
import eu.ascetic.asceticarchitecture.paas.type.Charges;
import eu.ascetic.asceticarchitecture.paas.type.DeploymentInfo;
import eu.ascetic.asceticarchitecture.paas.type.Price;
import eu.ascetic.asceticarchitecture.paas.type.ResourceDistribution;
import eu.ascetic.asceticarchitecture.paas.type.Time;
import eu.ascetic.asceticarchitecture.paas.type.VMinfo;



/**
 * This pricing scheme has ID==0. 
 * It gives only a static prices per hour. 
 * @author E. Agiatzidou
 */



public class PricingSchemeA extends PaaSPricingModellerPricingScheme {
	
//Price price; 
ResourceDistribution distribution = new ResourceDistribution();
	
	
	public PricingSchemeA(int id) {
		super(id);

	}

	/*public Price getPrice(){
		return price;
	}*/
	
	/////////////////////////PREDICTION/////////////////////////
	
	//TESTED
	@Override 
	public double predictTotalCharges(VMinfo vm, boolean energySet) {
		Charges b = predictResourcesCharges(vm, getDistribution(vm), getResourcePrice(vm));
		
		//double temp = (double) Math.round(b.getChargesOnly()*1000)/1000; 
		double temp = (double) Math.round(b.getChargesOnly()*1000) / 1000;
	//	System.out.println("Pricing A: " + temp +" " + b.getChargesOnly() + " " + vm.getProducer());
		try{
			vm.getProducer().sendToQueue("PMPREDICTION",  vm.getDepID(), vm.getVMid(),vm.getSchemeID(), Unit.CHARGES, temp);
		}
		catch(Exception ex){
		//	System.out.println("PM: Could not send message to queue");
			 logger.error("PM: Could not send message");
		}
		return temp;
	}
	
	//TESTED
	@Override 
	public double getVMPredictedPrice(VMinfo VM, double duration) {
		double price = predictResourcePrice (VM, getResourcePrice(VM), getDistribution(VM), duration);
	//	System.out.println("Pricing A:"+price);
		VM.setCurrentPrice(price);
		try{
			VM.getProducer().sendToQueue("PMPREDICTION",  VM.getDepID(), VM.getVMid(),VM.getSchemeID(), Unit.PRICEHOUR, VM.getCurrentprice());
		}
		catch(Exception ex){
		//	System.out.println("PM: Could not send message to queue");
			 logger.error("Scheme A PM: Could not send message to queue");
		}
	//	System.out.println("Pricing A: "+  VM.getCurrentprice() + " with price " + getResourcePrice(VM));
		return VM.getCurrentprice();
	}

////////////////////////////////// BILLING //////////////////////////
	
	//TESTED
	@Override
	public double getTotalCharges(VMinfo VM) {
		Unit unit = Unit.TOTALCHARGES;
		VM.setChangeTime();
		updateVMResourceCharges(VM, getResourcePrice(VM), getDistribution(VM));
		
		VM.setTotalCharges(VM.getResourcesCharges());
		try{
			VM.getProducer().sendToQueue("PMBILLING",  VM.getDepID(), VM.getVMid(),VM.getSchemeID(), unit, VM.getTotalCharges());
			VM.getProducer().sendToQueue("PMBILLING",  VM.getDepID(), VM.getVMid(),VM.getSchemeID(), Unit.CHARGES, VM.getCurrentCharges());
		}
		catch(Exception ex){
		//	System.out.println("PM: Could not send message to queue");
			 logger.error("PM: Could not set producer");
		}
		return (VM.getTotalCharges());
		
	}
	
	//TESTED
	private double getResourcePrice(VMinfo VM){
		double price = VM.getIaaSProvider().getPriceSec(VM.getIaaSProvider().getStaticResoucePrice());
		//System.out.println("Scheme A: price of provider " + VM.getIaaSProvider().getID()+" is "+price);
		return price;
	}
	
	//TESTED
	private ResourceDistribution getDistribution(VMinfo VM){
		return VM.getIaaSProvider().getDistribution();
	}
	
}