package eu.ascetic.asceticarchitecture.paas.paaspricingmodeller;

/**
 * This is the main interface of the pricing modeller of PaaS layer. 
 * Functionality:
 * The ability to provide a price estimation of an application per hour, given the energy consumed of this app, the deployment id, 
 * the app id, the IaaS provider id and the IaaS provider's price. 
 * 
 * The price estimation can be also given without the provision of energy estimation.
 * 
 * The price estimation can be also given without the provision of an IaaS price. 
 *
 */
public class PaaSPricingModeller {
	

    public PaaSPricingModeller() { }

    /**
     * Returns a price estimation based on the avg. power that a VM consumes during an hour.
     *
     * @param avgPower avg. power that a VM consumes during an hour.
     * @return the price estimation
     */
    public double getAppPriceEstimation(double totalEnergyUsed, int deploymentId, int appId, int iaasId, double iaasPrice) {
        double cost=0.07;
    	return cost; 
    }

    public double getAppPriceEstimation(int deploymentId, int appId, int iaasId, double iaasPrice) {
        double cost=0.07;
    	return cost; 
    }

    public double getAppPriceEstimation(double totalEnergyUsed, int deploymentId, int appId, int iaasId) {
        double cost=0.07;
    	return cost; 
    }
    

}