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


public class VMinfo {
	/*I will ask for the characteristics of the VMs or I will take it as a reference*/
	double RAM;
	double CPU;
	double storage;
	
	double energyPredicted;
	
	double currentEnergy;
	
	int numberOfEvents;
	
	int schemeID; 
	
	long predictedDuration;
	
	long actualDuration;
	
	public VMinfo (double RAM, double CPU, double storage, long duration){
		this.RAM = RAM/1024;
		this.CPU = CPU;
		this.storage = storage/1000;
		this.actualDuration=duration;
	}

	public VMinfo (double RAM, double CPU, double storage, long duration, int scheme){
		this.RAM = RAM/1024;
		this.CPU = CPU;
		this.storage = storage/1000;
		this.actualDuration=duration;
		this.schemeID = scheme;
	}
	
	public double getRAM(){
		return RAM;
	}
	
	public double getEnergyPredicted(){
		return energyPredicted;
	}
	
	public void setNumberOfEvents(int number){
		numberOfEvents=number;
	}
	
	public void setEnergyPredicted(double energy){
		energyPredicted = energy;
	}
	
	public int getNumberOfEvents(){
		return numberOfEvents;
	}
	
	public double getCPU(){
		return CPU;
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
}