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


public class VMPredInfo {
	
	long predictedDuration;
	
	double predictedEnergy;
	
	double predictedCharges; //coming from IaaS scheme

	double predictedPower;
	
	public VMPredInfo (){
		this.predictedDuration = 0;
		this.predictedEnergy = 0;
	}
	
	public VMPredInfo (long predictedDuration, double predictedEnergy){
		this.predictedDuration = predictedDuration;
		this.predictedEnergy = predictedEnergy;
	}

		
	public void setDuration(long duration){
		predictedDuration = duration;
	}
	
	public long getPredictedDuration(){
		return predictedDuration;
	}
	
	public double getPredictedCharges(){
		return predictedCharges;
	}
	
	public void setPredictedCharges(double price) {
		predictedCharges = price;
	}
	
	public void setPredictedEnergy(double energy){
		this.predictedEnergy = energy;
	}
	
	public double getPredictedEnergy(){
		return predictedEnergy;
	}
	
	public void setPredictedPowerPerHour(double power){
		this.predictedPower = power;
	}
	
	public double getPredictedPowerPerHour(){
		return this.predictedPower;
	}
	
}