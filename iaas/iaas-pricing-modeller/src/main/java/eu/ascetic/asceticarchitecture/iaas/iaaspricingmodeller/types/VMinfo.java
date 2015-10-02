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
	/*I will ask for the characteristics of the VMs or I will take it as a reference*/
	double RAM;
	double CPU;
	double storage;
	String hostname;
	
	public VMinfo (double RAM, double CPU, double storage, String hostname){
		this.RAM = RAM;
		this.CPU = CPU;
		this.storage = storage;
		this.hostname = hostname;
	}

	
	public double getRAM(){
		return RAM;
	}
	
	public String gethostname(){
		return hostname;
	}
	
	public double getCPU(){
		return CPU;
	}
	
	public double getStorage(){
		return storage;
	}
	
	public String getVMCharacteristics(){
		String toPrint = "RAM: " + RAM + "CPU: " + CPU + "Storage: " + storage;
	    return toPrint;
	}
}