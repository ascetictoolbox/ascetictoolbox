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
	
	String VMid;
		
	Stack<VMinfo> changesToCharacteristics = new Stack<VMinfo>();
		
	VMPredInfo predictedInformation = new VMPredInfo();
	
	EnergyProvider provider;
	
	/* The pricing scheme according to which this VM is charged*/
	IaaSPricingModellerPricingScheme pricingScheme; 
	
	String appID;
	
	Charges energyCharges;
	
	Charges resourceCharges;
	
	Charges TotalCharges;
	
	double energy;
	
	double totalEnergy;

	TimeParameters time;
	
	double duration;

	int chargingHours=0;
	
	int hoursChanged;
	
	////////////////////////////////////////////////////CONSTRUCTOR////////////////////////////////////////////////	
	public VMstate (String VMid, VMinfo vm, EnergyProvider provider, IaaSPricingModellerPricingScheme scheme, String appID){
		this.VMid = VMid;
		this.provider = provider;
		this.pricingScheme = scheme;
		this.appID = appID;
		
		changesToCharacteristics.push(vm);
			
		time = new TimeParameters();
		energyCharges = new Charges(time.getStartTime());
		resourceCharges = new Charges(time.getStartTime());
		TotalCharges = new Charges(time.getStartTime());
		energy=0;
		totalEnergy=0;
		System.out.println("VMstate: The VM with VMid " + VMid + "has been registered at: " + getStartTime().getTimeInMillis()+" with RAM "+getVMinfo().getRAM()+" CPU "+getVMinfo().getCPU()+" storage "+getVMinfo().getStorage()+" AppID= "+appID + " IaaSID= "+getVMinfo().getIaaSID());
	}
	
	public VMstate (String VMid, VMinfo vm, EnergyProvider provider, IaaSPricingModellerPricingScheme scheme){
		changesToCharacteristics.push(vm);
		this.VMid = VMid;
		this.provider = provider;
		this.pricingScheme = scheme;
		time = new TimeParameters();
		energyCharges = new Charges();
		resourceCharges = new Charges();
		TotalCharges = new Charges();
		chargingHours=1;
		energy=0;
		totalEnergy=0;
	}
	
	
	public VMstate (VMinfo vm, EnergyProvider provider, IaaSPricingModellerPricingScheme scheme){
		changesToCharacteristics.push(vm);
		this.provider = provider;
		this.pricingScheme = scheme;
		time = new TimeParameters();
		energyCharges = new Charges();
		resourceCharges = new Charges();
		TotalCharges = new Charges();
		chargingHours=1;
		energy=0;
		totalEnergy=0;
	}
	

	public VMstate (VMinfo vm, IaaSPricingModellerPricingScheme scheme){
		this.VMid = "null";
		this.pricingScheme = scheme;
		energy=0;
		totalEnergy=0;
		chargingHours=1;
	}
	
	////////////////////////////////////////////////////SET METHODS////////////////////////////////////////////////
	public void setEndTime(long endTime){
		time.setEndTime(endTime);
	}
	
	public void setAppID(String id){
		this.appID=id;
	}
	
	public void setDuration(double dur){
		duration=dur;
	}
	
	
////////////////////////////////////////////////////GET METHODS////////////////////////////////////////////////
	public String getVMid(){ 
		return VMid;
	}
	
	public IaaSPricingModellerPricingScheme getPricingScheme(){
		return pricingScheme;
	}
	
	public EnergyProvider getProvider(){
		return provider;
	}
	
	public String getAppID(){
		return appID;
	}
	
	public long getVMDuration(){
		return getTotalDuration();
	}

	public double getDuration(){
		return duration;
	}
	
	//////////////////////PREDICTION //////////////////////////
	public void setPrediction(long duration, EnergyPrediction energy, double power){
		predictedInformation.setDuration(duration);
		predictedInformation.setPredictionOfEnergy(energy);
		predictedInformation.setPredictedPowerPerHour(power);
	}
	
	public void setPredictedCharges(double charges){
		predictedInformation.setPredictedCharges(charges);
	}
	
	public void setPredictedPrice(double price){
		predictedInformation.setPredictedPrice(price);
	}
	
	public VMPredInfo getPredictedInformation(){
		return predictedInformation;
	}
	
	public PredictedCharges getPredictedCharges(){
		return predictedInformation.getPredictedCharges();
	}
	
	/////////////////////// UPDATE CHARGES /////////////////////////////
	
	public void updateEnergyCharges(double energyCharges){
		this.energyCharges.updateCharges(time.getEndTime(), energyCharges);
		
	}
	
	
	public void updateResourcesCharges(double resourcesCharges){
		this.resourceCharges.updateCharges(time.getEndTime(),resourcesCharges);
		
	}
	

	////////////////// BILLING /////////////////////////
	
	public double getEnergyCharges(){
		return energyCharges.getChargesOnly();
	}
	
	public double getResourcesCharges(){
		return resourceCharges.getChargesOnly();
	}
	
	public Charges getEnergyChargesAll(){
		return energyCharges;
	}
	
	public Charges getResourcesChargesAll(){
		return resourceCharges;
	}
	
	public double getTotalCharges(){
		return TotalCharges.getChargesOnly();
	}
	
	public void setTotalCharges(double charges){
		TotalCharges.setCharges(time.getEndTime(), charges);
		
	}
	
	public double getTotalEnergyConsumed(){
		return totalEnergy;
	}
	
	public void setTotalEnergyConsumed(double energy){
		 this.totalEnergy=energy;
	}
	
	public double getEnergyConsumedLast(){
		return energy;
	}
	
	public void setEnergyConsumedLast(double energy){
		 this.energy=energy;
	}
		
	public VMinfo getVMinfo(){
		return changesToCharacteristics.peek();
	}
	
	public void setNewVMinfo(VMinfo vm){
		changesToCharacteristics.push(vm);
		
	}
	
	public Calendar getChangeTime(){
		return time.getEndTime();
	}
	
	public void setChangeTime(){
		time.setEndTime();
		System.out.println("VMstate: Time changed for VM = " + VMid +" to " + time.getEndTime().getTimeInMillis()); 
	
	}
	
	public void setEndTime(){
		time.setEndTime();
		
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

	public void setHours(int hour){
		chargingHours=hour;
	}
	
	public int getHours(){
		return chargingHours;
	}
	
	public void setHoursCounter(){
		hoursChanged++;
	}
	
	public void resetHoursCounter(){
		hoursChanged=0;
	}
	
	public int getHoursCounter(){
		return hoursChanged;
	}
	
}