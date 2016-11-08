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
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.apache.log4j.Logger;

import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.PaaSPricingModeller;
import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.pricingschemes.PaaSPricingModellerPricingScheme;
import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.queue.client.PricingModellerQueueServiceManager;
import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.queue.client.GenericPricingMessage.Unit;
import eu.ascetic.asceticarchitecture.paas.type.DeploymentInfo;
import eu.ascetic.asceticarchitecture.paas.type.Price;
import eu.ascetic.asceticarchitecture.paas.type.TimeParameters;
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


public class PaaSPricingModellerRegistration {
	
	static HashMap<Integer,DeploymentInfo> registeredStaticApps = new HashMap<Integer,DeploymentInfo>();
	static HashMap<Integer,DeploymentInfo> registeredDynamicApps = new HashMap<Integer,DeploymentInfo>();
	static HashMap<Integer,DeploymentInfo> allApps = new HashMap<Integer,DeploymentInfo>();
	static LinkedList<Integer> allAppIDs = new LinkedList<Integer> ();
	
	static HashMap<Integer, VMinfo> registeredStaticEnergyPricesVMs = new HashMap<Integer, VMinfo>();
    static HashMap<Integer, VMinfo> registeredDynamicEnergyPricesVMs = new HashMap<Integer, VMinfo>();
	//static HashMap<Integer, DeploymentInfo> deployments = new HashMap<Integer, DeploymentInfo>();
	
	double averagePrice=1;

	static PricingModellerQueueServiceManager producer;
	//protected final Logger logger = Logger.getLogger(this.getClass());
	  
	public DeploymentInfo getApp(int depl) {
		if (registeredDynamicApps.containsKey(depl))
			return registeredDynamicApps.get(depl);
		else if (registeredStaticApps.containsKey(depl))
			return registeredStaticApps.get(depl);
		else
			return allApps.get(depl);
	}
	
	public LinkedList<Integer> getAllApps() {
		return allAppIDs;
	}
	
	@SuppressWarnings("static-access")
	public void setQueue(PricingModellerQueueServiceManager producer){
	//	System.out.println("Billing: producer set");
		try{
			this.producer = producer;
		}
		catch(Exception ex){
		//	System.out.println("PM: Could not set producer");
			// logger.error("PM: Could not set producer");
		}
	}
	
	//TESTED
	 public double predictCharges(DeploymentInfo deploy, HashMap<Integer, Double> energy){
		 VMinfo VM;
		 double charges=0;
		 List <Integer> vm = new ArrayList<Integer>();
		 for (int i=0; i<deploy.getNumberOfVMs();i++){
			 VM=deploy.getVM(i);
			 try{
				 VM.setProducer(producer);
			 }catch(Exception ex){
				//	System.out.println("PM: Could not set producer");
					// logger.error("PM: Could not set producer");
				}
		//	 System.out.println(VM.getProducer());
			 if (VM.schemeChange()&&(deploy.getSchemeId()!=100)){
				 VM.setSchemeID(deploy.getSchemeId());
				 VM.setScheme(deploy.getSchemeId());
			 }
			 if (VM.getSchemeID()==0){
				 charges= charges+ VM.getScheme().predictTotalCharges(VM, false);
				 vm.add(VM.getVMid());
			 }
			 else {
				 if (energy!=null){
					// System.out.println("Energy not null " +energy.get(VM.getVMid()));
					 VM.setEnergyPredicted(energy.get(VM.getVMid()));
					 charges= charges+ VM.getScheme().predictTotalCharges(VM, true);}
				 else{
				//	 System.out.println("Energy null");
				// VM.setEnergyPredicted(energy.get(VM.getVMid())*(Math.ceil(VM.getActualDuration()/3600)));
						 charges= charges+ VM.getScheme().predictTotalCharges(VM, false);}
				 vm.add(VM.getVMid());
			 }
		 }
		 try{
			 producer.sendToQueue("PMPREDICTION", deploy.getVM().getIaaSProvider().getID(), deploy.getId(), vm, Unit.PRICEHOUR, charges);
		 }
		 catch(Exception ex){
			//	System.out.println("PM: Could not send message to queue");
				// logger.error("PM: Could not set producer");
			}
		// System.out.println("charges " + charges);
		 return charges;
	 }
	
	//TESTED
	public void registerApp (int deplID, LinkedList<VMinfo> VMs, PaaSPricingModellerBilling billing) {
		DeploymentInfo app = new DeploymentInfo(deplID, billing);
		app.setVMs(VMs);
		
			for (int i=0;i<app.getNumberOfVMs();i++){
				if ((app.getVM(i).getSchemeID() == 0) || (app.getVM(i).getSchemeID() == 2)) {
					registeredStaticEnergyPricesVMs.put(app.getVM(i).getVMid(), app.getVM(i));
					app.getVM(i).resetVMTimers();
					app.getVM(i).setAppID(app.getAppID());
					app.getVM(i).setDepID(deplID);
					try{
						app.getVM(i).setProducer(producer);
					}
					catch(Exception ex){
					//	System.out.println("Registration PM: Could not set producer");
					//	 logger.error("PM: Could not set producer");
					}
					//	System.out.println("Registration: VM with ID: " + app.getVM(i).getVMid() + " has been registered in static");
	            
				} else {
					registeredDynamicEnergyPricesVMs.put(app.getVM(i).getVMid(), app.getVM(i));
				//	System.out.println("Registration: VM with ID: " + app.getVM(i).getVMid() + " has been registered in dynamic");
					app.getVM(i).setDepID(deplID);
					try{
						app.getVM(i).setProducer(producer);
					}
					catch(Exception ex){
				//		System.out.println("Registration PM: Could not set producer");
					//	 logger.error("PM: Could not set producer");
					}
					app.getVM(i).resetVMTimers();
				}
			}
			if (!allApps.containsKey(app)){
			//	System.out.println("Registration: adding app with ID: " + app.getId() );
				allApps.put(app.getId(), app);
				allAppIDs.add(app.getId());
				try{
				app.setProducer(producer);
				}
				catch(Exception ex){
			//		System.out.println("Registration PM: Could not set producer");
					// logger.error("PM: Could not set producer");
				}
				predictPriceofNextHour(app, 3600);}
	}
	
	//TESTED
	 public double predictPriceofNextHour(DeploymentInfo depl, double duration){
		 double price=0.0;
		 List <Integer> vm = new ArrayList<Integer>();
		 try{
			 for (int i=0; i<depl.getNumberOfVMs();i++){
				 int VMid = depl.getVM(i).getVMid();
				 VMinfo VM = depl.getVM(i);
				 if (VM.isActive()){
					 vm.add(VMid);    
					// System.out.println(" VM scheme" +VM.getSchemeID()+"and"+ VM.getSchemeID());
					 price = price + getVMPredictedPrice(VM, duration);
				//	 System.out.println("Registration predict next hour : updating price of VM "+VM.getVMid()+" with "+ VM.getCurrentprice());
					 // logger.error("PM: Could not set producer");
				 }
			 }	
			 try{
				 depl.setCurrentPrice(price);

				 //	 System.out.println("Billing: current price of app is "+ depl.getCurrentPrice());
				 producer.sendToQueue("PMPREDICTION", depl.getVM().getIaaSProvider().getID(), depl.getId(), vm, Unit.PRICEHOUR, depl.getCurrentPrice());
 			 
 				}
 				catch (Exception ex){
 			//		System.out.println("Registration predict next hour: Could not send the message to queue 1");
 					//	 logger.error("PM: Could not set producer");
 					//logger.info("Could not send the message to queue");
 				}
		 }
		 catch (NullPointerException e){
		//	 System.out.println("Registration predict next hour: Not a deployed application. This is used only for deployed applications");
		 }
         return price;
   
	 }
	
	//TESTED
	public double getVMPredictedPrice(VMinfo VM, double duration) {
		 PaaSPricingModellerPricingScheme scheme = VM.getScheme();
	     //  System.out.println("Billing: the scheme of VM "+VM.getVMid()+" is "+scheme.getSchemeId());
	     return scheme.getVMPredictedPrice(VM, duration);
	}
	
	//TESTED
	public void registerApp(String appID, int deplID, int schemeID) {
		DeploymentInfo app = new DeploymentInfo(appID, deplID, schemeID);
		if (app.getSchemeId()==0||app.getSchemeId()==2)
				registeredStaticApps.put(app.getId(), app);
			else
				registeredDynamicApps.put(app.getId(), app);
			
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
	
	//TESTED
	public void addVM(int deplID, VMinfo VM) {
		
		synchronized(allApps.get(deplID).getLock()){
		
		allApps.get(deplID).setChanging(true);
		allApps.get(deplID).addVM(VM);

				if ((VM.getSchemeID() == 0) || (VM.getSchemeID() == 2)) {
					registeredStaticEnergyPricesVMs.put(VM.getVMid(), VM);
					VM.resetVMTimers();
					VM.setAppID(allApps.get(deplID).getAppID());
					VM.setDepID(deplID);
					try{
						VM.setProducer(producer);
					}
					catch(Exception ex){
					//	System.out.println("PM: Could not send message to queue");
					//	 logger.error("PM: Could not set producer");
					}
			//		System.out.println("Billing: VM with ID: " + VM.getVMid() + " has been registered in static");
	            
				} else {
					registeredDynamicEnergyPricesVMs.put(VM.getVMid(), VM);
					VM.resetVMTimers();
					VM.setAppID(allApps.get(deplID).getAppID());
					VM.setDepID(deplID);
					try{
						VM.setProducer(producer);
					}
					catch(Exception ex){
					//	System.out.println("PM: Could not send message to queue");
					//	 logger.error("PM: Could not set producer");
					}
				//		System.out.println("Billing: VM with ID: " + VM.getVMid() + " has been registered in dynamic");
				}
		 allApps.get(deplID).setChanging(false);

		 allApps.get(deplID).getLock().notifyAll();
		}
		
	}

	

}