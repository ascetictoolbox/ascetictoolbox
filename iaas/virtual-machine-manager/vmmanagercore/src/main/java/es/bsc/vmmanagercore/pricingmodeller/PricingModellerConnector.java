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
        //TODO I should be able to send a string for the host ID
        return pricingModeller.getVMCostEstimation(totalEnergy, 1/*hostname*/);
    }

}
