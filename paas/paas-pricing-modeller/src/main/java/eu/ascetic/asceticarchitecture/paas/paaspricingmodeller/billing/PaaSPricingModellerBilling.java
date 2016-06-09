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


import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;

import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.pricingschemes.PaaSPricingModellerPricingScheme;
import eu.ascetic.asceticarchitecture.paas.type.DeploymentInfo;
import eu.ascetic.asceticarchitecture.paas.type.Price;
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
	
	
	
	
	public void updateVMCharges(double price) {
        for (Integer key : registeredDynamicEnergyPricesVMs.keySet()) {
        //	System.out.println("I am searching for VMs to be updated with this price " + price);
        	VMinfo VM= registeredDynamicEnergyPricesVMs.get(key);
        //	System.out.println("found this " + VM.getVMid());
            VM.getScheme().updateVMEnergyCharges(VM);
        }

    }
	

	
////////////////////////////////// App and VM Charges //////////////////////
	
	public double getAppCurrentTotalCharges(int depl){
		 if (allApps.containsKey(depl)) {
			 	DeploymentInfo app = allApps.get(depl);
	            double charges = 0;
	            for (int i=0; i<app.getNumberOfVMs();i++){

	            	int VMid = app.getVM(i).getVMid();
	            	VMinfo VM = app.getVM(i);
	            	                          	
	                charges = charges + getVMCharges(VM);
	            }

	            return charges;
	        } else {
	            return 0;
	        }

	}
	
	 public double getVMCharges(VMinfo VM) {

	        PaaSPricingModellerPricingScheme scheme = VM.getScheme();
	      //  System.out.println("Billing: the scheme of VM "+VM.getVMid()+" is "+scheme.getSchemeId());
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
	 
	 public void updateVMEnergy(HashMap<Integer, Double> energyPerVM){
		 VMinfo VM;
		 for (Integer key : registeredDynamicEnergyPricesVMs.keySet()) {
	        	VM= registeredDynamicEnergyPricesVMs.get(key);
		 	 
	        	if (energyPerVM.containsKey(VM.getVMid())){
	        		VM.updateEnergyConsumption((energyPerVM.get(VM.getVMid())));
	        	}
		 }
		 
	 }
	 
	 public double predictCharges(DeploymentInfo deploy, HashMap<Integer, Double> averageWattPerHourPerVM){
		 VMinfo VM;
		 double charges=0;
		 for (int i=0; i<deploy.getNumberOfVMs();i++){
			 VM=deploy.getVM(i);
			 if (VM.getSchemeID()==0){
				 charges= charges+ VM.getScheme().predictTotalCharges(VM);
			 }
			 else {
				 VM.setEnergyPredicted(averageWattPerHourPerVM.get(VM.getVMid())*(Math.ceil(VM.getActualDuration()/3600)));
				 charges= charges+ VM.getScheme().predictTotalCharges(VM);
			 }
		 }
		 return charges;
	 }
	 
	 
	public LinkedList<Integer> consumingVMs(int deplID, double currentAppCharges, double totalChargesLimit, double remainingAppDuration, HashMap<Integer, Double> averageWattPerHourPerVM){
		 double differenceInCharges = totalChargesLimit - currentAppCharges;
		 LinkedList<Integer> VMs = null;
		 HashMap<Integer,Double> VMCharges = null;
		 if (allApps.containsKey(deplID)) {
			 	DeploymentInfo app = allApps.get(deplID);
			 	for (int i=0; i<app.getNumberOfVMs();i++){
			 		VMinfo VM = app.getVM(i);
			 		VM.setDuration(remainingAppDuration);
			 		if (VM.getSchemeID()==0){
						 VMCharges.put(VM.getVMid(),VM.getScheme().predictTotalCharges(VM));
					 }
					 else {
						 VM.setEnergyPredicted(averageWattPerHourPerVM.get(VM.getVMid())*(Math.ceil(VM.getActualDuration()/3600)));
			 			VMCharges.put(VM.getVMid(),VM.getScheme().predictTotalCharges(VM));
					 }
				 }
			 	
			 	double predictedCharges=predictCharges(app, averageWattPerHourPerVM);
			 	while (differenceInCharges<predictedCharges) {
			 		double maxValueInMap=(Collections.max(VMCharges.values()));
				 	int key = 0;
				 	for (Integer i : VMCharges.keySet()) {
				 		if (VMCharges.get(i)==maxValueInMap){
				 			key = i;
				 		}
				 	}
			 		predictedCharges = predictedCharges - VMCharges.get(key);
			 		VMs.add(key);
			 	}
			 	return VMs;
			 	
		 }
		 else return null;
		 
	 }
	public void removeVM(int deplID, int VMid) {
		try{
			getVMCharges(allApps.get(deplID).getVMbyID(VMid));
		}catch (NullPointerException ex) {
           // logger.error("The VM with VMid " + VMid + " does not exist");
			return;
		}
		allApps.get(deplID).getVMbyID(VMid).stopped();
		allApps.get(deplID).getVMbyID(VMid).setEndTime(allApps.get(deplID).getVM().getChangeTime().getTimeInMillis());
	}
	
	public void resizeVM(int depID, int VMid, double CPU, double RAM, double storage){
		//System.out.println("Billing: " +getVMCharges(allApps.get(depID).getVMbyID(VMid)));
		//System.out.println("Billing: The charges of the VM with id " + VMid + " are until now " + allApps.get(depID).getVMbyID(VMid).getTotalChargesAll());
		allApps.get(depID).getVM().createNewChars(CPU, RAM, storage, allApps.get(depID).getVMbyID(VMid).getTotalChargesAll());	
	}
	
	 //////////////////////////////////Event Charges//////////////////////
	
	public double predictEventCharges(DeploymentInfo deployment) {
		return eventsBilling.predictAppEventCharges(deployment);
	}
	
	public double predictAppEventCharges(DeploymentInfo deployment) {
		return eventsBilling.predictAppEventCharges(deployment);
	}
	
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
	
public double getAppCurrentTotalCharges(int depl, double charges){
		
		DeploymentInfo deploy = getApp(depl);
		if (deploy ==null){
		deploy = new DeploymentInfo(depl);	
		}
		
		return IaaSBilling.getAppCurrentTotalCharges(deploy, charges);
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