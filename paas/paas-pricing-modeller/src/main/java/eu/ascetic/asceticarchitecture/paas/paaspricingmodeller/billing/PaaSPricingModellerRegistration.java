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


public class PaaSPricingModellerRegistration {
	
	static HashMap<Integer,DeploymentInfo> registeredStaticApps = new HashMap<Integer,DeploymentInfo>();
	static HashMap<Integer,DeploymentInfo> registeredDynamicApps = new HashMap<Integer,DeploymentInfo>();
	static HashMap<Integer,DeploymentInfo> allApps = new HashMap<Integer,DeploymentInfo>();
	
	static HashMap<Integer, VMinfo> registeredStaticEnergyPricesVMs = new HashMap<Integer, VMinfo>();
    static HashMap<Integer, VMinfo> registeredDynamicEnergyPricesVMs = new HashMap<Integer, VMinfo>();
	//static HashMap<Integer, DeploymentInfo> deployments = new HashMap<Integer, DeploymentInfo>();
	
	double averagePrice=1;
	
	public DeploymentInfo getApp(int depl) {
		if (registeredDynamicApps.containsKey(depl))
			return registeredDynamicApps.get(depl);
		else
			return registeredStaticApps.get(depl);
	}
	
	public void registerApp(int deplID, LinkedList<VMinfo> VMs) {
		DeploymentInfo app = new DeploymentInfo(deplID);
		app.setVMs(VMs);
		
			for (int i=0;i<app.getNumberOfVMs();i++){
				if ((app.getVM(i).getSchemeID() == 0) || (app.getVM(i).getSchemeID() == 2)) {
					registeredStaticEnergyPricesVMs.put(app.getVM(i).getVMid(), app.getVM(i));
					//System.out.println("Billing: VM with ID: " + app.getVM(i).getVMid() + " has been registered in static");
	            
				} else {
					registeredDynamicEnergyPricesVMs.put(app.getVM(i).getVMid(), app.getVM(i));
					//System.out.println("Billing: VM with ID: " + app.getVM(i).getVMid() + " has been registered in dynamic");
				}
	      //  vm.setStartTime();
				allApps.put(app.getId(), app);
			}

	}
	
	public void registerApp(int deplID, int schemeID) {
		DeploymentInfo app = new DeploymentInfo(deplID, schemeID);
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
	
	public void addVM(int deplID, VMinfo VM) {
		allApps.get(deplID).addVM(VM);

				if ((VM.getSchemeID() == 0) || (VM.getSchemeID() == 2)) {
					registeredStaticEnergyPricesVMs.put(VM.getVMid(), VM);
				//	System.out.println("Billing: VM with ID: " + VM.getVMid() + " has been registered in static");
	            
				} else {
					registeredDynamicEnergyPricesVMs.put(VM.getVMid(), VM);
				//	System.out.println("Billing: VM with ID: " + VM.getVMid() + " has been registered in dynamic");
				}
		
	}

	

}