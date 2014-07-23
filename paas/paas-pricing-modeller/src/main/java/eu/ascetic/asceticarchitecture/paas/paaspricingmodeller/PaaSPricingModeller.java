package eu.ascetic.asceticarchitecture.paas.paaspricingmodeller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;


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
 *
 */

//TO BE ADDED: CONNECTION WITH DATABASE FOR RETRIEVING INFORMATION
public class PaaSPricingModeller implements PaaSPricingModellerInterface{
	public static Collection<PaaSPrice> prices = new ArrayList<>();
	

    public PaaSPricingModeller() {
    }

    @Override
    public double getAppPriceEstimation(double totalEnergyUsed, int deploymentId, int appId, int iaasId, double iaasPrice) {
        double price=0.0;
        price=iaasPrice + iaasPrice*20/100;
        PaaSPrice paasPrice = new PaaSPrice(totalEnergyUsed, deploymentId, appId, iaasId, iaasPrice);
        prices.add(paasPrice);
    	return price; 
    }

 
    public double getAppPriceEstimation(int deploymentId, int appId, int iaasId, double iaasPrice) {
    	 double price=0.0;
         price=iaasPrice + iaasPrice*20/100;
         PaaSPrice paasPrice = new PaaSPrice(deploymentId, appId, iaasId, iaasPrice);
         prices.add(paasPrice);
     	return price; 
    }

   /*public double getAppPriceEstimation(double totalEnergyUsed, int deploymentId, int appId, int iaasId) {
    	double price=0.0;
    	for (Iterator< prices> it = data.iterator(); it.hasNext();) {
        	next=it.next();
        price=iaasPrice + iaasPrice*20/100;
     	return price; 
    }
    */

}