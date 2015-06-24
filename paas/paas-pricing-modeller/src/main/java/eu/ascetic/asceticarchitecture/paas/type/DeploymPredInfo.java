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


public class DeploymPredInfo {
	
	double predictedDuration;
	
	
	double predictedIaaSCharges; //coming from IaaS scheme
	
	double TotalPredictedCharges;
	
	public DeploymPredInfo (double predictedDuration, double predictedEnergy){
		this.predictedDuration = predictedDuration;
	}

		
	public void setDuration(double duration){
		predictedDuration = duration;
	}
	
	public double getPredictedDuration(){
		return predictedDuration;
	}
	
	public double getIaaSPredictedCharges(){
		return predictedIaaSCharges;
	}
	
	public void setIaaSPredictedCharges(double charges) {
		predictedIaaSCharges = charges;
	}
	
		
	public double getPredictedCharges(){
		return TotalPredictedCharges;
	}
	
	public void setPredictedCharges(double price) {
		TotalPredictedCharges = price;
	}


}