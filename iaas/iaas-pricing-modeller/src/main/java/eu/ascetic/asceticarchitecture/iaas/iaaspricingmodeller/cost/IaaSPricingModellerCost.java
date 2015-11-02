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
import eu.ascetic.asceticarchitecture.iaas.energymodeller.EnergyModeller;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.TimePeriod;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VmDeployed;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.energyprovider.EnergyProvider;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.pricingschemesrepository.IaaSPricingModellerPricingScheme;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.types.Price;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.types.TimeParameters;
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
	
	Price price;
	EnergyModeller energyModeller;

	public IaaSPricingModellerCost(EnergyModeller energyModeller){
		this.energyModeller = energyModeller;
	
	}
	
	
	/////////////////////////////UPDATE OF COSTS ////////////////////////////////
	
	public double updateEnergyCharges(VMstate VM){
		price=getEnergyPrice(VM.getProvider(), VM.getPricingScheme().getSchemeId());
		//the energy charges for the past period
		double energyCharges = updateEnergy(VM)*price.getPriceOnly();
		return energyCharges;
	}
	
	
	private Price getEnergyPrice(EnergyProvider provider, int scheme){
		if (scheme == 0){
			return provider.getStaticEnergyPrice();
		}
		else {
		    return provider.getOldDynamicEnergyPrice();
			}
	}

	public double updateEnergy(VMstate VM){
		
		VmDeployed vm = energyModeller.getVM(VM.getVMid());
		TimePeriod timePeriod = new TimePeriod(VM.getStartTime(), VM.getChangeTime());
		double newEnergyValue = energyModeller.getEnergyRecordForVM(vm, timePeriod).getTotalEnergyUsed();
		VM.setTotalEnergyConsumed(newEnergyValue);
		double difference = newEnergyValue-VM.getEnergyConsumedLast();
		
		VM.setEnergyConsumedLast(newEnergyValue);
		
		return difference;

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