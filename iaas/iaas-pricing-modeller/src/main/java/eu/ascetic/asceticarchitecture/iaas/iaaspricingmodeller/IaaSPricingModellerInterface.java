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

//TO IMPLEMENT PER EACH HOST!!
package eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller;

/**
 * This is the standard interface for any pricing module to be loaded into
 * the ASCETiC architecture.
 * @author E. Agiatzidou
 */

public interface IaaSPricingModellerInterface{
	
	 /**
     * This functions returns the value of the cost of energy
     */
	public double getEnergyCost();
	
	 /**
     * This functions returns the value of the amortised cost of the host
     */
	public double getAmortHostCost();
	
	 /**
     * This functions returns the value of the PUE
     */
	public double getPUE();
	
	
	/**
     * This function set the value of the cost of energy for the model
     */
	public void setEnergyCost(double energycost);
	
	/**
     * This function set the value of the amortised cost of the host
     */
	public void setAmortHostCost(double amorthostcost);
	
	/**
     * This function set the value of the PUE
     */
	public void setPUE(double PUE);
	
	
	/**
     * Returns a cost (price) estimation based on the avg. power that a VM consumes during an hour.
     *
     * @param avgPower avg. power that a VM consumes during an hour.
     * @return the price estimation
     */
	public double getVMCostEstimation(double totalEnergyUsed, int hostId);
	public double getVMPriceEstimation(double totalEnergyUsed, int hostId);
}