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

package eu.ascetic.asceticarchitecture.paas.paaspricingmodeller;

/**
 * This is the standard interface for any pricing module to be loaded into
 * the ASCETiC architecture.
 * @author E. Agiatzidou
 */

public interface PaaSPricingModellerInterface{
	
	
	/**
     * Returns a price estimation based on the total power that an application consumes during an hour.
     *
     * @param totalEnergyUsed total power that an application consumes during an hour.
     * @param deploymentId the id of the deployment that the application is using
     * @param appId the id of the application running
     * @param iaasId the id of the IaaS provider that will be used or is a candidate for the application to be deployed
     * @param iaasPrice the price that the IaaS provider is asking for the deployment of the application on his premises in $/hour
     * @return the price estimation in $/hour of the PaaS provider
     */
	public double getAppPriceEstimation(double totalEnergyUsed, int deploymentId, int appId, int iaasId, double iaasPrice);
	
	
	
}