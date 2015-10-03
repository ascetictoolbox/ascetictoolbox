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
import eu.ascetic.asceticarchitecture.paas.type.DeploymentInfo;




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
	
	static HashMap<Integer,DeploymentInfo> registeredStaticApps = new HashMap<Integer,DeploymentInfo>();
	static HashMap<Integer,DeploymentInfo> registeredDynamicApps = new HashMap<Integer,DeploymentInfo>();
	
	//static HashMap<Integer, DeploymentInfo> deployments = new HashMap<Integer, DeploymentInfo>();
	
	double averagePrice=1;
	
	public void registerApp(DeploymentInfo app) {
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
	
	////////////////////////////////// Based on IaaS Calculations//////////////////////
	
	public double predictCharges(DeploymentInfo deploy){
		double charges = deploy.getPredictedInformation().getIaaSPredictedCharges();
		charges = charges+0.2*charges;
		deploy.setPredictedCharges(charges);
		return deploy.getPredictedCharges();
	}
	
	public double predictPrice(DeploymentInfo deploy){
		double charges = deploy.getPredictedInformation().getIaaSPredictedCharges();
		if (deploy.getPredictedInformation().getPredictedDuration()<3600){
			deploy.getPredictedInformation().setDuration(3600);
		}
		charges = (charges+0.2*charges)/(deploy.getPredictedInformation().getPredictedDuration()/3600);
		deploy.setPredictedPrice(charges);
		return deploy.getPredictedInformation().getPredictedPrice();
	}
	
	public double predictEventCharges(DeploymentInfo deploy){
		if (deploy.getSchemeId()==0||deploy.getSchemeId()==2){
			double charges = deploy.getIaaSProvider().predictResourcesCharges(deploy.getVM(), deploy.getPredictedInformation().getPredictedDuration(), deploy.getIaaSProvider().getStaticResoucePrice(), deploy.getVM().getNumberOfEvents());
			charges= charges+0.2*charges;
			deploy.setPredictedCharges(charges);
			return deploy.getPredictedCharges();
		}
		
		if (deploy.getSchemeId()==1){
			double a = deploy.getIaaSProvider().predictResourcesCharges(deploy.getVM(), deploy.getPredictedInformation().getPredictedDuration(), deploy.getIaaSProvider().getResoucePrice(), deploy.getVM().getNumberOfEvents());
			double b = deploy.getIaaSProvider().predictEnergyCharges(deploy.getVM().getEnergyPredicted(), deploy.getIaaSProvider().getAverageEnergyPrice());;
			double charges = a+b;
			charges= charges+0.2*charges;
			deploy.setPredictedCharges(charges);
			return deploy.getPredictedCharges();
		}
		
		return 0.0;		
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