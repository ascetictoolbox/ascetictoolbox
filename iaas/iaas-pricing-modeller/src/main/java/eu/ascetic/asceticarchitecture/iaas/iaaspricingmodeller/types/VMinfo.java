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


public class VMinfo {

	int RAM;
	int CPU;
	double storage;
	String hostname;
	int IaaSID;
	
	public VMinfo (int RAM, int CPU, double storage, String hostname){
		this.RAM = RAM/1024;
		this.CPU = CPU;
		this.storage = storage/1000;
		this.hostname = hostname;
	}

	public VMinfo (int RAM, int CPU, double storage, String hostname, int IaaSID){
		this.RAM = RAM/1024;
		this.CPU = CPU;
		this.storage = storage/1000;
		this.hostname = hostname;
		this.IaaSID = IaaSID;
		System.out.println("VMInfo: the IaaSID is= " + IaaSID);
	}
	
	public int getRAM(){
		return RAM;
	}
	
	public String gethostname(){
		return hostname;
	}
	
	public int getCPU(){
		return CPU;
	}
	
	public double getStorage(){
		return storage;
	}
	
	public String getVMCharacteristics(){
		String toPrint = "RAM: " + RAM + "CPU: " + CPU + "Storage: " + storage;
	    return toPrint;
	}
	
	public int getIaaSID(){
		return IaaSID;
	}
}