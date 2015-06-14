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
 * @author E. Agiatzidou
 */

public class IaaSProvider{
	
	int IaaSid;
	
	int scheme; 
	
	double totalAppCharges;
	
	public IaaSProvider(int id, int scheme, double charges) {
		IaaSid=id;
		this.scheme = scheme;
		charges = totalAppCharges;
	}
	
	public IaaSProvider(int id, int scheme) {
		IaaSid=id;
		this.scheme = scheme;
	}
	
	public void setIaaSID(int id){
		IaaSid = id;
	}
	
	public int getIaaSId(){
		return IaaSid;
	}
	
	public void setScheme(int id){
		scheme = id;
	}
	
	public int getScheme(){
		return scheme;
	}
	
	public void setIaaSCharges(double id){
		totalAppCharges = id;
	}
	
	public double getIaaSCharges(){
		return totalAppCharges;
	}
	
}