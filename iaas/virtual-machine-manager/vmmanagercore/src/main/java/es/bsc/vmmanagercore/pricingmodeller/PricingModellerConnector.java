package es.bsc.vmmanagercore.pricingmodeller;

import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.IaaSPricingModeller;

/**
 *
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class PricingModellerConnector {

    private static IaaSPricingModeller pricingModeller = new IaaSPricingModeller();

    public static double getVmCost(double totalEnergy, String hostname) {
        return pricingModeller.getVMCostEstimation(totalEnergy, hostname);
    }

}
