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


package eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.cost;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.energyprovider.EnergyProvider;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.pricingschemesrepository.IaaSPricingModellerPricingScheme;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.types.Price;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.types.Time;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.types.VMinfo;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.types.VMstate;

import java.io.ObjectInputStream.GetField;
import java.util.*;

import org.omg.CORBA.ValueMemberHelper;


/**
 * 
 * @author E. Agiatzidou
 */

public class  IaaSPricingModellerCost implements IaaSPricingModellerCostInterface {
	
	//ArrayList <EnergyProvider> arrayOfProviders = new ArrayList <EnergyProvider>();
	
	//Map mapOfProviders = new HashMap();

	Price price;


	public IaaSPricingModellerCost(){
	}
	
	
	/////////////////////////////UPDATE OF COSTS ////////////////////////////////
	
	public double updateEnergyCharges(VMstate VM){
		//System.out.println("Cost: The VMid is "+VM.getVMid() +"and the price is " +VM.getProvider().getDynamicEnergyPrice().getEnergyPrice().getEnergyPriceOnly());
		price=getEnergyPrice(VM.getProvider(), VM.getPricingScheme().getSchemeId());
		//the energy charges for the past period
		double energyCharges = updateEnergy(VM)*price.getPriceOnly();
		System.out.println("New energy Charges "+energyCharges);
		return energyCharges;
	}
	
	
	private Price getEnergyPrice(EnergyProvider provider, int scheme){
		if (scheme == 0){
			System.out.println("Static Cost: The price is "+provider.getStaticEnergyPrice().getPriceOnly());
			return provider.getStaticEnergyPrice();
		}
		else {
			System.out.println("Dynamic Cost: The price is "+provider.getOldDynamicEnergyPrice().getPriceOnly());
			return provider.getOldDynamicEnergyPrice();
			}
	}
	
	///to be implemented
	private double updateEnergy(VMstate VM){
		double newEnergyValue = 5;
		//this should ask for the energy consumed the VMManager. It takes the total energy consumption from the beginning of the life of the VM
		//double newEnergyValue = getEnergyConsumption(VM.getVMid());
		System.out.println("VM's last energy consumption "+VM.getEnergy());
		return newEnergyValue-VM.getEnergy();
	}
	
	
	
	////////////////// UPDATE RESOURCES CHARGES IF VM IS CHANGING ////////////////////
		
	public void updateDynamicResourcesCharges(VMstate VM, VMinfo vm) {
	//	VM.setNewVMinfo(vm);
	//	VM.getPricingScheme().
	//	Time oldTime = VM.getResourcesChangeTime();
	//	Time newTime = new Time();
		//VM.setResourcesChangeTime(newTime);
	//	double staticCharges = VM.getVMinfo().getStaticPrice()*(newTime.difTime(oldTime));
		//VM.setResourcesCharges(staticCharges);	
		
	}


		
}