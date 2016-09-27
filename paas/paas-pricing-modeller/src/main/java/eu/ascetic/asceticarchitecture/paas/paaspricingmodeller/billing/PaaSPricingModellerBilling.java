/**
 *  Copyright 2014 Athens University of Economics and Business
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
package eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.billing;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.pricingschemes.PaaSPricingModellerPricingScheme;
import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.queue.client.GenericPricingMessage;
import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.queue.client.GenericPricingMessage.Unit;
import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.queue.client.PricingModellerQueueServiceManager;
import eu.ascetic.asceticarchitecture.paas.type.ChargesCalculator;
import eu.ascetic.asceticarchitecture.paas.type.DeploymentInfo;
import eu.ascetic.asceticarchitecture.paas.type.Price;
import eu.ascetic.asceticarchitecture.paas.type.ResetCharges;
import eu.ascetic.asceticarchitecture.paas.type.VMinfo;




/**
 * This is the main interface of the pricing modeller of PaaS layer. 
 * Functionality:
 * The ability to provide a price estimation of an application per hour, given the energy consumed of this app, the deployment id, 
 * the application id, the IaaS provider id and the IaaS provider's price. 
 * 
 * The price estimation can be also given without the provision of energy estimation.
 * 
 * The price estimation can be also given without the provision of an PaaS price. 
 * @author E. Agiatzidou
 */


public class PaaSPricingModellerBilling extends PaaSPricingModellerRegistration implements PaaSPricingModellerBillingInterface{
	
	static PaaSPricingModellerBillingEvents eventsBilling = new PaaSPricingModellerBillingEvents();
	static PaaSPricingModellerBillingBasedOnIaaS IaaSBilling = new PaaSPricingModellerBillingBasedOnIaaS();
	
	
	double averagePrice=1;

	boolean lastEnergyNotCalculatedFromPaaS = false;
	
	//static Logger logger = Logger.getLogger(PaaSPricingModellerBilling.class);
	
	public PaaSPricingModellerBilling(){
		//Logger logger = Logger.getLogger(PaaSPricingModellerBilling.class);
	}
	
	public boolean getEnergyFlag(){
		return lastEnergyNotCalculatedFromPaaS;
	}
	
	public void updateVMCharges(double price) {
        for (Integer key : registeredDynamicEnergyPricesVMs.keySet()) {
        //	System.out.println("I am searching for VMs to be updated with this price " + price);
        	VMinfo VM= registeredDynamicEnergyPricesVMs.get(key);
        //	System.out.println("found this " + VM.getVMid());
            VM.getScheme().updateVMEnergyCharges(VM);
        }

    }
	

	
////////////////////////////////// App and VM Charges //////////////////////
	//TESTED
	public double getAppCurrentTotalCharges(int depl, HashMap<Integer,Double> energyOfVMs){
		 if (allApps.containsKey(depl)) {
			
			 List <Integer> vm = new ArrayList<Integer>();
			 	DeploymentInfo app = allApps.get(depl);
	            double totalcharges = 0;
	            double currentcharges = 0;
	            double energy = 0.0;
	            for (int i=0; i<app.getNumberOfVMs();i++){
	            	
	            	int VMid = app.getVM(i).getVMid();
	            	VMinfo VM = app.getVM(i);
	            	if(VM.isActive()){
	            	  vm.add(VMid); 
	            	  if (energyOfVMs!=null){
	            		  if (energyOfVMs.get(VMid)!=null){
	            			  VM.setEnergyFromAppMan(energyOfVMs.get(VMid));
	            		  }
	            	  }
	            	  totalcharges = totalcharges + getVMCharges(VM);
	            	  currentcharges = currentcharges + VM.getCurrentCharges();
	            	  if (VM.energyFailed()){
	            		  app.setEnergyFailed(true);
	            	  }
	            	//  System.out.println("Billing: updating charges of VM "+VM.getVMid()+" with "+ VM.getTotalCharges());
	            	//  logger.info("Billing: updating charges of VM "+VM.getVMid()+" with "+ VM.getTotalCharges());
	            	}else{
	            		totalcharges = totalcharges + VM.getTotalCharges();
	            		 currentcharges = currentcharges + VM.getCurrentCharges();
	            	}
	            }
	            try{
        			app.setCurrentCharges(currentcharges);
        			app.setTotalCharges(totalcharges);
        		//	 System.out.println("Billing: current charges of app are "+ app.getCurrentCharges());
        			// logger.info("Billing: current charges of app are "+ app.getCurrentCharges());
         			 producer.sendToQueue("PMBILLING", app.getVM().getIaaSProvider().getID(), app.getId(), vm, Unit.TOTALCHARGES, app.getTotalCharges());
        			 producer.sendToQueue("PMBILLING", app.getVM().getIaaSProvider().getID(), app.getId(), vm, Unit.CHARGES, app.getCurrentCharges());
	            	
        			
        			}
        			catch (Exception ex){
        				//System.out.println("Billing: Could not send the message to queue");
        			//	logger.error("PM: Could not send message to queue");
        				//logger.info("Could not send the message to queue");
        			}
	            lastEnergyNotCalculatedFromPaaS=app.getEnergyFailed();
	            return totalcharges;
	        } else {
	            return 0;
	        }

	}
	
	//TESTED
	 public double getVMCharges(VMinfo VM) {
		 
	        PaaSPricingModellerPricingScheme scheme = VM.getScheme();
	    //   System.out.println("Billing: the scheme of VM "+VM.getVMid()+" is "+scheme.getSchemeId());
	   //     logger.info("Billing: the scheme of VM "+VM.getVMid()+" is "+scheme.getSchemeId());
	      
	        return scheme.getTotalCharges(VM);
	    }
	 
	 public void setVMsPredictedEnergy(HashMap<Integer, Double> energyPerVM){
		 VMinfo VM;
		 for (Integer key : registeredDynamicEnergyPricesVMs.keySet()) {
	        	VM= registeredDynamicEnergyPricesVMs.get(key);
		 	 
	        	if (energyPerVM.containsKey(VM.getVMid())){
	        		VM.setEnergyPredicted(energyPerVM.get(VM.getVMid()));
	        	}
		 }
	 }
	 
	 public void updateVMEnergy(HashMap<Integer, Double> energyPerVM) throws Exception{
		 VMinfo VM;
		 for (Integer key : registeredDynamicEnergyPricesVMs.keySet()) {
	        	VM= registeredDynamicEnergyPricesVMs.get(key);
		 	 
	        	if (energyPerVM.containsKey(VM.getVMid())){
	        		VM.updateEnergyConsumption((energyPerVM.get(VM.getVMid())));
	       //  System.out.println("Billing: energy of VM "+VM.getVMid()+" is updated");
	       //  logger.info("Billing: energy of VM "+VM.getVMid()+" is updated");
	    	        
	        	}
		 }
		 
	 }


	public LinkedList<Integer> getCostlyVMs(int deplID, double currentAppCharges, double totalChargesLimit, double remainingAppDuration, HashMap<Integer, Double> energy){
		 double differenceInCharges = totalChargesLimit - currentAppCharges;
		 LinkedList<Integer> VMs = null;
		 HashMap<Integer,Double> VMCharges = null;
		 double charges = 0.0;
		 double vmCharges = 0.0;
		 if (allApps.containsKey(deplID)) {
			 	DeploymentInfo app = allApps.get(deplID);
			 	for (int i=0; i<app.getNumberOfVMs();i++){
			 		VMinfo VM = app.getVM(i);
			 		VM.setPredictedDuration(remainingAppDuration);
			 		if (VM.getSchemeID()==0){
			 			vmCharges = VM.getScheme().predictTotalCharges(VM, false);
						 VMCharges.put(VM.getVMid(),vmCharges);
						 charges = charges + vmCharges;
					 }
					 else {
						 VM.setEnergyPredicted(energy.get(VM.getVMid())*(Math.ceil(VM.getPredictedDuration()/3600)));
						 vmCharges = VM.getScheme().predictTotalCharges(VM, true);
						
			 			VMCharges.put(VM.getVMid(),vmCharges);
			 			charges = charges +vmCharges;
					 }
				 }
			 	
			 //	double predictedCharges=predictCharges(app, averageWattPerHourPerVM);
			 	while (differenceInCharges<charges) {
			 		double maxValueInMap=(Collections.max(VMCharges.values()));
				 	int key = 0;
				 	for (Integer i : VMCharges.keySet()) {
				 		if (VMCharges.get(i)==maxValueInMap){
				 			key = i;
				 		}
				 	}
				 	charges = charges - VMCharges.get(key);
			 		VMs.add(key);
			 	}
			 	return VMs;
			 	
		 }
		 else return null; 
	 }
	
	@SuppressWarnings("null")
	public LinkedList<Integer> getCostlyVMs(int deplID){
		synchronized(allApps.get(deplID).getLock()){
			allApps.get(deplID).setChanging(true);
		//	System.out.println("calculating costs");
			if (allApps.containsKey(deplID)) {
			DeploymentInfo app = allApps.get(deplID);
			double differenceInCharges = app.getCurrentPrice()-app.getCurrentCharges();
			LinkedList<Integer> VMs = new LinkedList<>();
			HashMap<Integer,Double> VMCharges = new HashMap<>();
			 double charges = 0.0;
			 double vmCharges = 0.0;
			 double remainingDuration = 3600;
			 
			for (int i=0; i<app.getNumberOfVMs();i++){
				VMinfo VM = app.getVM(i);
				double temp = 90 - VM.getCurrentDuration();
				if (remainingDuration>temp){
					remainingDuration = temp;
				}
			}
		//	 System.out.println("the remaining duration is "+remainingDuration);
			 	for (int i=0; i<app.getNumberOfVMs();i++){
			 		VMinfo VM = app.getVM(i);
			 		
			 		VM.setPredictedDuration(remainingDuration);
			 		if (VM.getSchemeID()==0){
			 			vmCharges = VM.getScheme().predictTotalCharges(VM, false);
			 			VMCharges.put(VM.getVMid(),vmCharges);
			 //			System.out.println("Charges for scheme 0 " + vmCharges + " Just counted VMid " + VM.getVMid());
			 			charges = charges + vmCharges;
					 }
					 else {
						 vmCharges = VM.getScheme().predictTotalCharges(VM, false);
							
				 			VMCharges.put(VM.getVMid(),vmCharges);
				 			charges = charges +vmCharges;
				// 			System.out.println("Charges for scheme 1 " + charges + " Just counted VMid " + VM.getVMid());
					 }
				 }
			 	
			 //	System.out.println("Difference in charges is " + differenceInCharges + " Predicted ones are " + charges);
			 	while (differenceInCharges<charges) {
			 		double maxValueInMap=(Collections.max(VMCharges.values()));
				 	int key = 0;
				 	for (Integer i : VMCharges.keySet()) {
				 		if (VMCharges.get(i)==maxValueInMap){
				 			key = i;
				 		}
				 	}
			//	 	System.out.println("key is " + key);
				 	charges = charges - VMCharges.get(key);
			 		VMs.add(key);
			 	}
				allApps.get(deplID).setChanging(false);
				allApps.get(deplID).getLock().notifyAll();
			 	return VMs;
			 	
		 }
		 else{
				allApps.get(deplID).setChanging(false);
				allApps.get(deplID).getLock().notifyAll();
				return null; 
		 }
			 

	}
}
	
	//TESTED
	public void removeVM(int deplID, int VMid) {
	//	 System.out.println("Billing: removing VM");
	//	 System.out.println("try "+ allApps.containsKey(deplID));
		 synchronized(allApps.get(deplID).getLock()){
		 allApps.get(deplID).setChanging(true);
		try{
//			System.out.println("try "+ allApps.get(deplID).getVMbyID(VMid));
			getVMCharges(allApps.get(deplID).getVMbyID(VMid));
			
		}catch (NullPointerException ex) {
	//		System.out.println("catch");
        //   logger.error("The VM with VMid " + VMid + " does not exist");
			allApps.get(deplID).setChanging(false);
			return;
		}
		allApps.get(deplID).getVMbyID(VMid).stopped();
		allApps.get(deplID).getVMbyID(VMid).setEndTime(allApps.get(deplID).getVM().getChangeTime().getTimeInMillis());
		allApps.get(deplID).setChanging(false);
		 allApps.get(deplID).getLock().notifyAll();
		 }
	}
	
	//TESTED
	public void resizeVM(int depID, int VMid, double CPU, double RAM, double storage){
		synchronized(allApps.get(depID).getLock()){
			allApps.get(depID).setChanging(true);
			getVMCharges(allApps.get(depID).getVMbyID(VMid));
			allApps.get(depID).getVMbyID(VMid).createNewChars(CPU, RAM, storage,allApps.get(depID).getVMbyID(VMid).getTotalChargesAll() );	
		//	System.out.println("Billing: The charges of the VM with id " + VMid + " are until now " + allApps.get(depID).getVMbyID(VMid).getTotalChargesAll().getChargesOnly());
			allApps.get(depID).setChanging(false);
			allApps.get(depID).getLock().notifyAll();
		}
		//new ResetCharges(allApps.get(depID), this);
		
	}
	
	 //////////////////////////////////Event Charges//////////////////////
	
	public double predictEventCharges(DeploymentInfo deployment) {
		return eventsBilling.predictAppEventCharges(deployment);
	}
	
	//TESTED
	public double predictAppEventCharges(DeploymentInfo deployment) {
		return eventsBilling.predictAppEventCharges(deployment);
	}
	
	//TESTED
	public double predictAppEventChargesVMbased(DeploymentInfo deployment) {
		return eventsBilling.predictAppEventChargesVMbased(deployment);
	}
	 
	 
	 
	 
	////////////////////////////////// Based on IaaS Calculations//////////////////////
	
	public double predictCharges(DeploymentInfo deploy){
		return IaaSBilling.predictCharges(deploy);
	}
	
	public double predictPrice(DeploymentInfo deploy){
		return IaaSBilling.predictPrice(deploy);
	}
	
	//TESTED
public double getAppCurrentTotalCharges(int depl, double charges){
		
		DeploymentInfo deploy = getApp(depl);
		try{
			return IaaSBilling.getAppCurrentTotalCharges(deploy, charges);
		}catch (NullPointerException ex) {
			deploy = new DeploymentInfo(depl, this);
		//	System.out.println("PM PaaS: Trying to get App Charges but the Deployment has been removed. Re-creating");
			return IaaSBilling.getAppCurrentTotalCharges(deploy, charges);
		}
		
		
	}










	/*
	static HashMap<Integer,DeploymentInfo> registeredStaticApps = new HashMap<Integer,DeploymentInfo>();
	static HashMap<Integer,DeploymentInfo> registeredDynamicApps = new HashMap<Integer,DeploymentInfo>();
	static HashMap<Integer,DeploymentInfo> allApps = new HashMap<Integer,DeploymentInfo>();
	
	static HashMap<Integer, VMinfo> registeredStaticEnergyPricesVMs = new HashMap<Integer, VMinfo>();
    static HashMap<Integer, VMinfo> registeredDynamicEnergyPricesVMs = new HashMap<Integer, VMinfo>();
	//static HashMap<Integer, DeploymentInfo> deployments = new HashMap<Integer, DeploymentInfo>();
	*/

	
	/*
	public void registerApp(DeploymentInfo app) {
		if (app.getSchemeId()==100){
			for (int i=0;i<app.getNumberOfVMs();i++){
				
			if ((app.getVM(i).getSchemeID() == 0) || (app.getVM(i).getSchemeID() == 2)) {
	            registeredStaticEnergyPricesVMs.put(app.getVM(i).getVMid(), app.getVM(i));
	            app.getVM(i).setIaaSProvider(app.getIaaSProvider());
	            
	        } else {
	            registeredDynamicEnergyPricesVMs.put(app.getVM(i).getVMid(), app.getVM(i));
	            app.getVM(i).setIaaSProvider(app.getIaaSProvider());
	        }
	      //  vm.setStartTime();
			allApps.put(app.getId(), app);
		}
		if (app.getSchemeId()==0||app.getSchemeId()==2)
			registeredStaticApps.put(app.getId(), app);
		else
			registeredDynamicApps.put(app.getId(), app);
		}	
	}
	
	
	public void unregisterApp(DeploymentInfo app) {
		if ((app.getSchemeId()==0)){
			registeredStaticApps.remove(app.getSchemeId());
			app.setEndTime();}
		else{
			registeredDynamicApps.remove(app.getSchemeId());
			app.setEndTime();}
		
	}


	public void stopApp(DeploymentInfo app) {
		app.setEndTime();
		
	}
	*/

/*
public double predictEventCharges(DeploymentInfo deploy){
	if (deploy.getSchemeId()==0||deploy.getSchemeId()==2){
		double charges = deploy.getIaaSProvider().predictResourcesCharges(deploy.getVM(), deploy.getPredictedInformation().getPredictedDuration(), deploy.getIaaSProvider().getStaticResoucePrice());
		charges= charges+0.2*charges;
		deploy.setPredictedCharges(charges);
		return deploy.getPredictedCharges();
	}
	
	if (deploy.getSchemeId()==1){
		double a = deploy.getIaaSProvider().predictResourcesCharges(deploy.getVM(), deploy.getPredictedInformation().getPredictedDuration(), deploy.getIaaSProvider().getResoucePrice());
		double b = deploy.getIaaSProvider().predictEnergyCharges(deploy.getVM().getEnergyPredicted(), deploy.getIaaSProvider().getAverageEnergyPrice());
		double charges = a+b;
		charges= charges+0.2*charges;
		deploy.setPredictedCharges(charges);
		return deploy.getPredictedCharges();
	}
	
	return 0.0;		
}

public double predictAppEventCharges(DeploymentInfo deploy) {
	double temp =0;
	if (deploy.getSchemeId()==0||deploy.getSchemeId()==2){
		double charges =0;
		for (int i=0; i<deploy.getNumberOfVMs(); i++){
			temp = deploy.getIaaSProvider().predictResourcesCharges(deploy.getVM(i), deploy.getVM(i).getActualDuration(),  deploy.getIaaSProvider().getStaticResoucePrice());
			charges = charges+temp;
		}
		charges= charges+0.2*charges;
		deploy.setPredictedCharges(charges);
		return deploy.getPredictedCharges();
	}
	
	if (deploy.getSchemeId()==1){
		double charges =0;
		for (int i=0; i<deploy.getNumberOfVMs(); i++){
			double a = deploy.getIaaSProvider().predictResourcesCharges(deploy.getVM(i), deploy.getVM(i).getActualDuration(), deploy.getIaaSProvider().getResoucePrice());
			charges = charges+a;
		}
		double b = deploy.getIaaSProvider().predictEnergyCharges(deploy.getEnergy(), deploy.getIaaSProvider().getAverageEnergyPrice());
		charges = charges +b;
		charges= charges+0.2*charges;
		
		deploy.setPredictedCharges(charges);
		return deploy.getPredictedCharges();
	}
	
	return 0.0;		
}

public double predictAppEventChargesVMbased(DeploymentInfo deploy) {
	double temp =0;
	double charges=0;
	for (int i=0; i<deploy.getNumberOfVMs(); i++){
	if (deploy.getVM(i).getSchemeID()==0||deploy.getVM(i).getSchemeID()==2){
		 	
			temp = deploy.getIaaSProvider().predictResourcesCharges(deploy.getVM(i), deploy.getVM(i).getActualDuration(),  deploy.getIaaSProvider().getStaticResoucePrice());
			charges = charges+temp;
		charges= charges+0.2*charges;
		deploy.setPredictedCharges(charges);
		
	}
	
	if (deploy.getVM(i).getSchemeID()==1){
		
		double a = deploy.getIaaSProvider().predictResourcesCharges(deploy.getVM(i), deploy.getVM(i).getActualDuration(), deploy.getIaaSProvider().getResoucePrice());
		charges = charges+a;
		double b = deploy.getIaaSProvider().predictEnergyCharges(deploy.getEnergy(), deploy.getIaaSProvider().getAverageEnergyPrice());
		charges = charges +b;
		charges= charges+0.2*charges;
		deploy.setPredictedCharges(charges);
		
	}
	
		
	}
	return deploy.getPredictedCharges();
}


*/

}