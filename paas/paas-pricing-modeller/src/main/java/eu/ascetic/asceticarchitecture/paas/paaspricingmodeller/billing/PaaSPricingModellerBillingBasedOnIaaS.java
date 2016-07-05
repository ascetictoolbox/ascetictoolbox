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


public class PaaSPricingModellerBillingBasedOnIaaS extends PaaSPricingModellerRegistration implements PaaSPricingModellerBillingInterface{
	
	 
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
	
public double getAppCurrentTotalCharges(DeploymentInfo deploy, double charges){
		deploy.setIaaSTotalCurrentCharges(charges);
		double totalcharges = charges + 0.2*charges;
		deploy.setTotalCharges(totalcharges);
		return deploy.getTotalCharges();
	}


}