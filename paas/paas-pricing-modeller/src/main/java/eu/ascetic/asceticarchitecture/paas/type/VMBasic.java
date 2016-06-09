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








import java.util.HashMap;
import java.util.Stack;

import eu.ascetic.asceticarchitecture.paas.type.VMinfo;
import eu.ascetic.asceticarchitecture.paas.type.Charges;
import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.pricingschemes.PaaSPricingModellerPricingScheme;
import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.pricingschemes.PricingSchemeA;
import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.pricingschemes.PricingSchemeB;
import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.pricingschemes.PricingSchemeC;


public class VMBasic {

	int VMid;
	
	int appId;
	
	Stack<VMChars> changesToCharacteristics = new Stack<VMChars>();
	
	double RAM;
	double CPU;
	double storage;
	
	double duration;
	
	boolean active =true;
	
	static EnergyInfo energyInfo = new EnergyInfo();
	
	static HashMap<Integer,IaaSProvider> IaaSProviders = new HashMap<Integer,IaaSProvider>();
	
	//double energyPredicted;
	

	int numberOfEvents;
	
	int schemeID; 
	
	PaaSPricingModellerPricingScheme scheme;
	
	//long predictedDuration;
	
	long actualDuration;
	
	TimeParameters time;
	
	Charges energyCharges;
	
	Charges resourceCharges;
	
	Charges TotalCharges;
	
	IaaSProvider IaaSProvider;
	
	
	public void updateEnergyConsumption(double energy){
		 energyInfo.setCurrentTotalConsumption(energy);
	}
	
	public Charges getResourcesChargesAll(){
		return resourceCharges;
	}
	
	public void setVMid(int VMid){
		this.VMid= VMid; 
	}
	
	public int getVMid(){
		return VMid;
	}
	
	public void setAppid(int appId){
		this.appId= appId; 
	}
	
	public int getAppID(){
		return appId;
	}
	public double getRAM(){
		return RAM;
	}
	
	public long getDuration(Calendar start, Calendar end){
		return time.getDuration(start, end);
	}
	
	public double getEnergyPredicted(){
		return energyInfo.getEnergyPredicted();
	}
	
	public void setNumberOfEvents(int number){
		numberOfEvents=number;
	}
	
	public void setIaaSProvider(IaaSProvider ip){
		
		this.IaaSProvider = ip;
	//	System.out.println("VMBasic: this VM with id = "+ this.VMid+" is running on provider "+ IaaSProvider.getID());
	}
	
	public IaaSProvider getIaaSProvider(){
		return IaaSProvider;
	}
	
	public void setEnergyPredicted(double energy){
		energyInfo.setEnergyPredicted(energy);
	}
	
	public int getNumberOfEvents(){
		return numberOfEvents;
	}
	
	public double getCPU(){
		return CPU;
	}
	
	public void setDuration(double dur){
		duration=dur;
	}
	
	public int getSchemeID(){
		return schemeID;
	}
	
	public double getStorage(){
		return storage;
	}
	
	public String getVMCharacteristics(){
		String toPrint = "RAM: " + RAM + "CPU: " + CPU + "Storage: " + storage;
	    return toPrint;
	}
	
	public long getActualDuration(){
		return actualDuration;
	}
	
	public PaaSPricingModellerPricingScheme getScheme(){
		return scheme;
		
	}

	public Calendar getChangeTime(){
		return time.getEndTime();
	}
	
	public void setChangeTime(){
		time.setEndTime();
		
	}
	
	public void setEndTime(){
		time.setEndTime();
		
	}
	public Calendar getStartTime(){
		return time.getStartTime();
	}
	
	public void setEndTime(long endTime){
		time.setEndTime(endTime);
	}
	
	/*public void setTotalEnergyConsumed(double energy){
		 this.totalEnergy=energy;
	}*/
	
	
	
	/*public double getEnergyConsumedLast(){
		return energy;
	}
	*/
	
	//////UPDATES////////////
	public void updateResourcesCharges(double resourcesCharges){
		this.resourceCharges.updateCharges(time.getEndTime(),resourcesCharges);
	}
	
	public void updateEnergyCharges(double energyCharges){
		this.energyCharges.updateCharges(time.getEndTime(), energyCharges);
		
	}
	
	public double getResourcesCharges(){
		return resourceCharges.getChargesOnly();
	}
	
	public Charges getEnergyChargesAll(){
		return energyCharges;
	}
	
		
	public double getTotalCharges(){
		return TotalCharges.getChargesOnly();
	}
	
	public Charges getTotalChargesAll(){
		return TotalCharges;
	}
	
	public void setTotalCharges(double charges){
		TotalCharges.setCharges(time.getEndTime(), charges);
		
	}
	
	 public PaaSPricingModellerPricingScheme initializeScheme(int schemeId) {
	        PaaSPricingModellerPricingScheme scheme = null;
	        if (schemeId == 0) {
	            scheme = new PricingSchemeA(schemeId);
	        }
	        if (schemeId == 1) {
	            scheme = new PricingSchemeB(schemeId);
	        }
	        if (schemeId == 2) {
	            scheme = new PricingSchemeC(schemeId);
	        }
	        return scheme;
	    }
	 
	 public double getEnergyCharges(){
			return energyCharges.getChargesOnly();
		}
	 		
		public IaaSProvider getProvider(int i){
			return IaaSProviders.get(i);
		}
		
		public void stopped(){
		//	System.out.println("VMBasic: this VM with id = "+ this.VMid+" has stopped");
			active = false;
		}
		
		public void createNewChars(double CPU, double RAM, double storage, Charges totalCharges){
			VMChars oldVM = new VMChars(this.RAM, this.CPU, this.storage);
			oldVM.setTotalCharges(totalCharges);
			changesToCharacteristics.add(oldVM);
			this.RAM = RAM;
			this.CPU = CPU;
			this.storage = storage;
		}

}