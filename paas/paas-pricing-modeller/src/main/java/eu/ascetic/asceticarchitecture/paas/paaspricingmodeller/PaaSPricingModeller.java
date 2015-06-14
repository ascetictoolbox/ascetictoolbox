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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.io.IOException;


import eu.ascetic.asceticarchitecture.paas.type.PaaSPrice;;


/**
 * This is the main interface of the pricing modeller of PaaS layer. 
 * Functionality:
 * The ability to provide a price estimation of an application per hour, given the energy consumed of this app, the deployment id, 
 * the application id, the IaaS provider id and the IaaS provider's price. 
 * 
 * The price estimation can be also given without the provision of energy estimation.
 * 
 * The price estimation can be also given without the provision of an PaaS price. 
 * @author E. Agiatzidou
 */


public class PaaSPricingModeller implements PaaSPricingModellerInterface{
	public static Collection<PaaSPrice> prices = new ArrayList<>();
	private final double price =  0.24; //Default value in case we do not have enough information. It should be removed.

    public PaaSPricingModeller() {
    }

    /**
     * This function returns a price estimation based on the 
     * total energy that the application has/will consume and the price of the IaaS provider.
     * @param totalEnergyUsed The total estimated energy that the application has consumed.
     * @param deploymentId The deployment ID
     * @param appId The application ID
     * @param iaasId The IaaS ID
     * @param iaasPrice The price of the IaaS provider
     * @return the estimated price of the application running on this IaaS provider
     */
    @Override
    public double getAppPriceEstimation(double totalEnergyUsed, int deploymentId, int appId, int iaasId, double iaasPrice) {
    	double tempPrice=iaasPrice + iaasPrice*20/100;
        PaaSPrice paasPrice = new PaaSPrice(totalEnergyUsed, deploymentId, appId, iaasId, iaasPrice);
        prices.add(paasPrice);
    	return  tempPrice; 
    }

    
    /**
     * This function returns a price estimation based on the price of the IaaS provider.
     * @param deploymentId The deployment ID
     * @param appId The application ID
     * @param iaasId The IaaS ID
     * @param iaasPrice The price of the IaaS provider
     * @return the estimated price of the application running on this IaaS provider
     */
    public double getAppPriceEstimation(int deploymentId, int appId, int iaasId, double iaasPrice) {
    	double tempPrice=iaasPrice + iaasPrice*20/100;
         PaaSPrice paasPrice = new PaaSPrice(deploymentId, appId, iaasId, iaasPrice);
         prices.add(paasPrice);
     	return  tempPrice; 
    }

    
    /**
     * This function returns a price estimation based on the 
     * total energy that the application has/will consume and the price of the IaaS provider.
     * In this case the price of the provider is not given. It will be retrieved by historic data
     * otherwise a default value is returned. 
     * @param totalEnergyUsed The total estimated energy that the application has consumed.
     * @param deploymentId The deployment ID
     * @param appId The application ID
     * @param iaasId The IaaS ID
     * @return the estimated price of the application running on this IaaS provider or a default value of it.
     */
   public double getAppPriceEstimation(double totalEnergyUsed, int deploymentId, int appId, int iaasId) {
    	double iaasPrice=findIaaSPrice(totalEnergyUsed, deploymentId, appId, iaasId);
    
    	if (iaasPrice!=0.0){
    		double tempPrice=iaasPrice + iaasPrice*20/100;
    		System.out.println("the price is:"+ tempPrice);
    	        return tempPrice; 
    	}
    	System.out.println("the price is:"+ price);
    	return price;
    }
    
   /**
    * This is a private function that searches the collection to find if a price has been given before 
    * for this application under this deployment and on top of this IaaS Provider. 
    * @param totalEnergyUsed The total estimated energy that the application has consumed.
    * @param deploymentId The deployment ID
    * @param appId The application ID
    * @param iaasId The IaaS ID
    * @return the estimated price of the application running on this IaaS provider or a default value of it.
    */
   private double findIaaSPrice(double totalEnergyUsed, int deploymentId, int appId, int iaasId){
	   PaaSPrice next=new PaaSPrice();
	   for (Iterator< PaaSPrice> it = prices.iterator(); it.hasNext();) {
       	next=it.next();
       	if (equals(next,totalEnergyUsed, deploymentId, appId, iaasId)){
       		return next.getIaaSPrice();
       	}
	   }
       	return 0.0;	
   }
   
   
   /**
    * This is a private function that compares the query of the caller with the given parameters. 
    * @param priceObj A PaaSPrice object which is compared to the parameters. 
    * @param totalEnergyUsed The total estimated energy that the application has consumed.
    * @param deploymentId The deployment ID
    * @param appId The application ID
    * @param iaasId The IaaS ID
    * @return true if there is a match otherwise false.
    */
   private boolean equals(PaaSPrice priceObj, double totalEnergyUsed, int deploymentId, int appId, int iaasId){
	   if ((priceObj.getIaaSId()==iaasId)&&(priceObj.getAppId()==appId)&&(priceObj.getDeploymentId()==deploymentId)&&(priceObj.getTotalEnergyUsed()==totalEnergyUsed)){
		   return true;
	   }
	   else return false;
   }
}