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









import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.types.Charges;
import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.pricingschemes.PaaSPricingModellerPricingScheme;
import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.pricingschemes.PricingSchemeA;
import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.pricingschemes.PricingSchemeB;
import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.pricingschemes.PricingSchemeC;


public class VMinfo extends VMBasic{

	
	public VMinfo (double RAM, double CPU, double storage, long duration){
		
		this.RAM = RAM/1024;
		this.CPU = CPU;
		this.storage = storage/1000;
		this.actualDuration=duration;
		time = new TimeParameters();
		this.scheme = initializeScheme(0);
		energyInfo.setCurrentTotalConsumption(0.0);
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
	public VMinfo (int VMid, double RAM, double CPU, double storage, long duration, int scheme, int IaaSProviderID){
		this.VMid= VMid; 
		this.RAM = RAM/1024;
		this.CPU = CPU;
		this.storage = storage/1000;
		this.actualDuration=duration;
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
	}
	
	
	//For deployment
	public VMinfo (int VMid, double RAM, double CPU, double storage, int scheme, int IaaSProviderID){
		this.VMid= VMid; 
		this.RAM = RAM/1024;
		this.CPU = CPU;
		this.storage = storage/1000;
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
	}
	
	public void updateEnergyConsumption(double energy){
		 energyInfo.setCurrentTotalConsumption(energy);
	}
	
	public double getEnergyConsumptionofLastPeriod(){
		 return energyInfo.getEnergyConsumedAfterUpdate();
	}
		
	public void setEnergyPredicted(double energy){
		energyInfo.setEnergyPredicted(energy);
	}
	
	
}