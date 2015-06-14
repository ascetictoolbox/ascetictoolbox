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


import eu.ascetic.asceticarchitecture.paas.type.Time;

/**
 * This class creates objects that keep information about the deployment ID, the
 * application ID, the IaaS ID, the IaaS Price and the total energy that has
 * been used.
 * 
 * @author E. Agiatzidou
 */

public class AppState {

	int appId;

	AppPredInfo predictedInfo;

	DeploymentInfo depl;

	IaaSProvider provider;

	Time startTime;

	Time lastChange;

	Time endTime;
	
	Charges TotalCharges;

	public AppState(int appID, DeploymentInfo depl, int iaasID, int schemeId) {
		appId = appID;
		this.depl = depl;
		provider = new IaaSProvider(iaasID, schemeId);
		TotalCharges = new Charges();
	}

	public int getAppId() {
		return appId;
	}

	public void setStartTime() {
		startTime = new Time();
	}

	public void setEndTime() {
		endTime = new Time();
	}

	public void setProvider(IaaSProvider provider) {
		this.provider = provider;
	}

	public IaaSProvider getProvider() {
		return provider;
	}

	// ////////////////////PREDICTION //////////////////////////
	public void setPrediction(double duration, double energy, double amount) {
		predictedInfo.setDuration(duration);
		predictedInfo.setPredictedEnergy(energy);
		predictedInfo.setPredictedCharges(amount);
	}

	public void setPredictedCharges(double charges) {
		predictedInfo.setPredictedCharges(charges);
	}

	public AppPredInfo getPredictedInformation() {
		return predictedInfo;
	}

	public double getPredictedCharges() {
		return predictedInfo.getPredictedCharges();
	}

	// ///////////////////// UPDATE CHARGES /////////////////////////////

	public void updateIaaSCharges(double energyCharges) {
		this.provider.setIaaSCharges(energyCharges);
	}

	// ////////////////BILLING /////////////////////////

	

	public double getTotalCharges() {
		return TotalCharges.getChargesOnly();
	}

	public void setTotalCharges(double charges) {
		TotalCharges.setCharges(charges);
	}

	
	public Time getChangeTime() {
		return lastChange;
	}

	public void setChangeTime(Time time) {
		lastChange = time;
	}

	public Time getStartTime() {
		return startTime;
	}

	
	public double getDuration() {
		long a = lastChange.difTime(startTime);
		return (a / 1000 / 60) % 60;
	}

	public int getDeplId() {
		
		return depl.getId();
	}
	
	public DeploymentInfo getDeployment() {
		
		return depl;
	}
	
public void createDelpoyment(VMinfo vm) {
		depl.addVM(vm);
	}
}