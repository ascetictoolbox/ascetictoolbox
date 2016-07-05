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
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.billing.PaaSPricingModellerBilling;
import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.queue.client.PricingModellerQueueServiceManager;
import eu.ascetic.asceticarchitecture.paas.type.Time;

/**
 * This class creates objects that keep information about the deployment ID, the
 * application ID, the IaaS ID, the IaaS Price and the total energy that has
 * been used.
 * 
 * @author E. Agiatzidou
 */

public class DeploymentInfo {
	PaaSPricingModellerBilling billing;

	int deploymentId;
	String applicationId;
	
	DeploymPredInfo predictedInfo=new DeploymPredInfo();
	
	int schemeId = 100;
	
	Time startTime;

	Time lastChange;

	Time endTime;
	
	Charges TotalCharges = new Charges();
	
	Charges energyCharges= new Charges();
	
	Charges resourceCharges= new Charges();
	
	Charges totalIaaSCharges= new Charges();
	
	boolean changing = false;
	private final Object lock = new Object();

	LinkedList<VMinfo> VMs = new LinkedList<VMinfo>();
	PricingModellerQueueServiceManager producer;
	
	IaaSProvider IaaS;
	Timer timer;
	
	long delay = 10;
	
	double totalEnergy;//used for events;
	double currentCharges=0.0;
	double currentPrice;

	public DeploymentInfo(String applicationID, int deploymentId, int schemeID) {
		this.deploymentId=deploymentId;
		this.schemeId = schemeID;
		this.applicationId= applicationID;

	}
	
	public DeploymentInfo(int deploymentId, int schemeID) {
		this.deploymentId=deploymentId;
		this.schemeId = schemeID;
	}

	public String getAppID(){
		return applicationId;
	}
	public Object getLock(){
		return lock;
	}
	public void setCurrentCharges (double charges){
		currentCharges =charges;
	}
	
	public double getCurrentCharges (){
		return currentCharges;
	}
	
	public void resetCurrentCharges (){
		currentCharges =0.0;
	}
	public DeploymentInfo(int deploymentId, PaaSPricingModellerBilling billing) {
	//	System.out.println("DeloymentInfo: new deployment with ID: " + deploymentId);
		timer = new Timer();
		
		timer.scheduleAtFixedRate(new ChargesCalculator(this, billing), TimeUnit.SECONDS.toMillis(30), 30000);
		timer.scheduleAtFixedRate(new ResetCharges(this, billing), TimeUnit.SECONDS.toMillis(90), 60000);
		this.deploymentId=deploymentId;

	}
	public DeploymentInfo(int deplID) {
		this.deploymentId=deploymentId;
	}
	public void setProducer (PricingModellerQueueServiceManager producer){
	//	System.out.println("Deployment info: I have the producer");
		this.producer = producer;
	}
	public PricingModellerQueueServiceManager getProducer (){
		return producer;
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
	
	public LinkedList<VMinfo> getVMs(){
		return VMs;
	}
	
	public VMinfo getVMbyID(int VMid){
		for  (int i=0; i<VMs.size();i++){
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
	
	public void setTotalCharges(double charges){
		TotalCharges.setCharges(charges);
	}
	
	public void updateTotalCharges(double charges){
		TotalCharges.updateCharges(charges);
	}
	
	public double getTotalCharges(){
		return TotalCharges.getChargesOnly();
	}
	
	public double getEnergyCharges(){
		return energyCharges.getChargesOnly();
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
	public void setCurrentPrice(double price) {
		currentPrice = price;
		
	}
	public double getCurrentPrice() {
		
		return currentPrice;
	}
	public void setChanging(boolean b) {
		changing = b;
		
	}
	public boolean getChanging() {
		return changing;
		
	}
	

}