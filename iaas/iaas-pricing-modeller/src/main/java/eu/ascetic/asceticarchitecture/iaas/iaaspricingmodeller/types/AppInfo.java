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

import java.util.LinkedList;


public class AppInfo {

	int appID;
	
	LinkedList<VMstate> vms;
	
	public AppInfo(int appID){
		this.appID=appID;
		vms = new LinkedList<VMstate>();
	}
	
	
	public int getAppID(){
		return appID;
	}
	
	public LinkedList<VMstate> getList(){
		return vms;
	}
	
	public void addVM(VMstate VM){
		vms.add(VM);
	}
		
}