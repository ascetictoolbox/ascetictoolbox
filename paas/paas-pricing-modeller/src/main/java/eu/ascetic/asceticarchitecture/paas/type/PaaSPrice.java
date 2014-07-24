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


/**
 * This class creates objects that keep information about the deployment ID, the application ID, 
 * the IaaS ID, the IaaS Price and the total energy that has been used. 
 *
 */

public class PaaSPrice{
	double totalEnergyUsed;
	int deploymentId;
	int appId;
	int iaasId;
	double iaasPrice;
	
	public PaaSPrice(){
		
	}
	
	
	public PaaSPrice(double totalEnergyUsed, int deploymentId, int appId, int iaasId, double iaasPrice){
		this.totalEnergyUsed=totalEnergyUsed;
		this.deploymentId=deploymentId;
		this.appId=appId;
		this.iaasId=iaasId;
		this.iaasPrice=iaasPrice;
	}
	
	public PaaSPrice(int deploymentId, int appId, int iaasId, double iaasPrice){
		this.deploymentId=deploymentId;
		this.appId=appId;
		this.iaasId=iaasId;
		this.iaasPrice=iaasPrice;
	}
	
	public PaaSPrice(double totalEnergyUsed, int deploymentId, int appId, int iaasId){
		this.totalEnergyUsed=totalEnergyUsed;
		this.deploymentId=deploymentId;
		this.appId=appId;
		this.iaasId=iaasId;
	}
	
	public int getIaaSId(){
		return this.iaasId;
	}
	
	public int getAppId(){
		return this.appId;
	}
	
	public int getDeploymentId(){
		return this.deploymentId;
	}
	
	public double getTotalEnergyUsed(){
		return this.totalEnergyUsed;
	}
	
	public double getIaaSPrice(){
		return this.iaasPrice;
	}
	
}