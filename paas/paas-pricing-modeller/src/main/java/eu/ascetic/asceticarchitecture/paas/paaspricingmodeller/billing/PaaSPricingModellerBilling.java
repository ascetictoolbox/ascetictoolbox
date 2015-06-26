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
import eu.ascetic.asceticarchitecture.paas.type.DeploymPredInfo;
import eu.ascetic.asceticarchitecture.paas.type.DeploymentInfo;
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

	LinkedList<DeploymentInfo> queue = new LinkedList<DeploymentInfo>();
	
	static HashMap<Integer,DeploymentInfo> registeredStaticApps = new HashMap<Integer,DeploymentInfo>();
	static HashMap<Integer,DeploymentInfo> registeredDynamicApps = new HashMap<Integer,DeploymentInfo>();
	
	//static HashMap<Integer, DeploymentInfo> deployments = new HashMap<Integer, DeploymentInfo>();
	
	double averagePrice=1;
	
	public void registerApp(DeploymentInfo app) {
		if (app.getSchemeId()==0)
			registeredStaticApps.put(app.getId(), app);
		else
			registeredDynamicApps.put(app.getId(), app);
		
	}
	
	
	public void registerApp(int id) {
		ListIterator<DeploymentInfo> listIterator = queue.listIterator();
		DeploymentInfo app;
		boolean found = false;
        while (listIterator.hasNext()) {
        	app = listIterator.next();
            if (app.getSchemeId()==id){
            	if ((app.getSchemeId()==0)){
            		registeredStaticApps.put(app.getId(), app);
        		}
        		else {
        			registeredDynamicApps.put(app.getId(), app);
        		}
            	found =true;
            }
		}
        if (found==false)
        	System.out.println("App has not been found");
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
	
	////////////////////////////////// Based on IaaS Calculations//////////////////////
	
	public double predictCharges(DeploymentInfo deploy){
		queue.push(deploy);
		if (queue.size() > 10)
			queue.pollLast();
		double charges = deploy.getPredictedInformation().getIaaSPredictedCharges();
		charges = charges+0.2*charges;
		deploy.setPrediction(charges);
		return deploy.getPredictedCharges();
	}

	public double getAppCurrentTotalCharges(int depl, double charges){
		DeploymentInfo deploy = getApp(depl);
		deploy.setIaaSTotalCurrentCharges(charges);
		double totalcharges = charges + 0.2*charges;
		deploy.setTotalCurrentCharges(totalcharges);
		return deploy.getTotalCurrentCharges();
	}
	
	
	private DeploymentInfo getApp(int depl) {
		if (registeredDynamicApps.containsKey(depl))
			return registeredDynamicApps.get(depl);
		else
			return registeredStaticApps.get(depl);
	}

}