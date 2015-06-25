/**
 *  Copyright 2014 Athens University of Economics and Business
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


import java.util.LinkedList;

import eu.ascetic.asceticarchitecture.paas.type.Time;

/**
 * This class creates objects that keep information about the deployment ID, the
 * application ID, the IaaS ID, the IaaS Price and the total energy that has
 * been used.
 * 
 * @author E. Agiatzidou
 * @param <AppPredInfo>
 */

public class DeploymentInfo<AppPredInfo> {

	int deploymentId;
	
	DeploymPredInfo predictedInfo;
	
	int schemeId;
	
	Time startTime;

	Time lastChange;

	Time endTime;
	
	Charges TotalCharges;
	
	Charges energyCharges;
	
	Charges resourceCharges;
	
	Charges totalIaaSCharges;

	LinkedList<VMinfo> VMs = new LinkedList<VMinfo>();

	public DeploymentInfo(int deploymentId, int schemeID) {
		this.deploymentId=deploymentId;
		this.schemeId = schemeID;

	}

	public void addVM(VMinfo vm){
		VMs.add(vm);
	}
	
	public int getId(){
		return deploymentId;
	}
	
	public void setIaaSTotalCurrentCharges(double charges){
		totalIaaSCharges.setCharges(charges);
	}
	
	public void setTotalCurrentCharges(double charges){
		TotalCharges.setCharges(charges);
	}
	
	public double getTotalCurrentCharges(){
		return TotalCharges.getChargesOnly();
	}
	
	public VMinfo getVM(int i){
		return VMs.get(i);
	}
	
	public int getNumberOfVMs(){
		return VMs.size();
	}
	
	public void setStartTime() {
		startTime = new Time();
	}

	public void setEndTime() {
		endTime = new Time();
	}

	public int getSchemeId(){
		return schemeId;
	}
	
	public void setPrediction(double amount) {
		predictedInfo.setPredictedCharges(amount);
	}

	public void setIaaSPredictedCharges(double amount) {
		predictedInfo.setIaaSPredictedCharges(amount);
	}

	public DeploymPredInfo getPredictedInformation() {
		return predictedInfo;
	}

	public double getPredictedCharges() {
		return predictedInfo.getPredictedCharges();
	}


}