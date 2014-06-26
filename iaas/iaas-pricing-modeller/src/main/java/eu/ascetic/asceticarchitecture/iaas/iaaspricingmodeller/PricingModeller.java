package eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller;

/**
 * This is a dummy pricing modeller. It is used only to test the interaction with the VM Manager.
 *
 */
public class PricingModeller {

    public PricingModeller() { }

    /**
     * Returns a price estimation based on the avg. power that a VM consumes during an hour.
     *
     * @param avgPower avg. power that a VM consumes during an hour.
     * @return the price estimation
     */
    public double getPriceEstimationForHour(double avgPower) {
        return avgPower; //TODO :D
    }
}
