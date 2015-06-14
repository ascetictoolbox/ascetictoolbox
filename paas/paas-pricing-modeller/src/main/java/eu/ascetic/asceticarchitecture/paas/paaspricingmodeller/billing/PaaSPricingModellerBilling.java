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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.io.IOException;

import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.pricingschemes.PaaSPricingModellerPricingScheme;
import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.pricingschemes.PricingSchemeA;
import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.pricingschemes.PricingSchemeB;
import eu.ascetic.asceticarchitecture.paas.type.AppState;
import eu.ascetic.asceticarchitecture.paas.type.DeploymentInfo;
import eu.ascetic.asceticarchitecture.paas.type.PaaSPrice;
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


public class PaaSPricingModellerBilling implements PaaSPricingModellerBillingInterface{

	LinkedList<AppState> queue = new LinkedList<AppState>();
	
	static HashMap<Integer,AppState> registeredStaticApps = new HashMap<Integer,AppState>();
	static HashMap<Integer,AppState> registeredDynamicApps = new HashMap<Integer,AppState>();
	
	static HashMap<Integer, DeploymentInfo> deployments = new HashMap<Integer, DeploymentInfo>();
	
	double averagePrice=1;
	
	public void registerApp(AppState app) {
		if (app.getProvider().getScheme()==0)
			registeredStaticApps.put(app.getAppId(), app);
		else
			registeredDynamicApps.put(app.getAppId(), app);
		
	}
	
	
	public void registerApp(int id) {
		ListIterator<AppState> listIterator = queue.listIterator();
		AppState app;
		boolean found = false;
        while (listIterator.hasNext()) {
        	app = listIterator.next();
            if (app.getAppId()==id){
            	if ((app.getProvider().getScheme()==0)){
            		registeredStaticApps.put(app.getAppId(), app);
        		}
        		else {
        			registeredDynamicApps.put(app.getAppId(), app);
        		}
            	found =true;
            }
		}
        if (found==false)
        	System.out.println("App has not been found");
	}
	
	public void unregisterApp(AppState app) {
		if ((app.getProvider().getScheme()==0)){
			registeredStaticApps.remove(app.getAppId());
			app.setEndTime();}
		else{
			registeredDynamicApps.remove(app.getAppId());
			app.setEndTime();}
		
	}


	public void stopApp(AppState app) {
		app.setEndTime();
		
	}
	
	
	// ////////////////////////////// FOR PREDICTION // /////////////////////////////////
	
	public double predictAppCharges(AppState app, double totalAppCharges, double duration,double totalEnergyPredicted) {
		 queue.push(app);
		 if (queue.size()>10)
			 queue.pollLast();
		 PaaSPricingModellerPricingScheme scheme;
		app.getPredictedInformation().setIaaSPredictedCharges(totalAppCharges);
		app.getPredictedInformation().setPredictedEnergy(totalEnergyPredicted);
		app.getPredictedInformation().setDuration(duration);
		if (app.getProvider().getScheme() == 1)
			 scheme = new PricingSchemeA(1);
		else 
			scheme = new PricingSchemeB(2);
		scheme.predictCharges(app);
		return app.getPredictedCharges();

	}

	public double predictAppCharges(AppState app, double duration, double totalEnergyPredicted) {
		queue.push(app);
		 if (queue.size()>10)
			 queue.pollLast();
		 PaaSPricingModellerPricingScheme scheme;
		 app.getPredictedInformation().setPredictedEnergy(totalEnergyPredicted);
			app.getPredictedInformation().setDuration(duration);
			if (app.getProvider().getScheme() == 1)
				 scheme = new PricingSchemeA(1);
			else 
				scheme = new PricingSchemeB(2);
			
			scheme.predictCharges(app, averagePrice);
		return 0;
	}
	

	// ///////////////////////////////BILLING////////////////////////////////////

	public double getAppCharges(int appID, double iaasCharges) {
		PaaSPricingModellerPricingScheme scheme; 
		if (getApp(appID).getProvider().getScheme()==1)
			scheme = new PricingSchemeA(1);
		else 
			scheme = new PricingSchemeB(2);
			
		return scheme.getTotalCharges(getApp(appID), iaasCharges);
	}
	
	public double getAppCharges(int appID, double iaasCharges, boolean b) {
		PaaSPricingModellerPricingScheme scheme; 
		if (getApp(appID).getProvider().getScheme()==1)
			scheme = new PricingSchemeA(1);
		else 
			scheme = new PricingSchemeB(2);
		double charges = scheme.getTotalCharges(getApp(appID), iaasCharges);
		if (b)
			unregisterApp(getApp(appID));
		else
			stopApp(getApp(appID));
		return charges;
	}


	private AppState getApp(int appID) {
		if (registeredDynamicApps.containsKey(appID))
			return registeredDynamicApps.get(appID);
		else
			return registeredStaticApps.get(appID);
	}


	public void createDeployment(int deploymentId, double rAM, double cPU, 
			double storage) {
		VMinfo vm = new VMinfo(rAM, cPU,storage);
		if (deployments.containsKey(deploymentId))
			deployments.get(deploymentId).addVM(vm);
		else {
			DeploymentInfo depl = new DeploymentInfo(deploymentId);
			deployments.put(deploymentId, depl);
		}
	}


	public DeploymentInfo getDepl(int id){
		if (deployments.containsKey(id))
			return deployments.get(id);
		else 
			return null;
	}
	
	
}