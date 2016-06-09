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


import eu.ascetic.asceticarchitecture.paas.type.Charges;
import eu.ascetic.asceticarchitecture.paas.type.DeploymentInfo;
import eu.ascetic.asceticarchitecture.paas.type.ResourceDistribution;
import eu.ascetic.asceticarchitecture.paas.type.VMinfo;


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
public double predictTotalCharges(VMinfo vm){
	Charges a = predictEnergyCharges(vm, vm.getIaaSProvider().getAverageEnergyPrice());
	Charges b = predictResourcesCharges(vm, getResourcePrice(vm));
	double temp = (double) Math.round((a.getChargesOnly()+b.getChargesOnly()) * 1000) / 1000;
	return temp;
}




///////////////////////////// UPDATE CHARGES BASED ON ENERGY CHANGES ////////////////
public void updateVMCharges(VMinfo VM) {
	System.out.println("Found "+ VM.getVMid());
	VM.setChangeTime();
	updateVMEnergyCharges(VM);
	updateVMResourceCharges(VM, getResourcePrice(VM), getDistribution(VM));
	VM.setTotalCharges(VM.getEnergyCharges()+VM.getResourcesCharges());
	System.out.println("charges= " +VM.getTotalCharges());
}




/////////////////////////// GET CHARGES /////////////////////////
@Override
public double getTotalCharges(VMinfo VM) {
	VM.setChangeTime();
	updateVMResourceCharges(VM, getResourcePrice(VM), getDistribution(VM));
	//System.out.println("Set change time to " +VM.getChangeTime().getTimeInMillis());
	updateVMEnergyCharges(VM);
	
	VM.setTotalCharges(VM.getResourcesCharges()+VM.getEnergyCharges());
	System.out.println("Total charges for VM = " +VM.getTotalCharges());
	return (VM.getTotalCharges());
}

private double getResourcePrice(VMinfo VM){
	return VM.getIaaSProvider().getResoucePrice();
}


private ResourceDistribution getDistribution(VMinfo VM){
	return VM.getIaaSProvider().getDistribution();
}



}