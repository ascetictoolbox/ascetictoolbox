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

import org.apache.log4j.Logger;

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


public class PaaSPricingModellerBillingEvents extends PaaSPricingModellerBilling{
	
/*	public PaaSPricingModellerBillingEvents(){
		Logger logger = Logger.getLogger(PaaSPricingModellerBillingEvents.class);
	}
	*/
//////////////////////////////////Event Charges//////////////////////

	public double predictEventCharges(DeploymentInfo deploy){
		if (deploy.getSchemeId()==0||deploy.getSchemeId()==2){
			double charges = deploy.getIaaSProvider().predictResourcesCharges(deploy.getVM(), deploy.getPredictedInformation().getPredictedDuration(), deploy.getIaaSProvider().getPriceSec(deploy.getIaaSProvider().getStaticResoucePrice()));
			//charges= charges+0.2*charges;
			deploy.setPredictedCharges(charges);
			return deploy.getPredictedCharges();
		}
		
		if (deploy.getSchemeId()==1){
			double a = deploy.getIaaSProvider().predictResourcesCharges(deploy.getVM(), deploy.getPredictedInformation().getPredictedDuration(), deploy.getIaaSProvider().getPriceSec(deploy.getIaaSProvider().getResoucePrice()));
			double b = deploy.getIaaSProvider().predictEnergyCharges(deploy.getVM().getEnergyPredicted(), deploy.getIaaSProvider().getAverageEnergyPrice());
			double charges = a+b;
		//	charges= charges+0.2*charges;
			deploy.setPredictedCharges(charges);
			return deploy.getPredictedCharges();
		}
		
		return 0.0;		
	}

	//TESTED
	public double predictAppEventCharges(DeploymentInfo deploy) {
		double temp =0;
		if (deploy.getSchemeId()==0||deploy.getSchemeId()==2){
			double charges =0;
			for (int i=0; i<deploy.getNumberOfVMs(); i++){
				temp = deploy.getIaaSProvider().predictResourcesCharges(deploy.getVM(i), deploy.getVM(i).getPredictedDuration(),  deploy.getIaaSProvider().getPriceSec(deploy.getIaaSProvider().getStaticResoucePrice()));
				charges = charges+temp;
			}
		//	charges= charges+0.2*charges;
			deploy.setPredictedCharges(charges);
			return deploy.getPredictedCharges();
		}
		
		if (deploy.getSchemeId()==1){
			double charges =0;
			for (int i=0; i<deploy.getNumberOfVMs(); i++){
				double a = deploy.getIaaSProvider().predictResourcesCharges(deploy.getVM(i), deploy.getVM(i).getPredictedDuration(), deploy.getIaaSProvider().getPriceSec(deploy.getIaaSProvider().getResoucePrice()));
				charges = charges+a;
			}
			double b = deploy.getIaaSProvider().predictEnergyCharges(deploy.getEnergy(), deploy.getIaaSProvider().getAverageEnergyPrice());
			charges = charges +b;
			//charges= charges+0.2*charges;
			deploy.setPredictedCharges(charges);
			return deploy.getPredictedCharges();
		}
		
		return 0.0;		
	}
	
	//TESTED
	public double predictAppEventChargesVMbased(DeploymentInfo deploy) {
		return predictAppCharges(deploy, deploy.getEnergy());
	}
		/*if (deploy.getVM(i).getSchemeID()==0||deploy.getVM(i).getSchemeID()==2){
			 	//////////////////////////////////
			
				temp = deploy.getIaaSProvider().predictResourcesCharges(deploy.getVM(i), deploy.getVM(i).getPredictedDuration(),  deploy.getIaaSProvider().getStaticResoucePrice());
				charges = charges+temp;
			charges= charges+0.2*charges;
			deploy.setPredictedCharges(charges);
		
		}
		
		if (deploy.getVM(i).getSchemeID()==1){
			
			double a = deploy.getIaaSProvider().predictResourcesCharges(deploy.getVM(i), deploy.getVM(i).getPredictedDuration(), deploy.getIaaSProvider().getResoucePrice());
			charges = charges+a;
			double b = deploy.getIaaSProvider().predictEnergyCharges(deploy.getEnergy(), deploy.getIaaSProvider().getAverageEnergyPrice());
			charges = charges +b;
			charges= charges+0.2*charges;
			deploy.setPredictedCharges(charges);
			
		}
		
			
		}
		return deploy.getPredictedCharges();
	}
	*/

	private double predictAppCharges(DeploymentInfo deploy, double energy) {
		double charges =0;
		for (int i=0; i<deploy.getNumberOfVMs(); i++){
			if((deploy.getVM(i).getSchemeID()==0)||(deploy.getVM(i).getSchemeID()==1)){
				VMinfo VM = deploy.getVM(i);
				double duration = deploy.getVM(i).getPredictedDuration();
			//	System.out.println(deploy.getIaaSProvider());
				double price =  deploy.getIaaSProvider().getPriceSec(deploy.getIaaSProvider().getStaticResoucePrice());
				charges = charges+ deploy.getVM(i).getIaaSProvider().predictResourcesCharges(VM, duration, price);	
		//		System.out.println("here");
			}
		else
			{
		//	System.out.println("here2");
			charges = charges+ deploy.getVM(i).getIaaSProvider().predictResourcesCharges(deploy.getVM(i), deploy.getVM(i).getPredictedDuration(), deploy.getIaaSProvider().getPriceSec(deploy.getIaaSProvider().getResoucePrice()));	
			}

		}
	//	System.out.println(charges);
		double b = deploy.getIaaSProvider().predictEnergyCharges(deploy.getEnergy(), deploy.getIaaSProvider().getAverageEnergyPrice());
		charges = charges +b;
	//	System.out.println("with energy"+charges);
		deploy.setPredictedCharges(charges);
		return deploy.getPredictedCharges();

	}
}