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





import java.lang.Math;

/**
 * 
 * @author E. Agiatzidou
 */

public abstract class PaaSPricingModellerPricingScheme {
	
	private static final int E = 0;
	
	int scheme;
	
	//IaaSPricingModellerCost cost = new IaaSPricingModellerCost();


	//////////////////////////////UPDATE THE CHARGES OF VM ////////////////////////////
	//public abstract void updateAppCharges(AppState app);
		


	
	
	//////////////////GENERAL //////////////////////////////////
	public PaaSPricingModellerPricingScheme(int id){
		scheme=id;
		
	}
	
	public int getSchemeId(){
		return scheme;
	}



	//public abstract double predictCharges(AppState app);


	//public abstract double getTotalCharges(AppState app);

	
}