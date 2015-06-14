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

package eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller;

/**
 * This is the main interface of the pricing modeller of IaaS layer. 
 * Functionality:
 * 1. The ability to provide a cost estimation of a VM per hour, given the energy consumed of this VM and the host that
 * this VM is deployed.
 *
 * 2. The ability to provide a price estimation of a VM per hour, given the energy consumed of this VM and the host that
 * this VM is deployed.
 * @author E. Agiatzidou
 */




//TO IMPLEMENT PER EACH HOST!!
public class IaaSPricingModeller implements IaaSPricingModellerInterface{
	 /**
     * @param energycost The current cost of the energy consumed in $/kwh
     */
	double energyCost;
	
	/**
	@param amorthostcost The amortised cost of a host $/hour
    */
	double amortHostCost;
	
	/**
	@param PUE The power usage effectiveness of the infrastructure
	*/
	double PUE;
	 
	
	public IaaSPricingModeller() {
    	energyCost = 0.07;
    	amortHostCost=0.08;
    	PUE=1.7;
    	
    }
	
	public IaaSPricingModeller(double energyCost, double amortHostCost, double PUE) {
		super();
    	this.energyCost=energyCost;
    	this.amortHostCost=amortHostCost;
    	this.PUE=PUE;
    	
    }
   
    @Override
    public double getEnergyCost(){
    	return energyCost;
    }
    
    @Override
	public double getAmortHostCost(){
    	return amortHostCost;
    }
    
    @Override
	public double getPUE(){
    	return PUE;
    }
	
    @Override
    public void setEnergyCost(double energycost){
    	this.energyCost=energycost;
    }
	
    @Override
    public void setAmortHostCost(double amorthostcost){
    	this.amortHostCost=amorthostcost;
    }
	
    @Override
    public void setPUE(double PUE){
    	this.PUE=PUE;
    }
	
	
    /**
     * This function returns a cost estimation based on the 
     * total energy that a VM consumes during an hour. The VM runs on top of a 
     * specific host.
     * @param totalEnergyUsed total estimated energy that a VM consumes during an hour
     * @param hostId the id of the host that the VM is running on
     * @return the estimated cost of the VM running on this host
     */
    @Override
    public double getVMCostEstimation(double totalEnergyUsed, String hostId) {
        double cost=0.0;
        cost=amortHostCost+(energyCost*totalEnergyUsed*PUE);
    	return cost; 
    }
    
    
    /**
     * This function returns a price estimation based on the 
     * total energy that a VM consumes during an hour. The VM runs on top of a 
     * specific host.
     * @param totalEnergyUsed total estimated energy that a VM consumes during an hour
     * @param hostId the id of the host that the VM is running on
     * @return the estimated price of the VM running on this host
     */
    @Override
    public double getVMPriceEstimation(double totalEnergyUsed, String hostId) {
    	double cost=0.0;
    	double price = 0.0;
        cost=amortHostCost+(energyCost*totalEnergyUsed*PUE);
        price=cost+cost*20/100;
     	return price; 
}
}