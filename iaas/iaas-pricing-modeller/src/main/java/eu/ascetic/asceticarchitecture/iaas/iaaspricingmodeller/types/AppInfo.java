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

import java.util.HashMap;
import java.util.LinkedList;


public class AppInfo {

	String appID;
	
	LinkedList<VMstate> vms;
	
	Charges appCharges;
	
	HashMap<String, Charges> VMCharges = new HashMap<String, Charges>();
	
	public AppInfo(String appID){
		this.appID=appID;
		vms = new LinkedList<VMstate>();
	}
	
	
	public String getAppID(){
		return appID;
	}
	
	public LinkedList<VMstate> getList(){
		return vms;
	}
	
	public void addVM(VMstate VM){
	//	System.out.println("AppInfo: The VM added to app= " + appID);
		vms.add(VM);
	}
	
	public double getVMTotalCharges(VMstate VM){
		for (int i=0; i<=vms.size();i++){
			VMstate vm = vms.get(i);
			if (vm.getVMid()==VM.getVMid()){
				return vm.getTotalCharges();
			}
		}
		return 0.0;
	}
	
	public double getAppCharges(){
		double appCharges=0;
		for (int i=0; i<=vms.size();i++){
			VMstate vm = vms.get(i);
			appCharges = appCharges+vm.getTotalCharges();
		}
		return appCharges;
	}
	
	
		
}