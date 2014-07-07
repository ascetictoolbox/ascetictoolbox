package eu.ascetic.asceticarchitecture.paas.paaspricingmodeller;

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
public class PaaSPricingModeller implements PaaSPricingModellerInterface{
	

    public PaaSPricingModeller() { }

    @Override
    public double getAppPriceEstimation(double totalEnergyUsed, int deploymentId, int appId, int iaasId, double iaasPrice) {
        double price=0.0;
        price=iaasPrice + iaasPrice*20/100;
    	return price; 
    }

 
    public double getAppPriceEstimation(int deploymentId, int appId, int iaasId, double iaasPrice) {
    	 double price=0.0;
       //  price=iaasPrice + iaasPrice*20/100;
     	return price; 
    }

    public double getAppPriceEstimation(double totalEnergyUsed, int deploymentId, int appId, int iaasId) {
    	 double price=0.0;
         
     	return price; 
    }
    

}