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
import java.util.ListIterator;

import eu.ascetic.asceticarchitecture.paas.type.Time;

/**
 * This class creates objects that keep information about the deployment ID, the
 * application ID, the IaaS ID, the IaaS Price and the total energy that has
 * been used.
 * 
 * @author E. Agiatzidou
 */

public class DeploymentInfo {

	int deploymentId;
	
	DeploymPredInfo predictedInfo=new DeploymPredInfo();
	
	int schemeId = 100;
	
	Time startTime;

	Time lastChange;

	Time endTime;
	
	Charges TotalCharges = new Charges();
	
	Charges energyCharges= new Charges();
	
	Charges resourceCharges= new Charges();
	
	Charges totalIaaSCharges= new Charges();

	LinkedList<VMinfo> VMs = new LinkedList<VMinfo>();
	
	IaaSProvider IaaS;
	
	double totalEnergy;//used for events;

	public DeploymentInfo(int deploymentId, int schemeID) {
		this.deploymentId=deploymentId;
		this.schemeId = schemeID;

	}
	
	public DeploymentInfo(int deploymentId) {
	//	System.out.println("DeloymentInfo: new deployment with ID: " + deploymentId);
		this.deploymentId=deploymentId;
		

	}


	public void setVMs(LinkedList<VMinfo> listofVMs){
		for (int i=0; i<listofVMs.size();i++){
			VMs.add(listofVMs.get(i));
		//	System.out.println("DeloymentInfo: I added this VM "+ VMs.get(i).getVMid());
		}
	}
	
	public void addVM(VMinfo vm){
	//	System.out.println("DeloymentInfo: I added this VM "+ vm.getVMid());
		VMs.add(vm);
	}
	
	public void setEnergy(double energy){
		this.totalEnergy=energy;
	}
	
	public double getEnergy(){
		return totalEnergy;
	}

	public VMinfo getVM(){
		return VMs.getFirst();
	}
	
	public VMinfo getVMbyID(int VMid){
		for  (int i=1; i<=VMs.size();i++){
			if (VMs.get(i).getVMid() == VMid)
			//	System.out.println("DeloymentInfo:VMid to find " + VMid + " found the " + VMs.get(i).getVMid());
				return VMs.get(i);
		}
		return null;
	}
	
	public int getId(){
		return deploymentId;
	}
	
	public void setIaaSProvider(IaaSProvider prov){
		//System.out.println("DeloymentInfo: deployment with ID: " + deploymentId +" deployed to provider with ID " + prov.getID());
		IaaS = prov;
	}
	
	public IaaSProvider getIaaSProvider(){
		return IaaS;
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
	
	public void setPredictedCharges(double amount) {
		predictedInfo.setPredictedCharges(amount);
	}

	public void setPredictedPrice(double amount) {
		predictedInfo.setPredictedPrice(amount);
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

	public void setIaaSProvider(int i) {
		this.IaaS = new IaaSProvider(i);
		
	}

	

}