/*  Copyright 2015 Athens University of Economics and Business
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

package eu.ascetic.asceticarchitecture.paas.type;

import java.util.Calendar;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import eu.ascetic.asceticarchitecture.paas.type.Charges;
import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.pricingschemes.PaaSPricingModellerPricingScheme;
import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.pricingschemes.PricingSchemeA;
import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.pricingschemes.PricingSchemeB;
import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.pricingschemes.PricingSchemeC;


public class VMinfo extends VMBasic{
	static Logger logger = Logger.getLogger(PaaSPricingModellerPricingScheme.class);
	boolean dependentScheme = false;
	
	public void setDependentSchemeFalse(){
		dependentScheme = true;
	}
	
	public boolean getDependentScheme(){
		return dependentScheme;
	}
	
	public VMinfo (double RAM, double CPU, double storage, long predictedDuration){
		this.RAM = RAM/1024;
		this.CPU = CPU;
		this.storage = storage;
		this.predictedDuration=predictedDuration;
		this.scheme = initializeScheme(1);
		setDependentSchemeFalse();
		schemeToChange = true;
		time = new TimeParameters();
		energyInfo.setCurrentTotalConsumption(0.0);		
		energyCharges = new Charges();
		resourceCharges = new Charges();
		TotalCharges = new Charges();
		energyInfo.setCurrentTotalConsumption(0.0);
		IaaSProvider Prov = new IaaSProvider("0");
		IaaSProviders.put("0", Prov);
		setIaaSProvider(IaaSProviders.get("0"));
	//	System.out.println("PaaS Pricing Modeller: This is VMinfo 1");
	}
	


	/*
	public VMinfo (double RAM, double CPU, double storage, long duration, int scheme){
		this.RAM = RAM/1024;
		this.CPU = CPU;
		this.storage = storage/1000;
		this.actualDuration=duration;
		this.schemeID = scheme;
		time = new TimeParameters();
		this.scheme = initializeScheme(schemeID);
		energyInfo.setCurrentTotalConsumption(0.0);
	}
	
*/
	
	///For prediction
	public VMinfo (int VMid, double RAM, double CPU, double storage, long predictedDuration, int scheme, String IaaSProviderID){
		this.VMid= VMid; 
		this.RAM = RAM/1024;
		this.CPU = CPU;
		this.storage = storage;
		this.predictedDuration=predictedDuration;
		this.schemeID = scheme;
		time = new TimeParameters();
		this.scheme = initializeScheme(schemeID);
		energyCharges = new Charges();
		resourceCharges = new Charges();
		TotalCharges = new Charges();
		energyInfo.setCurrentTotalConsumption(0.0);
		IaaSProvider Prov = new IaaSProvider(IaaSProviderID);
		IaaSProviders.put(IaaSProviderID, Prov);
		setIaaSProvider(IaaSProviders.get(IaaSProviderID));
		logger.info("PaaS Pricing Modeller: This is VMinfo 2");
	}
	
	
	//For deployment
	public VMinfo (int VMid, double RAM, double CPU, double storage, int scheme, String IaaSProviderID){
		this.VMid= VMid; 
		this.RAM = RAM/1024;
		this.CPU = CPU;
		this.storage = storage;
	//	this.storage = storage/1000;
		this.schemeID = scheme;
		time = new TimeParameters();
		this.scheme = initializeScheme(schemeID);
		energyInfo.setCurrentTotalConsumption(0.0);
		IaaSProvider Prov = new IaaSProvider(IaaSProviderID);
		IaaSProviders.put(IaaSProviderID, Prov);
		setIaaSProvider(IaaSProviders.get(IaaSProviderID));
		energyCharges = new Charges();
		resourceCharges = new Charges();
		TotalCharges = new Charges();
		this.predictedDuration=3600;
	//	System.out.println("VMinfo: VM with id = " + VMid+" Time has started at " + time.getStartTime().getTimeInMillis()+ " end time is " + time.getEndTime().getTimeInMillis());
		logger.info("PaaS Pricing Modeller: This is VMinfo 3");
		
	}
	
	public void updateEnergyConsumption(double energy){
		 energyInfo.setCurrentTotalConsumption(energy);
	}
//	
	//public double getEnergyConsumptionofLastPeriod(){
	//	 return energyInfo.getEnergyConsumedAfterUpdate();
	//}
		
	public void setEnergyPredicted(double energy){
		energyInfo.setEnergyPredicted(energy);
	}
	public double getEnergyPredicted(){
		return energyInfo.getEnergyPredicted();
	}
	
}