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



public class ResourceDistribution {
	
	double ramPer;
	double cpuPer;
	double storPer;
	
	public ResourceDistribution() {
	 }
	
	public void setDistribution( double ramPer, double cpuPer, double storPer){
		this.ramPer=ramPer;
		this.cpuPer = cpuPer;
		this.storPer = storPer;
		
	}
	
	public double getDistribution(VMinfo vm){
	//	System.out.println("Distribution RAM: " + vm.getRAM() +" and Per " + ramPer);
	//	System.out.println("Distribution CPU: " + vm.getRAM() +" and Per " + cpuPer);
	//	System.out.println("Distribution storage: " + vm.getRAM() +" and Per " + storPer);
		return vm.getRAM()*ramPer+vm.getCPU()*cpuPer+vm.getStorage()*storPer;
	}
	
	public double getPreviousDistribution(VMinfo vm){
		return vm.getPreviousVMChars().getRAM()*ramPer+vm.getPreviousVMChars().getCPU()*cpuPer+vm.getPreviousVMChars().getStorage()*storPer;
	}
}