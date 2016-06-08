/**
 * Copyright 2015 Athens University of Economics and Business
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
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
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.types.Price;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.types.VMinfo;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.types.VMstate;
import org.apache.log4j.Logger;

/**
 *
 * @author E. Agiatzidou
 */
public class IaaSPricingModellerCost implements IaaSPricingModellerCostInterface {

    Price price;
    EnergyModeller energyModeller;
    static Logger logger = null;

    public IaaSPricingModellerCost(EnergyModeller energyModeller) {
        this.energyModeller = energyModeller;
        logger = Logger.getLogger(IaaSPricingModellerCost.class);
    }

	
    /////////////////////////////UPDATE OF COSTS ////////////////////////////////
	
    public double updateEnergyCharges(VMstate VM, int IaaSID) {
        price = getEnergyPrice(VM.getProvider(), VM.getPricingScheme().getSchemeId());
        //the energy charges for the past period
        double energyCharges = (updateEnergy(VM)/1000) * price.getPriceOnly();
    //    System.out.println("I am updating energy with this price "+price.getPriceOnly());
        return energyCharges;
    }

	
    private Price getEnergyPrice(EnergyProvider provider, int scheme) {
        if (scheme == 0) {
            return provider.getStaticEnergyPrice();
        } else {
        	if (provider.getFlagForChargesUpdated())
        		return provider.getNewDynamicEnergyPrice();
        	else{
        		provider.setFlagForChargesUpdated(true);
            	return provider.getOldDynamicEnergyPrice();}
        }
    }

    public double updateEnergy(VMstate VM) {
        double difference;
        try {
        	
            VmDeployed vm = energyModeller.getVM(VM.getVMid());
            TimePeriod timePeriod = new TimePeriod(VM.getEnergyChargesAll().getTimeOnly(), VM.getChangeTime().getTimeInMillis());
            logger.info("The VM " + VM.getVMid() + "Energy calculation - Start time: "
                    + timePeriod.getStartTimeInSeconds() + " end time: " + timePeriod.getEndTimeInSeconds());

          //  System.out.println("The VM " + VM.getVMid() + "Energy calculation - Start time: "
            //        + timePeriod.getStartTimeInSeconds() + " end time: " + timePeriod.getEndTimeInSeconds());
           double newEnergyValue = energyModeller.getEnergyRecordForVM(vm, timePeriod).getTotalEnergyUsed();
            
            logger.info((VM.getStartTime() == null ? "The VM start time was null" : "The VM start time was ok"));
            logger.info((VM.getChangeTime() == null ? "The VM change time was null" : "The VM change time was ok"));
            logger.info((vm == null ? "The vm obtained from the EM was null" : "The vm obtained from the EM was ok"));
            logger.info((energyModeller == null ? "The EM was null" : "The EM was not null"));
            

            VM.setTotalEnergyConsumed(newEnergyValue);
            difference = newEnergyValue - VM.getEnergyConsumedLast();
            logger.info("The VM " + VM.getVMid() + "consumed since the last calculation "
                    + difference + " with a total energy consumed of: " + newEnergyValue);
            VM.setEnergyConsumedLast(newEnergyValue);
        } catch (NullPointerException ex) {
            logger.error("The update to the energy value failed for VM: " + VM.getVMid() + ". " +
                    "The start time for the energy usage query is: " + VM.getStartTime().getTimeInMillis() + " and the end time is: " + VM.getChangeTime().getTimeInMillis(), ex);
            //for testing
            difference = 100;

        } 
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