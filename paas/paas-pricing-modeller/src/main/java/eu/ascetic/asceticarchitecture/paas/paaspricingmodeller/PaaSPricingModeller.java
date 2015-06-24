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
package eu.ascetic.asceticarchitecture.paas.paaspricingmodeller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.io.IOException;

import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.pricingschemesrepository.IaaSPricingModellerPricingScheme;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.pricingschemesrepository.PricingSchemeA;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.pricingschemesrepository.PricingSchemeB;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.types.VMstate;
import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.billing.PaaSPricingModellerBilling;
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


public class PaaSPricingModeller implements PaaSPricingModellerInterface{

	PaaSPricingModellerBilling billing = new PaaSPricingModellerBilling();
	
	
	public PaaSPricingModeller(){
	}
	
	public void initializeApp(int deplID, int schemeId){
		DeploymentInfo deployment = new DeploymentInfo(deplID, schemeId);
		billing.registerApp(deployment);
	}
	
	public void initializeApp(int deplID){
		billing.registerApp(deplID);
	}
	
	
	
/////////////////////////////////////////BASED ON CALCULATIONS FROM IAAS LAYER///////////////////////////////////
	public double getAppPredictedCharges(int deplID, int schemeID, double IaaSCharges){
		DeploymentInfo deployment = new DeploymentInfo(deplID, schemeID);
		deployment.setIaaSPredictedCharges(IaaSCharges);
		double charges = billing.predictCharges(deployment);
		return charges;
		
	}
	
	public double getAppTotalCharges(int deplID, int schemeID, double IaaSCharges){
		return billing.getAppCurrentTotalCharges(deplID, IaaSCharges);
	}
}