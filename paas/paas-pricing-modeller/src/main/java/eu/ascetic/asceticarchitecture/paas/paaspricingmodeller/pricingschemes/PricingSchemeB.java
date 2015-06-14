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

import java.util.concurrent.TimeUnit;

import eu.ascetic.asceticarchitecture.paas.type.AppState;
import eu.ascetic.asceticarchitecture.paas.type.Charges;


/**
 * 
 * @author E. Agiatzidou
 */

public class PricingSchemeB extends PaaSPricingModellerPricingScheme {

	
	public PricingSchemeB(int id) {
		super(id);
	}

/////////////////////////PREDICT CHARGES ///////////////////////////////
	@Override
	public double predictCharges(AppState app){
		//Charges a = predictEnergyCharges(app, average);
		//System.out.println("B: The energy charges are " + a);
		Charges b = predictResourcesCharges(app);
		System.out.println("B: The resource charges are " + b); 
		return (b.getChargesOnly());
	}
	
	public Charges predictEnergyCharges(AppState app, double average){
			Charges charges = new Charges();
			charges.setCharges(app.getPredictedInformation().getPredictedEnergy()*average);
			return charges;
	}
	
	
	public Charges predictResourcesCharges(AppState app) {
		Charges b = new Charges();
		double newCharges = app.getPredictedInformation().getIaaSPredictedCharges()+0.2*app.getPredictedInformation().getIaaSPredictedCharges();
		app.setPredictedCharges(newCharges);
		return b;
	}

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