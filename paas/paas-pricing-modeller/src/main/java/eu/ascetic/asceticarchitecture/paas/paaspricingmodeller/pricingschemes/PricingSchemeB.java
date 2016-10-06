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
import eu.ascetic.asceticarchitecture.paas.type.ResourceDistribution;
import eu.ascetic.asceticarchitecture.paas.type.VMinfo;


/**
 * 
 * @author E. Agiatzidou
 */

public class PricingSchemeB extends PaaSPricingModellerPricingScheme {

	
	public PricingSchemeB(int id) {
		super(id);
	}

	
/////////////////////////PREDICT CHARGES ///////////////////////////////
	
	//TESTED
	@Override
public double predictTotalCharges(VMinfo vm, boolean energySet){
	Charges a = predictEnergyCharges(vm, vm.getIaaSProvider().getAverageEnergyPrice(), energySet);
	Charges b = predictResourcesCharges(vm, getDistribution(vm), getResourcePrice(vm));
	
	double temp = (double) Math.round((a.getChargesOnly()+b.getChargesOnly()) * 1000) / 1000;
	try{
		vm.getProducer().sendToQueue("PMPREDICTION",  vm.getDepID(), vm.getVMid(),vm.getSchemeID(), Unit.CHARGES, temp);
		return temp;
	}catch(Exception ex){
	//	System.out.println("Pricing Modeller  getEnergy: Could not find producer");
		 logger.error("PM: Could not set producer");
		return temp;
	}
		
}

/////////////////////////////////////////////////
	//TESTED
	@Override 
	public double getVMPredictedPrice(VMinfo VM, double duration) {
		double price1 = predictResourcePrice (VM, getResourcePrice(VM), getDistribution(VM), duration);

	//	System.out.println("Pricing B: "+ price1);
		double price2 = predictEnergyPrice (VM, duration);
		VM.setCurrentPrice(price1+price2);
		try{
			VM.getProducer().sendToQueue("PMPREDICTION",  VM.getDepID(), VM.getVMid(),VM.getSchemeID(), Unit.PRICEHOUR, VM.getCurrentprice());
		}
		catch(Exception ex){
		//	System.out.println("PM: Could not send message to queue");
			 logger.error("Scheme B PM: Could not send message to queue");
		}
//		System.out.println("Pricing B: "+ VM.getCurrentprice() + " with price " + getResourcePrice(VM));
		return VM.getCurrentprice();
	}

///////////////////////////// UPDATE CHARGES BASED ON ENERGY CHANGES ////////////////
public void updateVMCharges(VMinfo VM) {
	System.out.println("Found "+ VM.getVMid());
	VM.setChangeTime();
	updateVMEnergyCharges(VM);
	updateVMResourceCharges(VM, getResourcePrice(VM), getDistribution(VM));
	VM.setTotalCharges(VM.getEnergyCharges()+VM.getResourcesCharges());
	VM.setCurrentCharges(VM.getResourcesCharges()+VM.getEnergyCharges());
	//System.out.println("charges= " +VM.getTotalCharges());
	try{
		VM.getProducer().sendToQueue("PMBILLING",  VM.getDepID(), VM.getVMid(),VM.getSchemeID(), Unit.TOTALCHARGES, VM.getTotalCharges());
		VM.getProducer().sendToQueue("PMBILLING",  VM.getDepID(), VM.getVMid(),VM.getSchemeID(), Unit.CHARGES, VM.getCurrentCharges());
	}
	catch(Exception ex){
		//System.out.println("PM: Could not send message to queue");
		 logger.error("PM: Could not send message to queue");
	}
}




/////////////////////////// GET CHARGES /////////////////////////
//TESTED
@Override
public double getTotalCharges(VMinfo VM) {
	VM.setChangeTime();
	updateVMResourceCharges(VM, getResourcePrice(VM), getDistribution(VM));
	//System.out.println("Scheme B Set change time to " +VM.getChangeTime().getTimeInMillis());
	updateVMEnergyCharges(VM);
	
	VM.setTotalCharges(VM.getResourcesCharges()+VM.getEnergyCharges());
//	System.out.println("B: Total charges for VM = " +VM.getTotalCharges());
//	System.out.println("B: VMid =" + VM.getVMid()+" Current charges= " + VM.getCurrentCharges());
	try{
		VM.getProducer().sendToQueue("PMBILLING",  VM.getDepID(), VM.getVMid(),VM.getSchemeID(), Unit.TOTALCHARGES, VM.getTotalCharges());
		VM.getProducer().sendToQueue("PMBILLING",  VM.getDepID(), VM.getVMid(),VM.getSchemeID(), Unit.CHARGES, VM.getCurrentCharges());
	}
	catch(Exception ex){
	//	System.out.println("PM: Could not send message to queue");
		 logger.error("PM: Could not send message to queue");
	}
	return (VM.getTotalCharges());
}

//TESTED
private double getResourcePrice(VMinfo VM){
	double price = VM.getIaaSProvider().getPriceSec(VM.getIaaSProvider().getResoucePrice());
	//System.out.println("Scheme B: price of provider " + VM.getIaaSProvider().getID()+" is "+price);
	return price;
}

//TESTED
private ResourceDistribution getDistribution(VMinfo VM){
	return VM.getIaaSProvider().getDistribution();
}



}