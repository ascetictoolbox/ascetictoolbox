package es.bsc.vmmanagercore.pricingmodeller;

import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.IaaSPricingModeller;

/**
 * Connector for the pricing modeller.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class PricingModellerConnector {

    private static IaaSPricingModeller pricingModeller = new IaaSPricingModeller();

    /**
     * Returns the predicted cost on a given host for a given amount of energy.
     *
     * @param totalEnergy total energy consumed by the VM (joules)
     * @param hostname the hostname
     * @return the predicted cost of the VM
     */
    public static double getVmCost(double totalEnergy, String hostname) {
        return pricingModeller.getVMCostEstimation(totalEnergy, hostname);
    }

}
