/**
 *  Copyright 2015 Athens University of Economics and Business
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


package eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.pricingschemes;

import eu.ascetic.asceticarchitecture.paas.type.AppState;



/**
 * This pricing scheme has ID==0. 
 * It gives only a static prices per hour. 
 * @author E. Agiatzidou
 */



public class PricingSchemeA extends PaaSPricingModellerPricingScheme {
	
	
	public PricingSchemeA(int id) {
		super(id);
	}

	
	
	/////////////////////////PREDICTION/////////////////////////
	@Override
	public double predictCharges(AppState app) {
		double newCharges= app.getPredictedInformation().getIaaSPredictedCharges()+0.2*app.getPredictedInformation().getIaaSPredictedCharges();
		app.setPredictedCharges(newCharges);
		return newCharges;
	}
	


////////////////////////////////// BILLING //////////////////////////
	


	@Override
	public void updateAppCharges(AppState app) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public double getTotalCharges(AppState app, double iaascharges) {
		double newCharges= iaascharges+0.2*iaascharges;
		app.setTotalCharges(newCharges);
		return newCharges;
	}



	@Override
	public double predictCharges(AppState app, double average) {
		// TODO Auto-generated method stub
		return 0;
	}

	
	
	
	
}