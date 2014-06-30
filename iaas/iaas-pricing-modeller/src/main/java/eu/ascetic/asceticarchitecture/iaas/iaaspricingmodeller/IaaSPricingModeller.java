package eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller;

/**
 * This is the main interface of the pricing modeller of IaaS layer. 
 * Functionality:
 * 1. The ability to provide a cost estimation of a VM per hour, given the energy consumed of this VM and the host that
 * this VM is deployed.
 *
 * 2. The ability to provide a price estimation of a VM per hour, given the energy consumed of this VM and the host that
 * this VM is deployed.
 */
public class IaaSPricingModeller {
	
	//private static final String DEFAULT_IAASPRICING_PACKAGE = "eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller";

    public IaaSPricingModeller() { }

    /**
     * Returns a price estimation based on the avg. power that a VM consumes during an hour.
     *
     * @param avgPower avg. power that a VM consumes during an hour.
     * @return the price estimation
     */
    public double getVMCostEstimation(double totalEnergyUsed, int hostId) {
        double cost=0.07;
    	return cost; 
    }


    public double getVMPriceEstimation(double totalEnergyUsed, int hostId) {
    double cost=0.07;
	return cost; 
}
}