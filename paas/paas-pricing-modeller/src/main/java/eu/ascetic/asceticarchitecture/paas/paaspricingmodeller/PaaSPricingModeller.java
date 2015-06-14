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

import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.billing.PaaSPricingModellerBilling;
import eu.ascetic.asceticarchitecture.paas.type.AppState;
import eu.ascetic.asceticarchitecture.paas.type.DeploymentInfo;
import eu.ascetic.asceticarchitecture.paas.type.IaaSProvider;
import eu.ascetic.asceticarchitecture.paas.type.PaaSPrice;
import eu.ascetic.asceticarchitecture.paas.type.VMinfo;

;

/**
 * This is the main interface of the pricing modeller of PaaS layer.
 * Functionality: The ability to provide a price estimation of an application
 * per hour, given the energy consumed of this app, the deployment id, the
 * application id, the IaaS provider id and the IaaS provider's price.
 * 
 * The price estimation can be also given without the provision of energy
 * estimation.
 * 
 * The price estimation can be also given without the provision of an PaaS
 * price.
 * 
 * @author E. Agiatzidou
 */

public class PaaSPricingModeller implements PaaSPricingModellerInterface {

	IaaSProvider iaasProvider;

	PaaSPricingModellerBilling billing = new PaaSPricingModellerBilling();

	private static int idIaaPP = 0;

	public PaaSPricingModeller() {
		idIaaPP = idIaaPP + 1;
	}
	
	public void createDeployment(int deploymentId, double RAM, double CPU, double storage){
		billing.createDeployment(deploymentId, RAM, CPU,  storage);
	}

	// /////////////////////////////////////PREDICTION////////////////////////////////////////////

	public double getAppChargesPrediction(int appID, int deploymentId, int IaaSId, double totalAppCharges, int schemeId,
			double totalEnergyPredicted, double duration) {
		
		DeploymentInfo delp = billing.getDepl(deploymentId);
		
		AppState app = new AppState(appID, delp, IaaSId, schemeId);
		
		return billing.predictAppCharges(app, totalAppCharges, duration,
				totalEnergyPredicted);
	}

	public double getAppChargesPrediction(int appID, int deploymentId,
		int IaaSId, int schemeId, double totalEnergyPredicted, double duration) {

		DeploymentInfo delp = billing.getDepl(deploymentId);
		AppState app = new AppState(appID, delp, IaaSId, schemeId);
		return billing.predictAppCharges(app, duration, totalEnergyPredicted);
				
	}
	
	//NOT for Y1
	public double chooseIaaS(int appID, int deploymentId, int schemeId, double totalEnergyPredicted, double duration) {

			//AppState app = new AppState(appID, deploymentId, IaaSId, schemeId);
			//return billing.predictAppCharges(app, totalAppCharges, duration, totalEnergyPredicted);
					return 0;
		}
	
	//TO DECIDE
	public double chooseIaaSPricingScheme(int appID, int deploymentId, int IaaSid, double totalEnergyPredicted, double duration) {

		//AppState app = new AppState(appID, deploymentId, IaaSId, schemeId);
		//return billing.predictAppCharges(app, totalAppCharges, duration, totalEnergyPredicted);
				return 0;
	}
	
	// /////////////////////////////////////////BILLING/////////////////////////////////////////////

	public void initializeApp(int appID, int deploymentId, int IaaSId,
			int schemeId) {
		DeploymentInfo delp = billing.getDepl(deploymentId);
		AppState app = new AppState(appID, delp, IaaSId, schemeId);
		billing.registerApp(app);
	}

	public void initializeApp(int appID) {
		billing.registerApp(appID);
	}

public double getAppCurrentCharges(int appID, double iaasCharges) {
		return billing.getAppCharges(appID, iaasCharges);
	}

	public double getAppFinalCharges(int appID, double iaasCharges, boolean unregister) {
		return billing.getAppCharges(appID,iaasCharges, unregister);
	}
	
	
	// /////////////////////Basic functions///////////////////////////////
	public int getPaaSId() {
		return idIaaPP;
	}

	public IaaSProvider getIaaSProvider() {
		return this.iaasProvider;
	}

	public PaaSPricingModellerBilling getBilling() {
		return this.billing;
	}

}