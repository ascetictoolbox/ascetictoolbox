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

package eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.types;
import java.util.Stack;

import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.energyprovider.EnergyProvider;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.pricingschemesrepository.*;
public class VMstate {
	
	
	/* The characteristics of a VM*/
	//VMinfo vm; 
	
	Stack<VMinfo> changesToCharacteristics = new Stack<VMinfo>();
	
	int VMid;
	
	VMPredInfo predictedInformation;
	
	/* The pricing scheme according to which this VM is charged*/
	IaaSPricingModellerPricingScheme pricingScheme; 
	
	//EnergyProvider provider;
	
	Charges energyCharges;
	
	Charges resourceCharges;
	
	Charges TotalCharges;

	EnergyProvider provider;
	double energy;
	
	/*time when the VM was registered to the IaaS price modeler*/
	Time startTime; 
	
	/* after this time no further charges are computed;
	 * this is useful if a VM is shut down but we still need 
	 * keep info for billing purposes */
	Time endTime; 
	
	Time lastChange;

	double alpha; // the alpha parameter in the long term energy price estimator
	double beta; // the beta parameter in the long term resources price estimator


	
	public VMstate (int VMid, VMinfo vm, EnergyProvider provider, IaaSPricingModellerPricingScheme scheme){
		changesToCharacteristics.push(vm);
		this.VMid = VMid;
		this.provider = provider;
		this.pricingScheme = scheme;
		energyCharges = new Charges();
		resourceCharges = new Charges();
		TotalCharges = new Charges();
		//pricingScheme.getTotalCharges(this);
		lastChange = startTime;
		
	}
			
	

	public VMstate (VMinfo vm, IaaSPricingModellerPricingScheme scheme){
		this.VMid = 0;
		this.pricingScheme = scheme;
	}
	
	public void setStartTime(){
		startTime = new Time();
	}
	
	public void setEndTime(){
		endTime = new Time();
	}
	
	public int getVMid(){
		return VMid;
	}
	
	
	public IaaSPricingModellerPricingScheme getPricingScheme(){
		return pricingScheme;
	}
	
	public EnergyProvider getProvider(){
		return provider;
	}
	
	

	//////////////////////PREDICTION //////////////////////////
	public void setPrediction(double duration, double energy){
		predictedInformation.setDuration(duration);
		predictedInformation.setPredictedEnergy(energy);
	}
	
	public void setPredictedCharges(double charges){
		predictedInformation.setPredictedCharges(charges);
	}
	
	public VMPredInfo getPredictedInformation(){
		return predictedInformation;
	}
	
	public double getPredictedCharges(){
		return predictedInformation.getPredictedCharges();
	}
	
	/////////////////////// UPDATE CHARGES /////////////////////////////
	
	public void updateEnergyCharges(double energyCharges){
		this.energyCharges.updateCharges(energyCharges);
	}
	
	
	public void updateResourcesCharges(double resourcesCharges){
		this.resourceCharges.updateCharges(resourcesCharges);
	}
	

	////////////////// BILLING /////////////////////////
	
	public double getEnergyCharges(){
		return energyCharges.getChargesOnly();
	}
	
	public double getResourcesCharges(){
		return resourceCharges.getChargesOnly();
	}
	
	public double getTotalCharges(){
		return TotalCharges.getChargesOnly();
	}
	
	public void setTotalCharges(double charges){
		TotalCharges.setCharges(charges);
	}
	
	public double getEnergy(){
		return energy;
	}
	
	public void setEnergyChangeTime(Time time){
		energyCharges.setTime(time);
	}
	
	public VMinfo getVMinfo(){
		return changesToCharacteristics.peek();
	}
	
	public void setNewVMinfo(VMinfo vm){
		changesToCharacteristics.push(vm);
		
	}
	
	public Time getResourcesChangeTime(){
		return resourceCharges.getTimeOnly();
	}
	
	public void setResourcesChangeTime(Time time){
		resourceCharges.setTime(time);
	}
	
	public Time getChangeTime(){
		return lastChange;
	}
	
	public void setChangeTime(Time time){
		lastChange = time;
	}
	
	public Time getStartTime(){
		return startTime;
	}

	public String printVMCharacteristics() {
		String toPrint = getVMinfo().getVMCharacteristics() + "Resources charges" + getResourcesCharges() + "Energy Charges" + getEnergyCharges();
		return toPrint;
	}
	
	
	public double getDuration(){
		long a = lastChange.difTime(startTime);
		return (a / 1000 / 60) % 60;
	}


	
}