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
	
	EnergyPrediction predictedEnergy;
	
	PredictedCharges predictedCharges; //coming from IaaS scheme

	
	public VMPredInfo (){
		this.predictedDuration = 0;
		this.predictedEnergy = null;
		this.predictedCharges=new PredictedCharges();
	}
	
	public VMPredInfo (long predictedDuration, EnergyPrediction predictedEnergy){
		this.predictedDuration = predictedDuration;
		this.predictedEnergy = predictedEnergy;
		this.predictedCharges=new PredictedCharges();
	}

		
	public void setDuration(long duration){
		predictedDuration = duration;
	}
	
	public long getPredictedDuration(){
		return predictedDuration;
	}
	
	public PredictedCharges getPredictedCharges(){
		return predictedCharges;
	}
	
	public void setPredictedCharges(PredictedCharges price) {
		predictedCharges = price;
	}
	
	public double getPredictedChargesOnly(){
		return predictedCharges.getChargesOnly();
	}
	
	public void setPredictedCharges(double price) {
		predictedCharges.setCharges(price);
	}
	
	public double getPredictedPrice(){
		return predictedCharges.getPriceOnly();
	}
	
	public void setPredictedPrice(double price) {
		predictedCharges.setPrice(price);
	}
	
	public void setPredictedTotalEnergy(double predictedEnergy){
		this.predictedEnergy.setTotalEnergy(predictedEnergy); 
	}
	
	public double getTotalPredictedEnergy(){
		return predictedEnergy.getTotalEnergy();
	}
	
	public void setPredictedPowerPerHour(double power){
		this.predictedEnergy.setAvergPower(power); 
	}
	
	public double getPredictedPowerPerHour(){
		return this.predictedEnergy.getAvrgPower();
	}
	
	public EnergyPrediction getPredictionsOfEnergy(){
		return this.predictedEnergy;
	}
	
	public void setPredictionOfEnergy(EnergyPrediction energy){
		this.predictedEnergy=energy;
	}
}