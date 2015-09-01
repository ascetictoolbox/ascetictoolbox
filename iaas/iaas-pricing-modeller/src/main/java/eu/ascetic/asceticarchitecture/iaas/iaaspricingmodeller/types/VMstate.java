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
import java.util.Calendar;
import java.util.Stack;

import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.energyprovider.EnergyProvider;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.pricingschemesrepository.*;
public class VMstate {
	
	
	/* The characteristics of a VM*/
	//VMinfo vm; 
	
	Stack<VMinfo> changesToCharacteristics = new Stack<VMinfo>();
	
	String VMid;
	
	VMPredInfo predictedInformation = new VMPredInfo();
	
	/* The pricing scheme according to which this VM is charged*/
	IaaSPricingModellerPricingScheme pricingScheme; 
	
	
	Charges energyCharges;
	
	Charges resourceCharges;
	
	Charges TotalCharges;

	EnergyProvider provider;
	double energy;
	

	TimeParameters time;
	


	
	public VMstate (String VMid, VMinfo vm, EnergyProvider provider, IaaSPricingModellerPricingScheme scheme){
		changesToCharacteristics.push(vm);
		this.VMid = VMid;
		this.provider = provider;
		this.pricingScheme = scheme;
		energyCharges = new Charges();
		resourceCharges = new Charges();
		TotalCharges = new Charges();
		time = new TimeParameters();

	}
	
	public VMstate (VMinfo vm, EnergyProvider provider, IaaSPricingModellerPricingScheme scheme){
		changesToCharacteristics.push(vm);
		this.provider = provider;
		this.pricingScheme = scheme;
		energyCharges = new Charges();
		resourceCharges = new Charges();
		TotalCharges = new Charges();
		time = new TimeParameters();
		

	}
	

	public VMstate (VMinfo vm, IaaSPricingModellerPricingScheme scheme){
		this.VMid = "null";
		this.pricingScheme = scheme;
	}
	
	public void setStartTime(){
		time.setStartTime();
	}
	
	public void setEndTime(long endTime){
		time.setEndTime(endTime);
	}
	
	public String getVMid(){
		return VMid;
	}
	
	
	public IaaSPricingModellerPricingScheme getPricingScheme(){
		return pricingScheme;
	}
	
	public EnergyProvider getProvider(){
		return provider;
	}
	
	

	//////////////////////PREDICTION //////////////////////////
	public void setPrediction(long duration, double energy, double power){
		predictedInformation.setDuration(duration);
		predictedInformation.setPredictedEnergy(energy);
		predictedInformation.setPredictedPowerPerHour(power);
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
	
	public void setEnergyChangeTime(Calendar time){
		energyCharges.setTime(time);
	}
	
	public VMinfo getVMinfo(){
		return changesToCharacteristics.peek();
	}
	
	public void setNewVMinfo(VMinfo vm){
		changesToCharacteristics.push(vm);
		
	}
	
	public long getResourcesChangeTime(){
		return resourceCharges.getTimeOnly();
	}
	
	public void setResourcesChangeTime(Calendar time){
		resourceCharges.setTime(time);
	}
	
	public Calendar getChangeTime(){
		return time.getLastChangeTime();
	}
	
	public void setChangeTime(){
		time.setLastChangeTime();
		
	}
	
	public void setEndTime(){
		time.setEndTime();
		
	}
	
	public void setChangeTime(Calendar time){
		this.time.setLastChangeTime(time);
		
	}
	
	public void setChangeTime(long time){
		this.time.setLastChangeTime(time);
	}
	
	public Calendar getStartTime(){
		return time.getStartTime();
	}

	public String printVMCharacteristics() {
		String toPrint = getVMinfo().getVMCharacteristics() + "Resources charges" + getResourcesCharges() + "Energy Charges" + getEnergyCharges();
		return toPrint;
	}
	
	
	public long getTotalDuration(){
		return time.getTotalDuration();
	}

	public long getDuration(Calendar start, Calendar end){
		return time.getDuration(start, end);
	}

	
}